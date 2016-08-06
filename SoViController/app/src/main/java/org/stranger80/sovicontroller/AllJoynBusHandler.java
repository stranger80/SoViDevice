/*
 * Developed on the basis of AllJoyn SDK Samples copyrighted as below:
 *
 * Copyright AllSeen Alliance. All rights reserved.
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH
 * REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT,
 * INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM
 * LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR
 * OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 * PERFORMANCE OF THIS SOFTWARE.
 */

package org.stranger80.sovicontroller;

import android.app.Service;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import org.alljoyn.bus.BusAttachment;
import org.alljoyn.bus.BusException;
import org.alljoyn.bus.MessageContext;
import org.alljoyn.bus.Observer;
import org.alljoyn.bus.PropertiesChangedListener;
import org.alljoyn.bus.ProxyBusObject;
import org.alljoyn.bus.Status;
import org.alljoyn.bus.Variant;
import org.alljoyn.bus.annotation.BusSignalHandler;
import org.stranger80.sovicontroller.model.ErrorDescription;
import org.stranger80.sovicontroller.model.HeatPoint;

import java.util.Map;

/** This class will handle all AllJoyn calls. */
public class AllJoynBusHandler extends Handler {
    BusAttachment busAttachment;
    private Observer busObserver;

    private SoViAgentEventListener agentListener;
    private String soViAgentName;
    private SoViAgent soViAgentProxy;
    private Messenger eventMessenger;

    //private final ConcurrentHashMap<SoViAgentAdapterItem, ProxyBusObject> soViAgentsMap = new ConcurrentHashMap<SoViAgentAdapterItem, ProxyBusObject>();


    /*
     * These are the messages sent to the BusHandler from the UI or event
     * callbacks.
     */
    public static final int CONNECT = 1;
    public static final int JOIN_SESSION = 2;
    public static final int DISCONNECT = 3;
    public static final int EXECUTE_HEATING_PATTERN = 4;
    public static final int SHUTDOWN = 5;

    public static String TAG = "AllJoynBusHandler";

    /** The port used to listen on for new incoming messages. */
    private static final short CONTACT_PORT = 42;

    /**
     * Constructor of the AllJoinBusHandler object.
     *
     * @param soViAgentName Name of specific AllJoyn object that this handler is expected to listen to.
     * @param looper
     */
    public AllJoynBusHandler(String soViAgentName, Messenger observer, Looper looper) {
        super(looper);
        this.soViAgentName = soViAgentName;
        this.eventMessenger = observer;
    }

    @Override
    public void handleMessage(Message msg) {
        boolean result = false;

        switch (msg.what) {
            /*
             * Connect to a remote instance of an object implementing the
             * BasicInterface.
             */
            case CONNECT: {
                Log.i(AllJoynBusHandler.TAG, "Connecting to bus...");

                // Send 'connecting' message
                try {
                    eventMessenger.send(Message.obtain(null, BusHandlerService.MSG_CONNECTING));
                }
                catch(RemoteException exc)
                {
                    Log.e(AllJoynBusHandler.TAG, "Error sending 'connecting' message...", exc);
                }


                Service client = (Service) msg.obj;
                org.alljoyn.bus.alljoyn.DaemonInit.PrepareDaemon(client.getApplicationContext());
                /*
                 * All communication through AllJoyn begins with a
                 * BusAttachment.
                 *
                 * A BusAttachment needs a name. The actual name is unimportant
                 * except for internal security. As a default we use the class
                 * name as the name.
                 *
                 * By default AllJoyn does not allow communication between
                 * devices (i.e. bus to bus communication). The second argument
                 * must be set to Receive to allow communication between
                 * devices.
                 */
                // TODO: this fails after a whole night of app working in background... why????
                // No implementation found for void org.alljoyn.bus.BusAttachment.create(...)
                busAttachment = new BusAttachment(client.getPackageName(), BusAttachment.RemoteMessage.Receive);

                /*
                 * To communicate with AllJoyn objects, we must connect the
                 * BusAttachment to the bus.
                 */
                Status status = busAttachment.connect();
                if (Status.OK != status) {
                    Log.e(AllJoynBusHandler.TAG, "Unable to connect to bus!");
                    client.stopSelf();
                    return;
                }

                /*
                 * Register a listener in order to receive signals emited by agent devices.
                 */
                agentListener = new SoViAgentEventListener();
                status = busAttachment.registerSignalHandlers(agentListener);
                if (status != Status.OK) {
                    Log.e(MainActivity.TAG, "Problem while registering signal handler");
                    return;
                }

                /*
                 * Create an observer and add a listener to it.
                 */
                busObserver = new Observer(busAttachment, new Class[] { SoViAgent.class });
                busObserver.registerListener(new Observer.Listener() {
                    @Override
                    public void objectLost(ProxyBusObject obj) {
                        // An agent device is no longer available. Remove it from the local
                        // list.
                        Log.d(AllJoynBusHandler.TAG, "Object lost...");

                        if(obj.getBusName().equals(soViAgentName))
                        {
                            soViAgentProxy = null;
                            // Send 'device lost' message
                            try {
                                eventMessenger.send(Message.obtain(null, BusHandlerService.MSG_DEVICE_LOST));
                            }
                            catch(RemoteException exc)
                            {
                                Log.e(AllJoynBusHandler.TAG, "Error sending 'device lost' message...", exc);
                            }
                        }
                    }

                    @Override
                    public void objectDiscovered(ProxyBusObject obj) {
                        // A new agent is found. Send a message to our worker thread.
                        Message msg = obtainMessage(JOIN_SESSION);
                        msg.obj = obj;
                        sendMessage(msg);
                    }
                });

                Log.i(AllJoynBusHandler.TAG, "Connected");

                break;
            }
            case JOIN_SESSION: {
                /*
                 * We have discovered a new device. Add it to our list and register
                 * event listeners on it.
                 */
                Log.i(AllJoynBusHandler.TAG, "New device joining...");
                ProxyBusObject obj = (ProxyBusObject) msg.obj;

                if(obj.getBusName().equals(soViAgentName))
                {
                    obj.enablePropertyCaching();
                    soViAgentProxy = obj.getInterface(SoViAgent.class);
                    obj.registerPropertiesChangedListener(SoViAgent.SOVI_AGENT_INTERFACE_NAME, new String[]{"Version", "Temperature", "Heater", "Circulation"}, agentListener);

                    // Send 'device found' message
                    try {
                        eventMessenger.send(Message.obtain(null, BusHandlerService.MSG_DEVICE_FOUND));
                    }
                    catch(RemoteException exc)
                    {
                        Log.e(AllJoynBusHandler.TAG, "Error sending 'device lost' message...", exc);
                    }

                    Log.i(AllJoynBusHandler.TAG, "Device connected: " + obj.getBusName());
                }
                else
                {
                    Log.i(AllJoynBusHandler.TAG, "Another device connected, ignoring... ");
                }

                break;
            }

            case EXECUTE_HEATING_PATTERN:
                HeatPoint[] heatPoints = (HeatPoint[])msg.obj;

                Log.i(AllJoynBusHandler.TAG, "Calling executeHeatingPattern()...");
                try {
                    result = soViAgentProxy.executeHeatPattern(heatPoints);
                    Log.i(AllJoynBusHandler.TAG, "executeHeatPattern() returned " + result);
                }
                catch(BusException exc)
                {
                    Log.e(AllJoynBusHandler.TAG, "Error in executeHeatPattern()", exc);
                    result = false;
                }
                break;

            case SHUTDOWN:
                Log.i(AllJoynBusHandler.TAG, "Calling shutdown()...");
                try {
                    if(soViAgentProxy != null) {
                        result = soViAgentProxy.shutdown();
                        Log.i(AllJoynBusHandler.TAG, "shutdown() returned " + result);
                    }
                    else
                    {
                        Log.i(AllJoynBusHandler.TAG, "No agent device to shutdown.");
                    }

                }
                catch(BusException exc)
                {
                    Log.e(AllJoynBusHandler.TAG, "Error in shutdown()", exc);
                    result = false;
                }
                break;

            /* Release all resources acquired in the connect. */
            case DISCONNECT: {
                Log.i(AllJoynBusHandler.TAG, "Disconnecting from bus...");
                busObserver.close();
                busAttachment.disconnect();
                getLooper().quit();
                Log.i(AllJoynBusHandler.TAG, "Disconnected");
                break;
            }
        }
    }

    /**
     * The SoViAgentEventListener listens to both propertyChanged events and signals.
     */
    class SoViAgentEventListener extends PropertiesChangedListener {

        @BusSignalHandler(iface = "org.stranger80.sovicontroller.SoViAgent", signal = SoViAgent.HEAT_POINT_COMPLETED)
        public void heatPointCompleted(boolean isHeatPatternComplete) {
            MessageContext msgCtx = busAttachment.getMessageContext();
            // send Heat Point Completed signal notification
            try {
                eventMessenger.send(
                        Message.obtain(null,
                                BusHandlerService.MSG_HEAT_POINT_COMPLETE,
                                isHeatPatternComplete));
            }
            catch(RemoteException exc)
            {
                Log.e(AllJoynBusHandler.TAG, "Error sending 'heat point completed' message...", exc);
            }
            Log.i(AllJoynBusHandler.TAG, "Event: Heat point completed!");
        }

        @BusSignalHandler(iface = "org.stranger80.sovicontroller.SoViAgent", signal = SoViAgent.DEVICE_ERROR_OCCURRED)
        public void deviceErrorOccurred(ErrorDescription errorDescription) {
            MessageContext msgCtx = busAttachment.getMessageContext();
            // send Error Occurred signal
            try {
                eventMessenger.send(
                        Message.obtain(null,
                                BusHandlerService.MSG_DEVICE_ERROR,
                                errorDescription));
            }
            catch(RemoteException exc)
            {
                Log.e(AllJoynBusHandler.TAG, "Error sending 'device error' message...", exc);
            }

            Log.i(AllJoynBusHandler.TAG, "Event: Device error occurred!");
        }

        /*
         * handle incoming property state changes.
         */
        @Override
        public void propertiesChanged(ProxyBusObject pObj, String ifaceName,
                                      Map<String, Variant> changed, String[] invalidated) {

            if(pObj.getBusName().equals(soViAgentName))
            {
                // Process the property change appropriately
                for(String key : changed.keySet())
                {
                    Variant v = changed.get(key);
                    if(v != null) {
                        // Send a property changed signal
                        try {
                            eventMessenger.send(
                                    Message.obtain(null,
                                            BusHandlerService.MSG_PROPERTY_CHANGED,
                                            key));
                        }
                        catch(RemoteException exc)
                        {
                            Log.e(AllJoynBusHandler.TAG, "Error sending 'property changed' message...", exc);
                        }

                    }
                }

            }

        }
    }

    public boolean isConnected()
    {
        return this.soViAgentProxy != null;
    }

    public SoViAgent getSoViAgentProxy()
    {
        return this.soViAgentProxy;
    }
}


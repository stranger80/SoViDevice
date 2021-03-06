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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.alljoyn.bus.BusAttachment;
import org.alljoyn.bus.MessageContext;
import org.alljoyn.bus.Observer;
import org.alljoyn.bus.PropertiesChangedListener;
import org.alljoyn.bus.ProxyBusObject;
import org.alljoyn.bus.Status;
import org.alljoyn.bus.Variant;
import org.alljoyn.bus.annotation.BusSignalHandler;
import org.stranger80.sovicontroller.adapter.SoViAgentAdapter;
import org.stranger80.sovicontroller.adapter.SoViAgentAdapterItem;
import org.stranger80.sovicontroller.model.ErrorDescription;
import org.stranger80.sovicontroller.model.HeatPoint;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.Pair;

/** This class will handle all AllJoyn calls. */
public class BusHandler extends Handler {
    BusAttachment busAttachment;
    private Observer busObserver;

    private SoViAgentEventListener agentListener;
    private final ConcurrentHashMap<SoViAgentAdapterItem, ProxyBusObject> soViAgentsMap = new ConcurrentHashMap<SoViAgentAdapterItem, ProxyBusObject>();
    private final SoViAgentAdapter soViAgentAdapter;

    /*
     * These are the messages sent to the BusHandler from the UI or event
     * callbacks.
     */
    public static final int CONNECT = 1;
    public static final int JOIN_SESSION = 2;
    public static final int DISCONNECT = 3;
    public static final int EXECUTE_HEATING_PATTERN = 4;
    public static final int SHUTDOWN = 5;

    public static String TAG = "BusHandler";

    /** The port used to listen on for new incoming messages. */
    private static final short CONTACT_PORT = 42;

    public BusHandler(SoViAgentAdapter adapter, Looper looper) {
        super(looper);
        soViAgentAdapter = adapter;
    }

    @Override
    public void handleMessage(Message msg) {
        SoViAgentAdapterItem agentItem = null;
        boolean result = false;

        switch (msg.what) {
        /*
         * Connect to a remote instance of an object implementing the
         * BasicInterface.
         */
            case CONNECT: {
                Log.i(BusHandler.TAG, "Connecting to bus...");
                Activity client = (Activity) msg.obj;
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
                busAttachment = new BusAttachment(client.getPackageName(), BusAttachment.RemoteMessage.Receive);

            /*
             * To communicate with AllJoyn objects, we must connect the
             * BusAttachment to the bus.
             */
                Status status = busAttachment.connect();
                if (Status.OK != status) {
                    Log.e(BusHandler.TAG, "Unable to connect to bus!");
                    client.finish();
                    return;
                }

            /*
             * Register a listener in order to receive signals emitted by agent devices.
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
                        Log.d(BusHandler.TAG, "Object lost...");

                        for (Map.Entry<SoViAgentAdapterItem, ProxyBusObject> entry : soViAgentsMap.entrySet()) {
                            if (obj == entry.getValue()) {
                                // Notify the UI.
                                soViAgentAdapter.remove(entry.getKey());
                                // Remove from internal map.
                                soViAgentsMap.remove(entry.getKey());
                                return;
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

                Log.i(BusHandler.TAG, "Connected");

                break;
            }
            case JOIN_SESSION: {
            /*
             * We have discovered a new device. Add it to our list and register
             * event listeners on it.
             */
                Log.i(BusHandler.TAG, "New device joining...");
                ProxyBusObject obj = (ProxyBusObject) msg.obj;
                obj.enablePropertyCaching();
                SoViAgent soViAgent = obj.getInterface(SoViAgent.class);
                String displayName = "SoViAgent";
                SoViAgentAdapterItem item = new SoViAgentAdapterItem(soViAgent, obj.getBusName(), displayName);
                soViAgentsMap.put(item, obj);
                obj.registerPropertiesChangedListener(SoViAgent.SOVI_AGENT_INTERFACE_NAME, new String[]{"Version", "Temperature", "Heater", "Circulation"}, agentListener);
                soViAgentAdapter.add(item);

                soViAgentAdapter.hideProgressDialog();

                Log.i(BusHandler.TAG, "Device connected: " + obj.getBusName());

                break;
            }

            case EXECUTE_HEATING_PATTERN:
                Pair<SoViAgentAdapterItem, HeatPoint[]> input = (Pair<SoViAgentAdapterItem, HeatPoint[]>)msg.obj;
                agentItem = input.first;
                HeatPoint[] heatPoints = input.second;

                Log.i(BusHandler.TAG, "Calling executeHeatingPattern()...");
                result = agentItem.executeHeatingPattern(heatPoints);
                Log.i(BusHandler.TAG, "executeHeatingPattern() returned " + result);
                break;

            case SHUTDOWN:
                agentItem = (SoViAgentAdapterItem)msg.obj;
                Log.i(BusHandler.TAG, "Calling shutdown()...");
                result = agentItem.shutdown();
                Log.i(BusHandler.TAG, "shutdown() returned " + result);
                break;

        /* Release all resources acquired in the connect. */
            case DISCONNECT: {
                Log.i(BusHandler.TAG, "Disconnecting from bus...");
                busObserver.close();
                busAttachment.disconnect();
                getLooper().quit();
                Log.i(BusHandler.TAG, "Disconnected");
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
            soViAgentAdapter.sendSignal("heatPointCompleted(" + isHeatPatternComplete + ") event received from " + msgCtx.sender);
        }

        @BusSignalHandler(iface = "org.stranger80.sovicontroller.SoViAgent", signal = SoViAgent.DEVICE_ERROR_OCCURRED)
        public void deviceErrorOccurred(ErrorDescription errorDescription) {
            MessageContext msgCtx = busAttachment.getMessageContext();
            soViAgentAdapter.sendSignal("deviceErrorOccurred([" + errorDescription.errorCode + "] " + errorDescription.erroDescription + ") event received from " + msgCtx.sender);
        }

        /*
         * handle incoming property state changes.
         */
        @Override
        public void propertiesChanged(ProxyBusObject pObj, String ifaceName,
                                      Map<String, Variant> changed, String[] invalidated) {

            SoViAgentAdapterItem agentAdapterItem = null;

            // Identify the proxy object which received property change notification
            for (Map.Entry<SoViAgentAdapterItem, ProxyBusObject> entry : soViAgentsMap.entrySet()) {
                if (pObj == entry.getValue()) {
                    agentAdapterItem = entry.getKey();
                }
            }

            // Process the property change appropriately
            for(String key : changed.keySet())
            {
                Variant v = changed.get(key);
                if(v != null) {
                    /* Use the following if any specific processing is required on the BusHandler level:
                    switch (key) {
                        case "Temperature":
                            break;
                        case "Heater":
                            break;
                        case "Circulation":
                            break;
                        default:
                            break;
                    }
                    */

                    soViAgentAdapter.propertyUpdate(agentAdapterItem, key);
                }
            }
        }
    }

}


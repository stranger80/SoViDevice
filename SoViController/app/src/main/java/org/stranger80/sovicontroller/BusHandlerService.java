package org.stranger80.sovicontroller;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import org.alljoyn.bus.BusException;
import org.stranger80.sovicontroller.dto.SoViAgentStateDTO;
import org.stranger80.sovicontroller.model.HeatPoint;

import java.util.ArrayList;

/**
 * BusHandlerService class.
 *
 * This Service is a kind of messaging hub between the AllJoyn infrastructure and the client UI.
 * Hence, two kinds of handler classes are defined here - handler for UI clients and handler for AllJoyn communication.
 *
 * Created by Miko on 2016-03-06.
 */
public class BusHandlerService extends Service {
    final Messenger mMessenger = new Messenger(new IncomingHandler()); // Target we publish for clients to send messages to IncomingHandler.

    ArrayList<Messenger> mClients = new ArrayList<Messenger>(); // Keeps track of all current registered clients.

    public static final String TAG = "BusHandlerService";
    public static final String SERVICE_NAME = "org.stranger80.sovicontroller.BusHandlerService";

    static final int MSG_REGISTER_CLIENT = 1;
    static final int MSG_UNREGISTER_CLIENT = 2;
    static final int MSG_SHUTDOWN = 3;
    static final int MSG_EXECUTE_HEAT_PATTERN = 4;

    static final int MSG_CONNECTING = 1000;
    static final int MSG_DEVICE_FOUND = 1001;
    static final int MSG_DEVICE_LOST = 1002;
    static final int MSG_PROPERTY_CHANGED = 1003;
    static final int MSG_HEAT_POINT_COMPLETE = 1004;
    static final int MSG_DEVICE_ERROR = 1005;

    private boolean isServiceRunning = false;
    private AllJoynBusHandler mBusHandler;

    /**
     * Handler class for messages coming from BusHandlerService clients.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    Log.d(BusHandlerService.TAG, "Registering service client...");
                    mClients.add(msg.replyTo);
                    // when client registered - immediately reply with full device state
                    try {
                        msg.replyTo.send(Message.obtain(null, BusHandlerService.MSG_PROPERTY_CHANGED, getDeviceState()));
                    }
                    catch (RemoteException e) {
                        Log.w(BusHandlerService.TAG, "Remote Exception", e);
                    }
                    break;
                case MSG_UNREGISTER_CLIENT:
                    Log.d(BusHandlerService.TAG, "UnRegistering service client...");
                    mClients.remove(msg.replyTo);
                    break;
                case MSG_SHUTDOWN:
                    shutdown();
                    break;
                case MSG_EXECUTE_HEAT_PATTERN:
                    executeHeatPattern((HeatPoint[])msg.obj);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    class AllJoynHandler extends Handler { // Handler of messages incoming from AllJoynBus.
        @Override
        public void handleMessage(Message msg) {
            for (int i=mClients.size()-1; i>=0; i--) {
                try {
                    switch(msg.what)
                    {
                        case BusHandlerService.MSG_DEVICE_FOUND: // intercept Device Found msg
                        case BusHandlerService.MSG_PROPERTY_CHANGED: // intercept Property Changed message and put whole device state in message
                            mClients.get(i).send(Message.obtain(null, msg.what, getDeviceState()));
                            break;
                        case BusHandlerService.MSG_HEAT_POINT_COMPLETE:
                            // TODO: pass on the message to UI and also launch notification
                            break;
                        case BusHandlerService.MSG_DEVICE_ERROR:
                            // TODO: pass on the message to UI
                            // TODO: also maybe launch notification?
                            break;
                        default:
                            // Send data
                            mClients.get(i).send(Message.obtain(null, msg.what, msg.obj));
                            break;
                    }
                }
                catch (RemoteException e) {
                    // The client is dead. Remove it from the list; we are going through the list from back to front so this is safe to do inside the loop.
                    mClients.remove(i);
                }
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if( !isServiceRunning && intent != null) {
            // Define the always-on notification to be displayed in task bar.
            // Tapping on this notification should take the user to the DevicePanelActivity for the selected device.
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.service_icon)
                            .setContentTitle(this.getString(R.string.app_name))
                            .setContentText(this.getString(R.string.controller_in_action_label))
                            .setOngoing(true);
            // Creates an explicit intent for an Activity in your app
            Intent resultIntent = new Intent(this, DevicePanelActivity.class);

            resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            // re-pack the agent name into the intent stored in the notification.
            resultIntent.putExtra(MainActivity.INTENT_AGENT_BUS_NAME, intent.getStringExtra(DevicePanelActivity.EXTRA_AGENT_NAME));

            // The stack builder object will contain an artificial back stack for the
            // started Activity.
            // This ensures that navigating backward from the Activity leads out of
            // your application to the Home screen.
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            // Adds the back stack for the Intent (but not the Intent itself)
            stackBuilder.addParentStack(MainActivity.class);
            // Adds the Intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(resultPendingIntent);
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            // mId allows you to update the notification later on.
            mNotificationManager.notify(0, mBuilder.build());

            // start Bus Handler

            Messenger eventObserver = new Messenger(new AllJoynHandler());
            String agentName = intent.getStringExtra(DevicePanelActivity.EXTRA_AGENT_NAME);

            HandlerThread busThread = new HandlerThread("BusHandler");
            busThread.start();
            mBusHandler = new AllJoynBusHandler(agentName, eventObserver, busThread.getLooper());

            /* Request connect to an AllJoyn object. */
            mBusHandler.sendMessage(mBusHandler.obtainMessage(BusHandler.CONNECT, this));

            isServiceRunning = true;
            Log.d(BusHandlerService.TAG, "Service started......");
        }
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(BusHandlerService.TAG, "OnDestroy() called...");
        super.onDestroy();

        // shutdown Bus Handler
        this.disconnect();

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.cancel(0);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    public class MyBinder extends Binder {
        BusHandlerService getService() {
            return BusHandlerService.this;
        }
    }

    /**
     * Relay the Shutdown message to the AllJoyn bus handler.
     */
    public void shutdown()
    {
        mBusHandler.sendEmptyMessage(AllJoynBusHandler.SHUTDOWN);
    }

    /**
     * Relay the Execute Heat Pattern message to the AllJoyn bus handler.
     */
    public void executeHeatPattern(HeatPoint[] heatPoints)
    {
        mBusHandler.sendMessage(
                mBusHandler.obtainMessage(
                        BusHandler.EXECUTE_HEATING_PATTERN,
                        heatPoints
                )
        );
    }

    public void disconnect()
    {
        if(mBusHandler != null) {
            mBusHandler.sendEmptyMessage(AllJoynBusHandler.DISCONNECT);
        }
    }

    /**
     * Take a snapshot of device's state and return in a DTO.
     *
     * @return
     */
    public SoViAgentStateDTO getDeviceState() {
        SoViAgentStateDTO state = new SoViAgentStateDTO();

        if(mBusHandler.isConnected())
        {
            SoViAgent proxy = mBusHandler.getSoViAgentProxy();

            try
            {
                state.setTemperature(proxy.getTemperature());
                state.setHeater(proxy.getHeater());
                state.setCirculation(proxy.getCirculation());
                state.setVersion(proxy.getVersion());
            }
            catch(BusException exc)
            {
                Log.e(BusHandlerService.TAG, "Error retrieving device state...", exc);
            }

            return state;
        }

        return state;
    }

}

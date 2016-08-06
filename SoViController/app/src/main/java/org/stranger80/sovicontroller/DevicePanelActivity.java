package org.stranger80.sovicontroller;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.stranger80.sovicontroller.control.RoundKnobButton;
import org.stranger80.sovicontroller.dto.SoViAgentStateDTO;
import org.stranger80.sovicontroller.model.HeatPoint;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Vector;

public class DevicePanelActivity extends AppCompatActivity implements RoundKnobButton.RoundKnobButtonListener {

    /* Load the native alljoyn_java library. */
    static {
        System.loadLibrary("alljoyn_java");
    }

    public static final String TAG = "DevicePanelActivity";

    public static final int MIN_TEMPERATURE = 20;
    public static final int MAX_TEMPERATURE = 100;


    static final String EXTRA_AGENT_NAME = "org.stranger80.sovicontroller.EXTRA_AGENT_NAME";

    private String soViAgentItemName;
    private UIHandler mHandler;
    private Messenger mMessenger;
    Messenger mService = null;

    private RoundKnobButton knobTemperature;
    private TextView setTempView;
    private TextView curTempView;
    private TextView curHeaterView;
    private TextView curCirculationView;
    private int timestampStart;

    // Graph view and data
    private GraphView graphView;
    private LineGraphSeries<DataPoint> tempDataSeries;
    private Vector<DataPoint> graphData = new Vector<DataPoint>();

    protected boolean isServiceBound = false;
    private boolean deviceState = false;

    /**
     * UI Handler class
     */
    private static class UIHandler extends Handler {
        String soViAgentName;
        ProgressDialog mDialog;
        DevicePanelActivity activity;
        SoViAgentStateDTO state;

        int timestampStart;

        public UIHandler(DevicePanelActivity activity, String soViAgentName, int timestampStart)
        {
            this.activity = activity;
            this.soViAgentName = soViAgentName;
            this.timestampStart = timestampStart;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BusHandlerService.MSG_CONNECTING:
                    mDialog = ProgressDialog.show(this.activity,
                            "",
                            this.activity.getString(R.string.attaching_to_device_msg),
                            true,
                            false);
                    break;

                case BusHandlerService.MSG_PROPERTY_CHANGED:
                    state = (SoViAgentStateDTO)msg.obj;
                    this.displayAgentData(state);
                    if(state != null) {
                        // add new data point to graph
                        double temperature = state.getTemperature();
                        int timeStepInt = this.activity.getCurrentTimestamp() - this.timestampStart;
                        double timeStep = timeStepInt;
                        DataPoint dataPoint = new DataPoint(timeStep, temperature);
                        this.activity.graphData.add(dataPoint);
                        this.activity.graphView.getViewport().setMaxX(dataPoint.getX());
                        this.activity.tempDataSeries.appendData(
                                dataPoint,
                                false, 300);
                    }
                    else
                    {
                        // if agent object removed (disconnected) - close the activity
                        Toast messageToast = Toast.makeText(this.activity.getApplicationContext(), "SoViAgent disconnected...", Toast.LENGTH_LONG);
                        messageToast.show();
                        this.activity.finish();
                    }
                    break;

                case BusHandlerService.MSG_DEVICE_FOUND:
                    state = (SoViAgentStateDTO)msg.obj;
                    this.displayAgentData(state);
                    if(mDialog != null) {
                        mDialog.dismiss();
                    }
                    break;
                default:
                    Log.w(DevicePanelActivity.TAG, "Unknown message received: " + msg.what);
                    break;
            }
        }

        protected SoViAgentStateDTO displayAgentData(SoViAgentStateDTO state) {
            if (state != null) {
                double temperature = state.getTemperature();
                int heater = state.getHeater();
                int circulation = state.getCirculation();

                // apply to UI
                this.activity.curTempView.setText("" + temperature + " C");
                this.activity.curHeaterView.setText(this.activity.formatHeaterValue(heater));
                this.activity.curCirculationView.setText(this.activity.formatCirculationValue(circulation));

                if (circulation > 0) {
                    this.activity.deviceState = true;
                    this.activity.knobTemperature.setState(this.activity.deviceState);
                }
            } else {
                Log.w(DevicePanelActivity.TAG, "Device state null...");
            }

            return state;
        }

    };

    private String formatHeaterValue(int heater)
    {
        if(heater < 10)
        {
            return "Off";
        }
        if(heater < 75)
        {
            return "Low";
        }
        return "High";
    }

    private String formatCirculationValue(int circulation)
    {
        if(circulation == 0)
        {
            return "Off";
        }
        return "On";
    }

    private int getCurrentTimestamp()
    {
        return (int)(System.currentTimeMillis()/1000);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        try {
            super.onCreate(savedInstanceState);

            // build UI
            setContentView(R.layout.activity_device_panel);
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            this.setTempView = (TextView) findViewById(R.id.setTempValue);
            this.curTempView = (TextView) findViewById(R.id.curTempValue);
            this.curHeaterView = (TextView) findViewById(R.id.curHeaterValue);
            this.curCirculationView = (TextView) findViewById(R.id.curCirculationValue);

            this.knobTemperature = (RoundKnobButton) findViewById(R.id.knobTemperature);

            if (knobTemperature != null) {
                this.knobTemperature.setListener(this);
            }

            // Setup the chart control
            this.graphView = (GraphView) findViewById(R.id.graph);
            this.tempDataSeries = new LineGraphSeries<DataPoint>();
            this.graphView.getViewport().setYAxisBoundsManual(true);
            this.graphView.getViewport().setMinY(DevicePanelActivity.MIN_TEMPERATURE);
            this.graphView.getViewport().setMaxY(DevicePanelActivity.MAX_TEMPERATURE);
            this.graphView.addSeries(this.tempDataSeries);

            // retrieve saved agent device name
            // if exists - there probably is a service already running and connected to this device
            if(savedInstanceState != null)
            {
                this.timestampStart = savedInstanceState.getInt("timestampStart");
                soViAgentItemName = savedInstanceState.getString(DevicePanelActivity.EXTRA_AGENT_NAME);
            }
            else
            {
                this.timestampStart = this.getCurrentTimestamp();

                // receive agent name passed in intent

                Intent intent = getIntent();
                soViAgentItemName = intent.getStringExtra(MainActivity.INTENT_AGENT_BUS_NAME);
            }

            // Setup the UI handler instance

            this.mHandler = new UIHandler(this, soViAgentItemName, this.timestampStart);
            this.mMessenger = new Messenger(this.mHandler);

            // Launch AllJoyn BusHandler service
            if(savedInstanceState == null) {
                Intent serviceIntent = new Intent(this, BusHandlerService.class);
                serviceIntent.putExtra(DevicePanelActivity.EXTRA_AGENT_NAME, soViAgentItemName);
                this.startService(serviceIntent);
            }

            // Register this activity in the service
            doBindService();

        }
        catch(Exception exc)
        {
            Log.e(DevicePanelActivity.TAG, "Unhandled exception occurred!", exc);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle instanceState)
    {
        super.onSaveInstanceState(instanceState);

        // persist
        instanceState.putString(DevicePanelActivity.EXTRA_AGENT_NAME, this.soViAgentItemName);
        instanceState.putBoolean("deviceState", this.deviceState);
        instanceState.putInt("knobTemperature.percentage", this.knobTemperature.getPercentage());
        instanceState.putSerializable("graphData", this.graphData);
        instanceState.putInt("timestampStart", this.timestampStart);

    }

    @Override
    protected void onRestoreInstanceState(Bundle instanceState)
    {
        super.onRestoreInstanceState(instanceState);

        // restore
        this.deviceState = instanceState.getBoolean("deviceState");
        this.knobTemperature.setState(this.deviceState);
        int percentage = instanceState.getInt("knobTemperature.percentage");
        int setTemp = getTemperatureFromPercentage(percentage);
        // Set the temperature on the UI and if device state is ON then send to device.
        this.setTempView.setText("" + setTemp + " C");
        this.knobTemperature.setPercentage(percentage);
        this.graphData = (Vector<DataPoint>)instanceState.getSerializable("graphData");
        for(DataPoint dataPoint : graphData ) {
            Log.d(DevicePanelActivity.TAG, "Restoring graph data, point: " + dataPoint.getX() + ", " + dataPoint.getY());
            this.tempDataSeries.appendData(
                    dataPoint
                    , true, 300);
        }
    }

    /**
     * Called when activity is destroyed - disconnect from Bus.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.doUnbindService();
    }

    /**
     * Round knob state change event handler.
     *
     * @param newstate
     */
    @Override
    public void onStateChange(boolean newstate) {

        Log.d(DevicePanelActivity.TAG, "onStateChange(" + newstate + ")...");

        this.deviceState = newstate;
        boolean result = false;

        if(newstate == false) // Shutdown the device
        {
            if(mService != null)
            {
                try {
                    Message msg = Message.obtain(null, BusHandlerService.MSG_SHUTDOWN);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                }
                catch (RemoteException e) {
                    // There is nothing special we need to do if the service has crashed.
                }
            }
        }
        else // Send the current temperature setting to the device, and start heating pattern
        {
            if(mService != null)
            {
                HeatPoint[] heatPoints = new HeatPoint[] {
                        new HeatPoint(this.getTemperatureFromPercentage(this.knobTemperature.getPercentage()), -1)
                };
                try {
                    Message msg = Message.obtain(null, BusHandlerService.MSG_EXECUTE_HEAT_PATTERN, heatPoints);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                }
                catch (RemoteException e) {
                    // There is nothing special we need to do if the service has crashed.
                }
            }
        }
    }

    /**
     * Round knob percentage state change.
     * @param percentage
     */
    @Override
    public void onRotate(int percentage) {

        int setTemp = getTemperatureFromPercentage(percentage);

        // Set the temperature on the UI and if device state is ON then send to device.
        this.setTempView.setText("" + setTemp + " C");

    }

    private int getTemperatureFromPercentage(int percentage)
    {
        return DevicePanelActivity.MIN_TEMPERATURE +
                (percentage * (DevicePanelActivity.MAX_TEMPERATURE
                        - DevicePanelActivity.MIN_TEMPERATURE))
                        /100;
    }


    // Methods related to bus handler service management

    protected void doBindService()
    {
        Log.d(DevicePanelActivity.TAG, "doBindService()...");
        Intent intent= new Intent(this, BusHandlerService.class);
        this.bindService(intent, mConnection,
                Context.BIND_AUTO_CREATE);
        isServiceBound = true;
    }

    protected void doUnbindService()
    {
        Log.d(DevicePanelActivity.TAG, "doUnbindService()...");
        if(isServiceBound)
        {
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null, BusHandlerService.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                }
                catch (RemoteException e) {
                    // There is nothing special we need to do if the service has crashed.
                }
            }
            Log.d(DevicePanelActivity.TAG, "Calling unbindService()...");
            unbindService(mConnection);
            isServiceBound = false;
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            mService = new Messenger(service);
            Log.d(DevicePanelActivity.TAG, "Service connected...");

            try {
                Message msg = Message.obtain(null, BusHandlerService.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mService.send(msg);
            }
            catch (RemoteException e) {
                // In this case the service has crashed before we could even do anything with it
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.d(DevicePanelActivity.TAG, "Service disconnected...");
            mService = null;
        }
    };


}

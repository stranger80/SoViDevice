package org.stranger80.sovicontroller;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.stranger80.sovicontroller.adapter.SoViAgentAdapter;
import org.stranger80.sovicontroller.adapter.SoViAgentAdapterItem;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    /* Load the native alljoyn_java library. */
    static {
        System.loadLibrary("alljoyn_java");
    }

    public static final int MSG_INCOMING_EVENT = 101;
    public static final int MSG_UPDATE_UI = 102;
    public static final int MSG_START_PROGRESS_DIALOG = 103;
    public static final int MSG_STOP_PROGRESS_DIALOG = 104;

    static final String INTENT_AGENT_BUS_NAME = "org.stranger80.sovicontroller.agentBusName";

    static final String TAG = "MainActivity";

    private SoViAgentAdapter mSoViAgentListAdapter;
    private ListView mSoViListView;
    private BusHandler mBusHandler;
    private ProgressBar mProgressBar;

    private final UIHandler mHandler = new UIHandler(this);

    /**
     * UI Handler class
     */
    private static class UIHandler extends Handler {
        ArrayAdapter<String> mMsgListViewArrayAdapter;
        MainActivity activity;
        private ProgressDialog mDialog;

        public UIHandler(MainActivity activity)
        {
            this.activity = activity;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_INCOMING_EVENT:
                    Log.i(MainActivity.TAG, "Incoming event:  " + msg.obj);
                    break;
                case MSG_UPDATE_UI:
                    Runnable uiTask = (Runnable) msg.obj;
                    uiTask.run();
                    break;
                case MSG_START_PROGRESS_DIALOG:
                    this.activity.mProgressBar.setVisibility(View.VISIBLE);
                    break;
                case MSG_STOP_PROGRESS_DIALOG:
                    this.activity.mProgressBar.setVisibility(View.GONE);

                    // if there are any available devices on the list - select the first one
                    /*
                    if(!this.activity.mSoViAgentListAdapter.isEmpty()) {
                        this.activity.selectAgent(0);
                    }*/

                    break;
                default:
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            this.mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

            mSoViAgentListAdapter = new SoViAgentAdapter(mHandler);

            mSoViListView = (ListView) findViewById(R.id.soViListView);
            mSoViListView.setAdapter(mSoViAgentListAdapter);

            // attach the handler to list view's click event
            mSoViListView.setOnItemClickListener(this);

            // Launch AllJoyn

            /*
             * Make all AllJoyn calls through a separate handler thread to prevent
             * blocking the UI.
             */

            HandlerThread busThread = new HandlerThread("BusHandler");
            busThread.start();
            mBusHandler = new BusHandler(mSoViAgentListAdapter, busThread.getLooper());

            /* Connect to an AllJoyn object. */

            mBusHandler.sendMessage(mBusHandler.obtainMessage(BusHandler.CONNECT, this));
            mHandler.sendEmptyMessage(MSG_START_PROGRESS_DIALOG);

        }
        catch(Exception exc)
        {
            Log.e(MainActivity.TAG, "Unhandled exception occurred!", exc);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Called when activity is destroyed - disconnect from Bus.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mBusHandler != null) {
            mBusHandler.sendEmptyMessage(BusHandler.DISCONNECT);
        }
        this.shutdownService();
    }

    /**
     * Shutdown the BusHandlerService if it runs...
     */
    private void shutdownService()
    {
        Log.d(DevicePanelActivity.TAG, "Shutting down the service...");
        Intent service = new Intent(this, BusHandlerService.class);
        boolean result = this.stopService(service);
        Log.d(DevicePanelActivity.TAG, "Service stop result: " + result);
    }

    /**
     * Handler for the click action on list of SoVi Agent devices
     *
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        this.selectAgent(position);
    }

    /**
     * Select the SoVi agent device from the list of available devices.
     *
     * @param position agent item index on the list
     */
    private void selectAgent(int position)
    {
        Log.i(MainActivity.TAG, "Selected agent " + position);
        SoViAgentAdapterItem selected = (SoViAgentAdapterItem)this.mSoViAgentListAdapter.getItem(position);

        Intent agentDetailIntent = new Intent(this, DevicePanelActivity.class);

        agentDetailIntent.putExtra(MainActivity.INTENT_AGENT_BUS_NAME, selected.getBusName());
        startActivity(agentDetailIntent);
    }
}

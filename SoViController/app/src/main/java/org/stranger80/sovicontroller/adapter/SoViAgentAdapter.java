package org.stranger80.sovicontroller.adapter;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.stranger80.sovicontroller.MainActivity;
import org.stranger80.sovicontroller.R;

import java.util.Vector;

/**
 * Created by Miko on 2016-01-24.
 */
public class SoViAgentAdapter extends BaseAdapter {

    private static class SoViAgentViewHolder {
        final TextView mName;

        public SoViAgentViewHolder(TextView name) {
            this.mName = name;
        }
    }

    /**
     * The list of device info objects. A Vector is used to synchronize access on
     * it between threads.
     */
    private final Vector<SoViAgentAdapterItem> agentItems;
    /** The list of handlers for sending messages to the UI thread. */
    private final Vector<Handler> handlers;

    public static String TAG = "SoViAgentAdapter";


    public SoViAgentAdapter(Handler handler) {
        agentItems = new Vector<SoViAgentAdapterItem>();
        handlers = new Vector<Handler>();
        handlers.add(handler);
    }

    @Override
    public int getCount() {
        return agentItems.size();
    }

    @Override
    public Object getItem(int position) {
        return agentItems.get(position);
    }

    /**
     * Extract item specified by agent device bus name.
     * @param busName
     * @return
     */
    public Object getItem(String busName) {
        for (SoViAgentAdapterItem item : this.agentItems) {
            if(item.getBusName().equals(busName))
            {
                return item;
            }
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return System.identityHashCode(agentItems.get(position));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.soviagent, parent, false);
            TextView name = (TextView) convertView.findViewById(R.id.mName);
            convertView.setTag(new SoViAgentViewHolder(name));
        }
        SoViAgentViewHolder data = (SoViAgentViewHolder) convertView.getTag();
        SoViAgentAdapterItem item = agentItems.get(position);
        data.mName.setText(item.getName());
        return convertView;
    }

    /** Add an extra agent device to the adapter and update the UI accordingly. */
    public void add(SoViAgentAdapterItem soViAgent) {
        updateUI(soViAgent, true);
    }

    /** Removes an agent device from the adapter and update the UI accordingly. */
    public void remove(SoViAgentAdapterItem soViAgent) {
        if (agentItems.contains(soViAgent)) {
            updateUI(soViAgent, false);
        }
    }

    /** Updates the UI via all handlers. Make sure the latest changes are shown on the UI. */
    private void updateUI(final SoViAgentAdapterItem soViAgentItem, final boolean add) {
        for(Handler handler : handlers) {
            handler.sendMessage(handler.obtainMessage(MainActivity.MSG_UPDATE_UI, new Runnable() {
                @Override
                public void run() {
                    if (add) {
                        Log.d(SoViAgentAdapter.TAG,"Adding device to adapter...");
                        agentItems.add(soViAgentItem);
                    } else {
                        Log.d(SoViAgentAdapter.TAG,"Removing device from adapter...");
                        agentItems.remove(soViAgentItem);
                    }
                    notifyDataSetChanged();
                }
            }));
        }
    }

    /** Show a notification in the UI upon receiving an signal from a device. */
    public void sendSignal(String string) {
        for(Handler handler : handlers) {
            handler.sendMessage(handler.obtainMessage(MainActivity.MSG_INCOMING_EVENT, string));
        }
    }

    /** Update UI after receiving an event from a device. */
    public void propertyUpdate(SoViAgentAdapterItem item, String propertyName) {
        Log.i(SoViAgentAdapter.TAG, propertyName + " property changed");
        for(Handler handler : handlers) {
            handler.sendMessage(handler.obtainMessage(MainActivity.MSG_UPDATE_UI, new Runnable() {
                @Override
                public void run() {
                    notifyDataSetChanged();
                }
            }));
        }
    }

    public void hideProgressDialog()
    {
        for(Handler handler : handlers) {
            handler.sendEmptyMessage(MainActivity.MSG_STOP_PROGRESS_DIALOG);
        }
    }

}

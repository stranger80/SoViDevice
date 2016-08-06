package org.stranger80.sovicontroller.adapter;

import android.util.Log;

import org.alljoyn.bus.BusException;
import org.stranger80.sovicontroller.model.HeatPoint;
import org.stranger80.sovicontroller.SoViAgent;

import java.io.Serializable;

/**
 * Created by Miko on 2016-01-24.
 */
public class SoViAgentAdapterItem implements Serializable{
    private final SoViAgent soViAgent;

    private final String busName;
    private final String displayName;
    static final String TAG = "SoViAgentAdapterItem";

    public SoViAgentAdapterItem(SoViAgent agent, String busName, String displayName) {
        this.soViAgent = agent;
        this.busName = busName;
        this.displayName = displayName;
    }

    public double getTemperature() {
        try {
            return this.soViAgent.getTemperature();
        }
        catch(BusException exc)
        {
            Log.e(SoViAgentAdapterItem.TAG, "Error in getTemperature()", exc);
            return -1;
        }
    }

    public int getHeater() {
        try {
            return this.soViAgent.getHeater();
        }
        catch(BusException exc)
        {
            Log.e(SoViAgentAdapterItem.TAG, "Error in getHeater()", exc);
            return -1;
        }
    }

    public int getCirculation() {
        try {
            return this.soViAgent.getCirculation();
        }
        catch(BusException exc)
        {
            Log.e(SoViAgentAdapterItem.TAG, "Error in getCirculation()", exc);
            return -1;
        }
    }

    public boolean shutdown()
    {
        try {
            return this.soViAgent.shutdown();
        }
        catch(BusException exc)
        {
            Log.e(SoViAgentAdapterItem.TAG, "Error in shutdown()", exc);
        }
        return false;
    }

    public boolean executeHeatingPattern(HeatPoint[] heatPoints)
    {
        try {
            return this.soViAgent.executeHeatPattern(heatPoints);
        }
        catch(BusException exc)
        {
            Log.e(SoViAgentAdapterItem.TAG, "Error in executeHeatPattern()", exc);
        }

        return false;
    }

    /**
     * Get display name of the device (includes current temperature on the device)
     * @return
     */
    public String getName() {
        return displayName + " (" + this.getTemperature() + "C)";
    }

    /**
     * Get object path - that identifies the device on the AllJoyn bus.
     * @return
     */
    public String getBusName() {
        return busName;
    }

}

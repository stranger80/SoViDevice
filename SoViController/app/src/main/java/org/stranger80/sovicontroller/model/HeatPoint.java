package org.stranger80.sovicontroller.model;

import org.alljoyn.bus.annotation.Position;
import org.alljoyn.bus.annotation.Signature;

/**
 * Created by Miko on 2016-01-24.
 */
public class HeatPoint {

    public HeatPoint(double temperature, int period)
    {
        this.temperature = temperature;
        this.period = period;
    }

    @Position(0)
    public double temperature;

    @Position(1)
    public int period;
}

package org.stranger80.sovicontroller.model;

import org.alljoyn.bus.annotation.Position;
import org.alljoyn.bus.annotation.Signature;

/**
 * Created by Miko on 2016-01-24.
 */
public class DeviceState {
    @Position(0)
    @Signature("u")
    public int timestamp;

    @Position(1)
    public double temperature;

    @Position(2)
    public byte heater;

    @Position(3)
    public byte circulation;
}

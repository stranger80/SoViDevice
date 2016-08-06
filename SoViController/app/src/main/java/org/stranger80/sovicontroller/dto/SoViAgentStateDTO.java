package org.stranger80.sovicontroller.dto;

import java.io.Serializable;

/**
 * Created by Miko on 2016-03-06.
 */
public class SoViAgentStateDTO implements Serializable {

    short version;
    double temperature;
    int heater;
    int circulation;

    public short getVersion() {
        return version;
    }

    public void setVersion(short version) {
        this.version = version;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public int getHeater() {
        return heater;
    }

    public void setHeater(int heater) {
        this.heater = heater;
    }

    public int getCirculation() {
        return circulation;
    }

    public void setCirculation(int circulation) {
        this.circulation = circulation;
    }

}

package org.stranger80.sovicontroller;

import org.alljoyn.bus.BusException;
import org.alljoyn.bus.annotation.BusInterface;
import org.alljoyn.bus.annotation.BusMethod;
import org.alljoyn.bus.annotation.BusProperty;
import org.alljoyn.bus.annotation.BusSignal;
import org.stranger80.sovicontroller.model.DeviceState;
import org.stranger80.sovicontroller.model.ErrorDescription;
import org.stranger80.sovicontroller.model.HeatPoint;

/**
 * Created by Miko on 2016-01-24.
 */
@BusInterface(name = SoViAgent.SOVI_AGENT_INTERFACE_NAME, announced = "true")
public interface SoViAgent {

    String SOVI_AGENT_INTERFACE_NAME = "org.stranger80.SoViAgent";
    String HEAT_POINT_COMPLETED = "HeatPointCompleted";
    String DEVICE_ERROR_OCCURRED = "DeviceErrorOccurred";

    @BusProperty(annotation = BusProperty.ANNOTATE_EMIT_CHANGED_SIGNAL, signature = "q")
    short getVersion() throws BusException;

    @BusProperty(annotation = BusProperty.ANNOTATE_EMIT_CHANGED_SIGNAL, signature = "d")
    double getTemperature() throws BusException;

    @BusProperty(annotation = BusProperty.ANNOTATE_EMIT_CHANGED_SIGNAL, signature = "i")
    int getHeater() throws BusException;

    @BusProperty(annotation = BusProperty.ANNOTATE_EMIT_CHANGED_SIGNAL, signature = "i")
    int getCirculation() throws BusException;

    @BusMethod(name = "Shutdown")
    boolean shutdown() throws BusException;

    @BusMethod(name = "ExecuteHeatPattern", signature = "a(di)")
    boolean executeHeatPattern(HeatPoint[] heatPoints) throws BusException;

    @BusMethod(name = "GetStateHistory", replySignature = "ar")
    DeviceState[] getStateHistory(int startTime) throws BusException;

    @BusSignal(name = SoViAgent.HEAT_POINT_COMPLETED)
    void heatPointCompleted(boolean isHeatPatternComplete) throws BusException;

    @BusSignal(name = SoViAgent.DEVICE_ERROR_OCCURRED)
    void deviceErrorOccurred(ErrorDescription errorDescription) throws BusException;

}

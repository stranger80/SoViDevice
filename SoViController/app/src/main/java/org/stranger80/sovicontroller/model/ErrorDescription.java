package org.stranger80.sovicontroller.model;

import org.alljoyn.bus.annotation.Position;

/**
 * Created by Miko on 2016-01-24.
 */
public class ErrorDescription {

    @Position(0)
    public int errorCode;

    @Position(1)
    public String erroDescription;
}

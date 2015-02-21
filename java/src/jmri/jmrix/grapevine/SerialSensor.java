// SerialSensor.java
package jmri.jmrix.grapevine;

import jmri.implementation.AbstractSensor;

/**
 * Implement AbstractSensor for Grapevine. Really doesn't do much, because the
 * abstract class and the SerialSensorManager do all the work in a node-based
 * system.
 * <P>
 * @author	Bob Jacobsen Copyright (C) 2003, 2006, 2007, 2008
 * @version $Revision$
 */
public class SerialSensor extends AbstractSensor {

    /**
     *
     */
    private static final long serialVersionUID = -6227425824416141982L;

    public SerialSensor(String systemName) {
        super(systemName);
        _knownState = UNKNOWN;
    }

    public SerialSensor(String systemName, String userName) {
        super(systemName, userName);
        _knownState = UNKNOWN;
    }

    /**
     * Request an update on status.
     * <P>
     * Since status is continually being updated, this isn't active now.
     * Eventually, we may want to have this move the related AIU to the top of
     * the polling queue.
     */
    public void requestUpdateFromLayout() {
    }

}

/* @(#)SerialSensor.java */

// SerialSensor.java
package jmri.jmrix.maple;

import jmri.implementation.AbstractSensor;

/**
 * Extend jmri.AbstractSensor for serial systems
 * <P>
 * @author	Bob Jacobsen Copyright (C) 2003, 2008
 */
public class SerialSensor extends AbstractSensor {

    /**
     *
     */
    private static final long serialVersionUID = -196410325440991581L;

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

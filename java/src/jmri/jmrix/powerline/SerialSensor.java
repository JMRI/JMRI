// SerialSensor.java
package jmri.jmrix.powerline;

import jmri.implementation.AbstractSensor;

/**
 * Extend jmri.AbstractSensor for serial systems
 * <P>
 * @author	Bob Jacobsen Copyright (C) 2003, 2006, 2007, 2008 Converted to
 * multiple connection
 * @author kcameron Copyright (C) 2011
 * @version $Revision$
 */
public class SerialSensor extends AbstractSensor {

    /**
     *
     */
    private static final long serialVersionUID = 7887272776433351376L;

    public SerialSensor(String systemName, SerialTrafficController tc) {
        super(systemName);
        this.tc = tc;
        _knownState = UNKNOWN;
    }

    public SerialSensor(String systemName, SerialTrafficController tc, String userName) {
        super(systemName, userName);
        this.tc = tc;
        _knownState = UNKNOWN;
    }

    SerialTrafficController tc = null;

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

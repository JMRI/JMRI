package jmri.jmrix.powerline;

import jmri.implementation.AbstractSensor;

/**
 * Extend jmri.AbstractSensor for serial systems
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2006, 2007, 2008 Converted to
 * multiple connection
 * @author kcameron Copyright (C) 2011
 */
public class SerialSensor extends AbstractSensor {

    public SerialSensor(String systemName, SerialTrafficController tc) {
        super(systemName);
        _knownState = UNKNOWN;
    }

    public SerialSensor(String systemName, SerialTrafficController tc, String userName) {
        super(systemName, userName);
        _knownState = UNKNOWN;
    }

    /**
     * Request an update on status.
     * <p>
     * Since status is continually being updated, this isn't active now.
     * Eventually, we may want to have this move the related AIU to the top of
     * the polling queue.
     */
    @Override
    public void requestUpdateFromLayout() {
    }

}



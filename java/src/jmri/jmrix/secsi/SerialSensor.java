package jmri.jmrix.secsi;

import jmri.implementation.AbstractSensor;

/**
 * Extend jmri.AbstractSensor for serial systems
 * <P>
 * @author	Bob Jacobsen Copyright (C) 2003, 2006, 2007, 2008
 */
public class SerialSensor extends AbstractSensor {

    public SerialSensor(String systemName,SecsiSystemConnectionMemo _memo) {
        super(systemName);
        _knownState = UNKNOWN;
    }

    public SerialSensor(String systemName, String userName,SecsiSystemConnectionMemo _memo) {
        super(systemName, userName);
        _knownState = UNKNOWN;
    }

    @Override
    public void dispose() {
    }

    /**
     * Request an update on status.
     * <P>
     * Since status is continually being updated, this isn't active now.
     * Eventually, we may want to have this move the related AIU to the top of
     * the polling queue.
     */
    @Override
    public void requestUpdateFromLayout() {
    }

}

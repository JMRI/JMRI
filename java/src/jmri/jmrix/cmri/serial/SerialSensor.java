package jmri.jmrix.cmri.serial;

import jmri.implementation.AbstractSensor;
import jmri.jmrix.cmri.CMRISystemConnectionMemo;

import javax.annotation.Nonnull;
import javax.annotation.CheckReturnValue;

/**
 * Extend jmri.AbstractSensor for C/MRI serial systems
 * <P>
 * @author Bob Jacobsen Copyright (C) 2003
 */
public class SerialSensor extends AbstractSensor {

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
    @Override
    public void requestUpdateFromLayout() {
    }

    /**
     * {@inheritDoc} 
     * 
     * Sorts by node number and then by bit
     */
    @CheckReturnValue
    public int compareSystemNameSuffix(@Nonnull String suffix1, @Nonnull String suffix2, @Nonnull jmri.NamedBean n) {
        return CMRISystemConnectionMemo.compareSystemNameSuffix(suffix1, suffix2);
    }

}

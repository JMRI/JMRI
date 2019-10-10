package jmri.jmrix.nce;

import jmri.implementation.AbstractSensor;

/**
 * Extend jmri.AbstractSensor for NCE systems
 *
 * @author Bob Jacobsen Copyright (C) 2003
 */
public class NceSensor extends AbstractSensor {

    public NceSensor(String systemName) {
        super(systemName);
    }

    public NceSensor(String systemName, String userName) {
        super(systemName, userName);
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

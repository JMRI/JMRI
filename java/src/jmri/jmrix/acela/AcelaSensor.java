package jmri.jmrix.acela;

import jmri.implementation.AbstractSensor;

/**
 * Extend jmri.AbstractSensor for Acela systems
 *
 * @author Bob Jacobsen Copyright (C) 2003
 * @author Bob Coleman Copyright (C) 2007, 2008 Based heavily on CMRI serial
 * example.
 */
public class AcelaSensor extends AbstractSensor {

    public AcelaSensor(String systemName) {
        super(systemName);
        _knownState = UNKNOWN;
    }

    public AcelaSensor(String systemName, String userName) {
        super(systemName, userName);
        _knownState = UNKNOWN;
    }

    /**
     * Request an update on status.
     * <p>
     * Since status is continually being updated, this isn't active now.
     */
    @Override
    public void requestUpdateFromLayout() {
    }

}

// AcelaSensor.java
package jmri.jmrix.acela;

import jmri.implementation.AbstractSensor;

/**
 * Extend jmri.AbstractSensor for Acela systems
 * <P>
 * @author	Bob Jacobsen Copyright (C) 2003
 * @version $Revision$
 *
 * @author	Bob Coleman Copyright (C) 2007, 2008 Based heavily on CMRI serial
 * example.
 */
public class AcelaSensor extends AbstractSensor {

    /**
     *
     */
    private static final long serialVersionUID = 1398533852085475990L;

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
     * <P>
     * Since status is continually being updated, this isn't active now.
     */
    public void requestUpdateFromLayout() {
    }
}

/* @(#)AcelaSensor.java */

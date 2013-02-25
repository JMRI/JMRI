// SerialSensor.java

package jmri.jmrix.secsi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.implementation.AbstractSensor;

/**
 * Extend jmri.AbstractSensor for serial systems
 * <P>
 * @author	Bob Jacobsen Copyright (C) 2003, 2006, 2007, 2008
 * @version     $Revision$
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

    public void dispose() {}

    /**
     * Request an update on status.
     * <P>
     * Since status is continually
     * being updated, this isn't active now.  Eventually, we may
     * want to have this move the related AIU to the top of the
     * polling queue.
     */
    public void requestUpdateFromLayout() {
    }

    static Logger log = LoggerFactory.getLogger(SerialSensor.class.getName());

}

/* @(#)SerialSensor.java */

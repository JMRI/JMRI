// SerialSensor.java

package jmri.jmrix.powerline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.implementation.AbstractSensor;

/**
 * Extend jmri.AbstractSensor for serial systems
 * <P>
 * @author	Bob Jacobsen Copyright (C) 2003, 2006, 2007, 2008
 * Converted to multiple connection
 * @author kcameron Copyright (C) 2011
 * @version     $Revision$
 */
public class SerialSensor extends AbstractSensor {

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

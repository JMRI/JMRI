// ConfigurationManager.java

package jmri.jmrix.openlcb;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.InstanceManager;

/**
 * Does configuration for OpenLCB communications
 * implementations.
 *
 * @author		Bob Jacobsen  Copyright (C) 2010
 * @version     $Revision$
 */
public class ConfigurationManager {

    static public void configure(String option) {
        // "OpenLCB CAN"
        ActiveFlag.setActive();
        
        InstanceManager.setTurnoutManager(new jmri.jmrix.openlcb.OlcbTurnoutManager());
        InstanceManager.setSensorManager(new jmri.jmrix.openlcb.OlcbSensorManager());
        
    }
    
}

/* @(#)ConfigurationManager.java */

// ConfigurationManager.java

package jmri.jmrix.openlcb;

/**
 * Does configuration for OpenLCB communications
 * implementations.
 *
 * @author		Bob Jacobsen  Copyright (C) 2010
 * @version     $Revision: 1.3 $
 */
public class ConfigurationManager {

    static public void configure(String option) {
        // "OpenLCB CAN"
        ActiveFlag.setActive();
        new SystemConnectionMemo();
        
        jmri.InstanceManager.setTurnoutManager(new jmri.jmrix.openlcb.OlcbTurnoutManager());
        jmri.InstanceManager.setSensorManager(new jmri.jmrix.openlcb.OlcbSensorManager());
        
    }

}

/* @(#)ConfigurationManager.java */

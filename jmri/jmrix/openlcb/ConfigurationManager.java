// ConfigurationManager.java

package jmri.jmrix.openlcb;

/**
 * Does configuration for OpenLCB communications
 * implementations.
 *
 * @author		Bob Jacobsen  Copyright (C) 2010
 * @version     $Revision: 1.2 $
 */
public class ConfigurationManager {

    static public void configure(String option) {
        new Exception("Report configure in ConfigurationManager: "+option).printStackTrace();
    
        // "OpenLCB CAN"
        ActiveFlag.setActive();
        new SystemConnectionMemo();

        // note sure if this is right place to do this
        System.out.println("Configurating in openlcb.ConfigurationManager");
        
        jmri.InstanceManager.setTurnoutManager(new jmri.jmrix.openlcb.OlcbTurnoutManager());
        jmri.InstanceManager.setSensorManager(new jmri.jmrix.openlcb.OlcbSensorManager());
        
    }

}

/* @(#)ConfigurationManager.java */

// ConfigurationManager.java

package jmri.jmrix.openlcb;

/**
 * Does configuration for OpenLCB communications
 * implementations.
 *
 * @author		Bob Jacobsen  Copyright (C) 2010
 * @version     $Revision: 1.1 $
 */
public class ConfigurationManager {

    static public void configure(String option) {
        new Exception("Report configure in ConfigurationManager: "+option).printStackTrace();
    
        // "OpenLCB CAN"
        ActiveFlag.setActive();
        new SystemConnectionMemo();

    }

}

/* @(#)ConfigurationManager.java */

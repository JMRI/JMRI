// ConfigurationManager.java

package jmri.jmrix.can;

/**
 * Does configuration for various CAN-based communications
 * implementations.
 *<p>
 * It would be good to replace this with properties-based 
 * method for redirecting to classes in particular subpackages.
 *
 * @author		Bob Jacobsen  Copyright (C) 2009
 * @version     $Revision: 1.6 $
 */
public class ConfigurationManager {

    private static final String[] options = new String[]{"MERG CBUS", "Raw CAN", "Test - do not use"};
    
    /**
     * Provide the current set of "Option1" 
     * values
     */
    static public String[] getSystemOptions() {
        return options;
    }

    static public void configure(String option) {
        if (options[0].equals(option)) {
            // "MERG CBUS"

            jmri.InstanceManager.setTurnoutManager(new jmri.jmrix.can.cbus.CbusTurnoutManager());
            jmri.InstanceManager.setSensorManager(new jmri.jmrix.can.cbus.CbusSensorManager());
            jmri.InstanceManager.setProgrammerManager(new jmri.jmrix.can.cbus.CbusDccProgrammerManager(
                    new jmri.jmrix.can.cbus.CbusDccProgrammer()));
            jmri.InstanceManager.setThrottleManager(new jmri.jmrix.can.cbus.CbusThrottleManager());
            jmri.InstanceManager.setPowerManager(new jmri.jmrix.can.cbus.CbusPowerManager());
            jmri.jmrix.can.cbus.ActiveFlag.setActive();

        } if (options[1].equals(option)) {
            // "Raw CAN"
            // This is just vanilla CAN with nothing additional
            jmri.jmrix.can.ActiveFlag.setActive();

        } if (options[2].equals(option)) {
            // "Test - do not use"
            jmri.jmrix.can.nmranet.ActiveFlag.setActive();

        } else {
            // just ignore.  null often used during reconfig process
        }
    }

}

/* @(#)ConfigurationManager.java */

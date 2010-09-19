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
 * @version     $Revision: 1.8 $
 */
public class ConfigurationManager {

    final private static String MERGCBUS = "MERG CBUS";
    final private static String OPENLCB = "OpenLCB";
    final private static String RAWCAN = "Raw CAN";
    final private static String TEST = "Test - do not use";
    
    private static final String[] options = new String[]{OPENLCB, MERGCBUS, RAWCAN, TEST};
    
    /**
     * Provide the current set of "Option1" 
     * values
     */
    static public String[] getSystemOptions() {
        return options;
    }

    static public void configure(String option) {
        if (MERGCBUS.equals(option)) {

            jmri.InstanceManager.setTurnoutManager(new jmri.jmrix.can.cbus.CbusTurnoutManager());
            jmri.InstanceManager.setSensorManager(new jmri.jmrix.can.cbus.CbusSensorManager());
            jmri.InstanceManager.setProgrammerManager(new jmri.jmrix.can.cbus.CbusDccProgrammerManager(
                    new jmri.jmrix.can.cbus.CbusDccProgrammer()));
            jmri.InstanceManager.setThrottleManager(new jmri.jmrix.can.cbus.CbusThrottleManager());
            jmri.InstanceManager.setPowerManager(new jmri.jmrix.can.cbus.CbusPowerManager());
            jmri.jmrix.can.cbus.ActiveFlag.setActive();

        } if (OPENLCB.equals(option)) {
            // Activate menu indirectly
            System.out.println("------- hit ---------");
            jmri.jmrix.openlcb.ConfigurationManager.configure(option);

        } if (RAWCAN.equals(option)) {
            // This is just vanilla CAN with nothing additional
            jmri.jmrix.can.ActiveFlag.setActive();

        } if (TEST.equals(option)) {
            // "Test - do not use"
            jmri.jmrix.can.nmranet.ActiveFlag.setActive();

        } else {
            // just ignore.  null often used during reconfig process
        }
    }

}

/* @(#)ConfigurationManager.java */

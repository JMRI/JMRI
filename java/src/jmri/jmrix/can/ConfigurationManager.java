// ConfigurationManager.java

package jmri.jmrix.can;

import java.util.ResourceBundle;

/**
 * Does configuration for various CAN-based communications
 * implementations.
 *<p>
 * It would be good to replace this with properties-based 
 * method for redirecting to classes in particular subpackages.
 *
 * @author		Bob Jacobsen  Copyright (C) 2009
 * @version     $Revision$
 */
abstract public class ConfigurationManager {

    final public static String MERGCBUS = "MERG CBUS";
    final public static String OPENLCB = "OpenLCB";
    final public static String RAWCAN = "Raw CAN";
    final public static String TEST = "Test - do not use";
    
    private static String[] options = new String[]{MERGCBUS, OPENLCB, RAWCAN, TEST};
    
    /**
     * Provide the current set of "Option1" 
     * values
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings({"EI_EXPOSE_REP", "MS_EXPOSE_REP"}) // OK until Java 1.6 allows return of cheap array copy
    static public String[] getSystemOptions() {
        return options;
    }

    /** 
     * Set the list of protocols to start with OpenLCB
     */
    static public void setOpenLCB() {   
        options = new String[]{OPENLCB, MERGCBUS, RAWCAN, TEST};
    }
    
    /** 
     * Set the list of protocols to start with MERG
     */
    static public void setMERG() {
        options = new String[]{MERGCBUS, OPENLCB, RAWCAN, TEST};
    }
    
    /*static public void configure(String option) {
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
    }*/
    
    public ConfigurationManager(CanSystemConnectionMemo memo){
        adapterMemo=memo;
    }
    
    protected CanSystemConnectionMemo adapterMemo;
    
    abstract public void configureManagers();
     
    /** 
     * Tells which managers this provides by class
     */
    abstract public boolean provides(Class<?> type);
    
    @SuppressWarnings("unchecked")
    abstract public <T> T get(Class<?> T);
    
    abstract public void dispose();
    
    abstract protected ResourceBundle getActionModelResourceBundle();

}

/* @(#)ConfigurationManager.java */

// EasyDccSystemConnectionMemo.java

package jmri.jmrix.easydcc;

import jmri.InstanceManager;

/**
 * Lightweight class to denote that a system is active,
 * and provide general information.
 * <p>
 * Objects of specific subtypes are registered
 * in the instance manager to activate their
 * particular system.
 *
 * @author		Bob Jacobsen  Copyright (C) 2010
 *              Kevin Dickerson
 * @version             $Revision: 1.2 $
 */
public class EasyDccSystemConnectionMemo extends jmri.jmrix.SystemConnectionMemo {

    public EasyDccSystemConnectionMemo(EasyDccTrafficController et) {
        super("E", "EasyDCC");
        this.et = et;
        register();
        /*InstanceManager.store(cf = new jmri.jmrix.easydcc.swing.ComponentFactory(this),
                jmri.jmrix.swing.ComponentFactory.class);*/
    }
    
    public EasyDccSystemConnectionMemo() {
        super("E", "EasyDCC");
        register(); // registers general type
        InstanceManager.store(this, EasyDccSystemConnectionMemo.class); // also register as specific type
        //Needs to be implemented
        /*InstanceManager.store(cf = new jmri.jmrix.easydcc.swing.ComponentFactory(this),
                        jmri.jmrix.swing.ComponentFactory.class);*/
    }
    
    jmri.jmrix.swing.ComponentFactory cf = null;
    
     /**
     * Provides access to the TrafficController for this
     * particular connection.
     */
    public EasyDccTrafficController getTrafficController() { return et; }
    public void setEasyDccTrafficController(EasyDccTrafficController et) { this.et = et; }
    private EasyDccTrafficController et;
    
    /**
     * Configure the common managers for Easy DCC connections.
     * This puts the common manager config in one
     * place.  This method is static so that it can be referenced
     * from classes that don't inherit.
     */
    public void configureManagers() {
      
        jmri.InstanceManager.setProgrammerManager(
            new EasyDccProgrammerManager(
                new EasyDccProgrammer()));

        jmri.InstanceManager.setPowerManager(new jmri.jmrix.easydcc.EasyDccPowerManager());

        jmri.InstanceManager.setTurnoutManager(jmri.jmrix.easydcc.EasyDccTurnoutManager.instance());
        
        jmri.InstanceManager.setThrottleManager(new jmri.jmrix.easydcc.EasyDccThrottleManager());

        jmri.InstanceManager.setConsistManager(new jmri.jmrix.easydcc.EasyDccConsistManager());

        jmri.InstanceManager.setCommandStation(new jmri.jmrix.easydcc.EasyDccCommandStation());

    }
    
    public void dispose(){
        et = null;
        InstanceManager.deregister(this, EasyDccSystemConnectionMemo.class);
        if (cf != null) 
            InstanceManager.deregister(cf, jmri.jmrix.swing.ComponentFactory.class);
        super.dispose();
    }
}


/* @(#)InternalSystemConnectionMemo.java */

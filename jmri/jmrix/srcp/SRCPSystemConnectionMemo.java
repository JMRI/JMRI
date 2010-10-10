// SRCPSystemConnectionMemo.java

package jmri.jmrix.srcp;

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
 * @version             $Revision: 1.2 $
 */
public class SRCPSystemConnectionMemo extends jmri.jmrix.SystemConnectionMemo {

    public SRCPSystemConnectionMemo(SRCPTrafficController et) {
        super("D", "SRCP");
        this.et = et;
        register();
        /*InstanceManager.store(cf = new jmri.jmrix.srcp.swing.ComponentFactory(this), 
                jmri.jmrix.swing.ComponentFactory.class);*/
    }
    
    public SRCPSystemConnectionMemo() {
        super("D", "SRCP");
        register(); // registers general type
        InstanceManager.store(this, SRCPSystemConnectionMemo.class); // also register as specific type
        //Needs to be implemented
        /*InstanceManager.store(cf = new jmri.jmrix.srcp.swing.ComponentFactory(this), 
                        jmri.jmrix.swing.ComponentFactory.class);*/
    }
    
    jmri.jmrix.swing.ComponentFactory cf = null;
    
     /**
     * Provides access to the TrafficController for this
     * particular connection.
     */
    public SRCPTrafficController getTrafficController() { return et; }
    public void setTrafficController(SRCPTrafficController et) { this.et = et; }
    private SRCPTrafficController et;
    
    /**
     * Configure the common managers for Internal connections.
     * This puts the common manager config in one
     * place.  This method is static so that it can be referenced
     * from classes that don't inherit, including hexfile.HexFileFrame
     * and locormi.LnMessageClient
     */
    public void configureManagers() {
    
        jmri.InstanceManager.setProgrammerManager(
            new SRCPProgrammerManager(
                new SRCPProgrammer()));
      
        jmri.InstanceManager.setPowerManager(new jmri.jmrix.srcp.SRCPPowerManager());

        jmri.InstanceManager.setTurnoutManager(jmri.jmrix.srcp.SRCPTurnoutManager.instance());
        
        jmri.InstanceManager.setThrottleManager(new jmri.jmrix.srcp.SRCPThrottleManager());

    }
    
    /**
     * Configure the programming manager and "command station" objects
     */
    public void configureCommandStation() {
        jmri.InstanceManager.setCommandStation(new jmri.jmrix.srcp.SRCPCommandStation());

        // start the connection
        et.sendSRCPMessage(new SRCPMessage("SET PROTOCOL SRCP 0.8.3\n"), null);
        et.sendSRCPMessage(new SRCPMessage("SET CONNECTIONMODE SRCP COMMAND\n"), null);
        et.sendSRCPMessage(new SRCPMessage("GO\n"), null);
    }
    
    public void dispose(){
        et = null;
        InstanceManager.deregister(this, SRCPSystemConnectionMemo.class);
        if (cf != null) 
            InstanceManager.deregister(cf, jmri.jmrix.swing.ComponentFactory.class);
        super.dispose();
    }
}


/* @(#)InternalSystemConnectionMemo.java */

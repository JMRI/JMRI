// LocoNetSystemConnectionMemo.java

package jmri.jmrix.loconet;

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
 * @version             $Revision: 1.11 $
 */
public class LocoNetSystemConnectionMemo extends jmri.jmrix.SystemConnectionMemo {

    public LocoNetSystemConnectionMemo(LnTrafficController lt,
                                        SlotManager sm) {
        super("L"+(instanceCount>1?""+instanceCount:""), "LocoNet"+(instanceCount>1?""+instanceCount:""));
        this.lt = lt;
        this.sm = sm;
        count = instanceCount++;
        register(); // registers general type
        InstanceManager.store(this, LocoNetSystemConnectionMemo.class); // also register as specific type
        
        // create and register the ComponentFactory
        InstanceManager.store(new jmri.jmrix.loconet.swing.ComponentFactory(this), 
                                jmri.jmrix.swing.ComponentFactory.class);
    }
    
    public LocoNetSystemConnectionMemo() {
        super("L"+(instanceCount>1?""+instanceCount:""), "LocoNet"+(instanceCount>1?""+instanceCount:""));
        count = instanceCount++;
        register(); // registers general type
        InstanceManager.store(this, LocoNetSystemConnectionMemo.class); // also register as specific type
        
        // create and register the ComponentFactory
        InstanceManager.store(new jmri.jmrix.loconet.swing.ComponentFactory(this), 
                                jmri.jmrix.swing.ComponentFactory.class);
    }


    
    private static int instanceCount = 1;
    
    private int count;
    
    /**
     * Provides access to the SlotManager for this
     * particular connection.
     */
    public SlotManager getSlotManager() { return sm; }
    private SlotManager sm;
    public void setSlotManager(SlotManager sm){ this.sm = sm;}
    
    /**
     * Provides access to the TrafficController for this
     * particular connection.
     */
    public LnTrafficController getLnTrafficController() { return lt; }
    private LnTrafficController lt;
    public void setLnTrafficController(LnTrafficController lt) { this.lt = lt; }
    public LnMessageManager getLnMessageManager() {
        // create when needed
        if (lnm == null) 
            lnm = new LnMessageManager(getLnTrafficController());
        return lnm;
    }
    private LnMessageManager lnm = null;
    
    String suffix() { return count>1?""+count:""; }
    
    /**
     * Configure the programming manager and "command station" objects
     * @param mCanRead
     * @param mProgPowersOff
     * @param name Command station type name
     */
    public void configureCommandStation(boolean mCanRead, boolean mProgPowersOff, String name) {

        // loconet.SlotManager to do programming (the Programmer instance is registered
        // when the SlotManager is created)
        // set slot manager's read capability
        sm.setCanRead(mCanRead);
        sm.setProgPowersOff(mProgPowersOff);
        sm.setCommandStationType(name);
        
        // store as CommandStation object
        jmri.InstanceManager.setCommandStation(sm);

    }

    /**
     * Configure the common managers for LocoNet connections.
     * This puts the common manager config in one
     * place.  This method is static so that it can be referenced
     * from classes that don't inherit, including hexfile.HexFileFrame
     * and locormi.LnMessageClient
     */
    public void configureManagers() {
    
        LocoNetThrottledTransmitter tm = new LocoNetThrottledTransmitter(getLnTrafficController());
        
        InstanceManager.setPowerManager(
            new jmri.jmrix.loconet.LnPowerManager(this));

        InstanceManager.setTurnoutManager(
            new jmri.jmrix.loconet.LnTurnoutManager(getLnTrafficController(), tm, getSystemPrefix()));

        InstanceManager.setLightManager(
            new jmri.jmrix.loconet.LnLightManager(getLnTrafficController(), getSystemPrefix()));

        InstanceManager.setSensorManager(
            new jmri.jmrix.loconet.LnSensorManager(getLnTrafficController(),getSystemPrefix()));

        InstanceManager.setThrottleManager(
            new jmri.jmrix.loconet.LnThrottleManager(getSlotManager()));

        InstanceManager.setReporterManager(
            new jmri.jmrix.loconet.LnReporterManager(getLnTrafficController(), getSystemPrefix()));

        InstanceManager.addClockControl(
            new jmri.jmrix.loconet.LnClockControl(getSlotManager(), getLnTrafficController()));

    }
}


/* @(#)LocoNetSystemConnectionMemo.java */

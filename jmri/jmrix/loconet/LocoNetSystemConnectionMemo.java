// LocoNetSystemConnectionMemo.java

package jmri.jmrix.loconet;

import jmri.*;

/**
 * Lightweight class to denote that a system is active,
 * and provide general information.
 * <p>
 * Objects of specific subtypes are registered
 * in the instance manager to activate their
 * particular system.
 *
 * @author		Bob Jacobsen  Copyright (C) 2010
 * @version             $Revision: 1.16 $
 */
public class LocoNetSystemConnectionMemo extends jmri.jmrix.SystemConnectionMemo {

    public LocoNetSystemConnectionMemo(LnTrafficController lt,
                                        SlotManager sm) {
        super("L", "LocoNet");
        this.lt = lt;
        this.sm = sm;
        register(); // registers general type
        InstanceManager.store(this, LocoNetSystemConnectionMemo.class); // also register as specific type
        
        // create and register the ComponentFactory
        InstanceManager.store(cf = new jmri.jmrix.loconet.swing.ComponentFactory(this), 
                                jmri.jmrix.swing.ComponentFactory.class);
    }
    
    public LocoNetSystemConnectionMemo() {
        super("L", "LocoNet");
        register(); // registers general type
        InstanceManager.store(this, LocoNetSystemConnectionMemo.class); // also register as specific type
        
        // create and register the ComponentFactory
        InstanceManager.store(cf = new jmri.jmrix.loconet.swing.ComponentFactory(this), 
                                jmri.jmrix.swing.ComponentFactory.class);
    }
    
    jmri.jmrix.swing.ComponentFactory cf = null;
    
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
    
    private ProgrammerManager programmerManager;
    
    public ProgrammerManager getProgrammerManager() {
        if (programmerManager == null)
            programmerManager = new LnProgrammerManager(getSlotManager());
        return programmerManager;
    }
    public void setProgrammerManager(ProgrammerManager p) {
        programmerManager = p;
    }
    
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
     * Currently provides only Programmer this way
     */
    public boolean provides(Class type) {
        if (type.equals(jmri.ProgrammerManager.class))
            return true;
        return false; // nothing, by default
    }
    
    /** 
     * Currently provides only Programmer this way
     */
    @SuppressWarnings("unchecked")
    public <T> T get(Class T) {
        if (T.equals(jmri.ProgrammerManager.class))
            return (T)getProgrammerManager();
        return null; // nothing, by default
    }
        
    /**
     * Configure the common managers for LocoNet connections.
     * This puts the common manager config in one
     * place.  
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

        jmri.InstanceManager.setProgrammerManager(
            getProgrammerManager());

        InstanceManager.setReporterManager(
            new jmri.jmrix.loconet.LnReporterManager(getLnTrafficController(), getSystemPrefix()));

        InstanceManager.addClockControl(
            new jmri.jmrix.loconet.LnClockControl(getSlotManager(), getLnTrafficController()));

    }
    
    public void dispose() {
        lt = null;
        sm = null;
        InstanceManager.deregister(this, LocoNetSystemConnectionMemo.class);
        if (cf != null) 
            InstanceManager.deregister(cf, jmri.jmrix.swing.ComponentFactory.class);
        super.dispose();
    }
    
}


/* @(#)LocoNetSystemConnectionMemo.java */

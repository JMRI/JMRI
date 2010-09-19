// LocoNetSystemConnectionMemo.java

package jmri.jmrix.nce;

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
 * @version             $Revision: 1.2 $
 */
public class NceSystemConnectionMemo extends jmri.jmrix.SystemConnectionMemo {

    public NceSystemConnectionMemo(NceTrafficController lt) {
        super("N", "NCE");
        this.lt = lt;
        register(); // registers general type
        InstanceManager.store(this, NceSystemConnectionMemo.class); // also register as specific type
        
        // create and register the ComponentFactory
        /*InstanceManager.store(cf = new jmri.jmrix.Nce.swing.ComponentFactory(this), 
                                jmri.jmrix.swing.ComponentFactory.class);*/
    }
    
    public NceSystemConnectionMemo() {
        super("N", "NCE");
        register(); // registers general type
        InstanceManager.store(this, NceSystemConnectionMemo.class); // also register as specific type
        
        // create and register the ComponentFactory
        /*InstanceManager.store(cf = new jmri.jmrix.Nce.swing.ComponentFactory(this), 
                                jmri.jmrix.swing.ComponentFactory.class);*/
    }
    
    jmri.jmrix.swing.ComponentFactory cf = null;
    
    public void setNceUSB(int result) { NceUSB.setUsbSystem(result); }
    public int getNceUSB() { return NceUSB.getUsbSystem(); }
    /**
     * Provides access to the TrafficController for this
     * particular connection.
     */
    public NceTrafficController getNceTrafficController() { return lt; }
    private NceTrafficController lt;
    public void setNceTrafficController(NceTrafficController lt) { this.lt = lt; }
    /*public NceMessageManager getNceMessageManager() {
        // create when needed
        if (Ncem == null) 
            Ncem = new NceMessageManager(getNceTrafficController());
        return Ncem;
    }
    private NceMessage Ncem = null;*/
    
    private ProgrammerManager programmerManager;
    
    public ProgrammerManager getProgrammerManager() {
        //Do not want to return a programmer if the system is disabled
        if (getDisabled())
                return null;
        if (programmerManager == null)
            programmerManager = new NceProgrammerManager(new NceProgrammer());
        return programmerManager;
    }
    public void setProgrammerManager(ProgrammerManager p) {
        programmerManager = p;
    }
    
    /**
     * Sets the NCE message option.
     */
    public void configureCommandStation(int val) {
        NceMessage.setCommandOptions(val);
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
        if (getDisabled())
            return null;
        if (T.equals(jmri.ProgrammerManager.class))
            return (T)getProgrammerManager();
        return null; // nothing, by default
    }
        
    /**
     * Configure the common managers for Nce connections.
     * This puts the common manager config in one
     * place.  
     */
    public void configureManagers() {
        InstanceManager.setPowerManager(
            new jmri.jmrix.nce.NcePowerManager());

        InstanceManager.setTurnoutManager(
            new jmri.jmrix.nce.NceTurnoutManager());  
            
        InstanceManager.setLightManager(
            new jmri.jmrix.nce.NceLightManager(getNceTrafficController(),getSystemPrefix()));
        
        NceSensorManager s;
        InstanceManager.setSensorManager(
            s = new jmri.jmrix.nce.NceSensorManager());
        NceTrafficController.instance().setSensorManager(s);
        

        InstanceManager.setThrottleManager(
            new jmri.jmrix.nce.NceThrottleManager());
        
        if (getNceUSB()!=NceUSB.USB_SYSTEM_NONE) {
            if (getNceUSB() != NceUSB.USB_SYSTEM_POWERHOUSE) {
                jmri.InstanceManager.setProgrammerManager(new NceProgrammerManager(
					new NceProgrammer()));
            }
        } else {
            InstanceManager.setProgrammerManager(
                getProgrammerManager());
        }

        InstanceManager.addClockControl(
           new jmri.jmrix.nce.NceClockControl());

    }
    
    public void dispose() {
        lt = null;
        InstanceManager.deregister(this, NceSystemConnectionMemo.class);
        if (cf != null) 
            InstanceManager.deregister(cf, jmri.jmrix.swing.ComponentFactory.class);
        super.dispose();
    }
    
}


/* @(#)NceSystemConnectionMemo.java */

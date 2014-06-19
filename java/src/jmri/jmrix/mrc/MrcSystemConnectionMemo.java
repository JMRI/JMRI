// MrcSystemConnectionMemo.java

package jmri.jmrix.mrc;

import jmri.*;
import java.util.ResourceBundle;

/**
 * Lightweight class to denote that a system is active,
 * and provide general information.
 * <p>
 * Objects of specific subtypes are registered
 * in the instance manager to activate their
 * particular system.
 *
 * @author      Ken Cameron Copyright (C) 2014
 * @author		Kevin Dickerson  Copyright (C) 2014
 * @version             $Revision: 23001 $
 */
public class MrcSystemConnectionMemo extends jmri.jmrix.SystemConnectionMemo {
    
    public MrcSystemConnectionMemo() {
        super("MR", "MRC");
        register(); // registers general type
        InstanceManager.store(this, MrcSystemConnectionMemo.class); // also register as specific type
        
        // create and register the ComponentFactory
        InstanceManager.store(componentFactory = new jmri.jmrix.mrc.swing.MrcComponentFactory(this), 
                                jmri.jmrix.swing.ComponentFactory.class);
    }
    
    jmri.jmrix.swing.ComponentFactory componentFactory = null;
    
    /**
     * Provides access to the TrafficController for this
     * particular connection.
     */
    public MrcTrafficController getMrcTrafficController() { return mrcTrafficController; }
    private MrcTrafficController mrcTrafficController;
    public void setMrcTrafficController(MrcTrafficController tc) { 
        mrcTrafficController = tc;
        if(tc!=null)
            tc.setAdapterMemo(this);
    }
    
    private ProgrammerManager programmerManager;
    
    public ProgrammerManager getProgrammerManager() {
        //Do not want to return a programmer if the system is disabled
        if (getDisabled())
                return null;
        if (programmerManager == null)
            programmerManager = new MrcProgrammerManager(new MrcProgrammer(getMrcTrafficController()), this);
        return programmerManager;
    }
    public void setProgrammerManager(ProgrammerManager p) {
        programmerManager = p;
    }
    
    /**
     * Sets the MRC message option.
     */
/*    public void configureCommandStation(int val) {
    	getMrcTrafficController().setCommandOptions(val);
        jmri.InstanceManager.setCommandStation(mrcTrafficController);
    }*/

    /** 
     * Tells which managers this provides by class
     */
    public boolean provides(Class<?> type) {
        if (getDisabled())
            return false;
        if (type.equals(jmri.ProgrammerManager.class))
            return true;
        if (type.equals(jmri.ThrottleManager.class))
            return true;
        if (type.equals(jmri.PowerManager.class))
            return true;
        if (type.equals(jmri.TurnoutManager.class))
            return true;
        if (type.equals(jmri.ClockControl.class))
            return true;
        /*if (type.equals(jmri.CommandStation.class))
            return true;
        if (type.equals(jmri.ConsistManager.class))
            return true;*/
        return false; // nothing, by default
    }
    
    /** 
     * Provide manager by class
     */
    @SuppressWarnings("unchecked")
    public <T> T get(Class<?> T) {
        if (getDisabled())
            return null;
        if (T.equals(jmri.ProgrammerManager.class))
            return (T)getProgrammerManager();
        if (T.equals(jmri.ThrottleManager.class))
            return (T)getThrottleManager();
        if (T.equals(jmri.PowerManager.class))
            return (T)getPowerManager();
        if (T.equals(jmri.TurnoutManager.class))
            return (T)getTurnoutManager();
        if (T.equals(jmri.ClockControl.class))
            return (T)getClockControl();
        /*if (T.equals(jmri.CommandStation.class))
            return (T)getMrcTrafficController();
        if (T.equals(jmri.ConsistManager.class))
            return (T)getConsistManager();*/
        return null; // nothing, by default
    }

    private MrcPowerManager powerManager;
    private MrcTurnoutManager turnoutManager;
    private MrcThrottleManager throttleManager;
    private MrcClockControl clockManager;
    /*private MrcConsistManager consistManager;*/
    
    /**
     * Configure the common managers for MRC connections.
     * This puts the common manager config in one
     * place.  
     */
    public void configureManagers() {
    	powerManager = new jmri.jmrix.mrc.MrcPowerManager(this);
        InstanceManager.setPowerManager(powerManager);

        turnoutManager = new jmri.jmrix.mrc.MrcTurnoutManager(getMrcTrafficController(), getSystemPrefix());
        InstanceManager.setTurnoutManager(turnoutManager);

        throttleManager = new jmri.jmrix.mrc.MrcThrottleManager(this);
        InstanceManager.setThrottleManager(throttleManager);
        
        InstanceManager.setProgrammerManager(
            getProgrammerManager());

        clockManager = new jmri.jmrix.mrc.MrcClockControl(getMrcTrafficController(), getSystemPrefix());
        InstanceManager.addClockControl(clockManager);
        
        /*consistManager = new jmri.jmrix.mrc.MrcConsistManager(this);
        InstanceManager.setConsistManager(consistManager);*/

    }

    public MrcPowerManager getPowerManager() { return powerManager; }
    public MrcTurnoutManager  getTurnoutManager() { return turnoutManager; }
    public MrcThrottleManager  getThrottleManager() { return throttleManager; }
    public MrcClockControl  getClockControl() { return clockManager; }
    /*public MrcConsistManager  getConsistManager() { return consistManager; }*/
    
    protected ResourceBundle getActionModelResourceBundle(){
        return ResourceBundle.getBundle("jmri.jmrix.mrc.MrcActionListBundle");
    }
    
    public void dispose() {
    	mrcTrafficController = null;
        InstanceManager.deregister(this, MrcSystemConnectionMemo.class);
        if (componentFactory != null) 
            InstanceManager.deregister(componentFactory, jmri.jmrix.swing.ComponentFactory.class);
        if (powerManager != null) 
            InstanceManager.deregister(powerManager, jmri.jmrix.mrc.MrcPowerManager.class);
        if (turnoutManager != null) 
            InstanceManager.deregister(turnoutManager, jmri.jmrix.mrc.MrcTurnoutManager.class);
        if (throttleManager != null)
            InstanceManager.deregister(throttleManager, jmri.jmrix.mrc.MrcThrottleManager.class);
        if (clockManager != null) 
            InstanceManager.deregister(clockManager, jmri.jmrix.mrc.MrcClockControl.class);
        /*if (consistManager != null)
        	InstanceManager.deregister(consistManager, jmri.jmrix.mrc.MrcConsistManager.class);*/
        	
        super.dispose();
    }
    
}


/* @(#)MrcSystemConnectionMemo.java */

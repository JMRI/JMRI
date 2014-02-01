// SRCPBusConnectionMemo.java

package jmri.jmrix.srcp;

import jmri.InstanceManager;
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
 * @author		Bob Jacobsen  Copyright (C) 2010
 * @version             $Revision: 21216 $
 */
public class SRCPBusConnectionMemo extends jmri.jmrix.SystemConnectionMemo {

    private int _bus = 0;

    public SRCPBusConnectionMemo(SRCPTrafficController et,String Prefix,int bus) {
        super(Prefix + bus, "SRCP");
        this.et = et;
        //this.et.setSystemConnectionMemo(this);
        _bus=bus;
        register();
    }
    
    jmri.jmrix.swing.ComponentFactory cf = null;
    
     /**
     * Provides access to the TrafficController for this
     * particular connection.
     */
    public SRCPTrafficController getTrafficController() { return et; }
    public void setTrafficController(SRCPTrafficController et) 
    { 
       this.et = et; 
       //this.et.setSystemConnectionMemo(this);
    }
    private SRCPTrafficController et;
    
    /**
     * Configure the common managers for Internal connections.
     * This puts the common manager config in one
     * place.  
     */
    public void configureManagers() {
    
        setProgrammerManager(new SRCPProgrammerManager(new SRCPProgrammer(this), this));
    
        jmri.InstanceManager.setProgrammerManager(getProgrammerManager());
     
        setPowerManager(new jmri.jmrix.srcp.SRCPPowerManager(this,_bus)); 
        jmri.InstanceManager.setPowerManager(getPowerManager());

        setTurnoutManager(new jmri.jmrix.srcp.SRCPTurnoutManager(this,_bus)); 
        jmri.InstanceManager.setTurnoutManager(getTurnoutManager());

        setSensorManager(new jmri.jmrix.srcp.SRCPSensorManager(this,_bus)); 
        jmri.InstanceManager.setSensorManager(getSensorManager());
        
        setThrottleManager(new jmri.jmrix.srcp.SRCPThrottleManager(this)); 
        jmri.InstanceManager.setThrottleManager(getThrottleManager());

    }

    /**
     * package protected function to get the bus associated with
     * this memo.
     * @return integer bus number.
     */
    int getBus() { return _bus; }
    
    /**
     * Provides access to the Programmer for this particular connection.
     * NOTE: Programmer defaults to null
     */
    public ProgrammerManager getProgrammerManager() {
        return programmerManager;
    }
    public void setProgrammerManager(ProgrammerManager p) {
        programmerManager = p;
    }

    private ProgrammerManager programmerManager=null;

    /*
     * Provides access to the Throttle Manager for this particular connection.
     */
    public ThrottleManager getThrottleManager(){
        if (throttleManager == null)
            throttleManager = new SRCPThrottleManager(this);
        return throttleManager;

    }
    public void setThrottleManager(ThrottleManager t){
         throttleManager = t;
    }

    private ThrottleManager throttleManager;

    /*
     * Provides access to the Power Manager for this particular connection.
     */
    public PowerManager getPowerManager(){
        return powerManager;
   }
    public void setPowerManager(PowerManager p){
         powerManager = p;
    }

    private PowerManager powerManager;

    /*
     * Provides access to the Sensor Manager for this particular connection.
     */
    public SensorManager getSensorManager(){
        return sensorManager;

    }
    public void setSensorManager(SensorManager s){
         sensorManager = s;
    }

    private SensorManager sensorManager=null;

    /*
     * Provides access to the Turnout Manager for this particular connection.
     * NOTE: Turnout manager defaults to NULL
     */
    public TurnoutManager getTurnoutManager(){
        return turnoutManager;

    }
    public void setTurnoutManager(TurnoutManager t){
         turnoutManager = t;
    }
    
    private TurnoutManager turnoutManager=null;

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Class<?> T) {
        if (getDisabled())
            return null;
        if (T.equals(jmri.ProgrammerManager.class))
            return (T)getProgrammerManager();
        if (T.equals(jmri.PowerManager.class))
            return (T)getPowerManager();
        if (T.equals(jmri.ThrottleManager.class))
            return (T)getThrottleManager();
        if (T.equals(jmri.SensorManager.class))
            return (T)getSensorManager();
        if (T.equals(jmri.TurnoutManager.class))
            return (T)getTurnoutManager();
        return null; // nothing, by default
    }
    
    /** 
     * Tells which managers this provides by class
     */
    @Override
    public boolean provides(Class<?> type) {
        if (getDisabled())
            return false;
        if (type.equals(jmri.ProgrammerManager.class))
            return true;
        if (type.equals(jmri.ThrottleManager.class))
            return true;
        if (type.equals(jmri.PowerManager.class))
            return true;
        if (type.equals(jmri.SensorManager.class))
            return true;
        if (type.equals(jmri.TurnoutManager.class))
            return true;
        return false; // nothing, by default
    }
    
    protected ResourceBundle getActionModelResourceBundle(){
        return ResourceBundle.getBundle("jmri.jmrix.srcp.SrcpActionListBundle");
    }
   
    public void dispose(){
        et = null;
        InstanceManager.deregister(this, SRCPBusConnectionMemo.class);
        if (cf != null) 
            InstanceManager.deregister(cf, jmri.jmrix.swing.ComponentFactory.class);
        super.dispose();
    }
}


/* @(#)SRCPBusConnectionMemo.java */

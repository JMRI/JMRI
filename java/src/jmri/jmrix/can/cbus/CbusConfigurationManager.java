// ConfigurationManager.java

package jmri.jmrix.can.cbus;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.*;

import java.util.ResourceBundle;

/**
 * Does configuration for various CAN-based communications
 * implementations.
 *<p>
 * It would be good to replace this with properties-based 
 * method for redirecting to classes in particular subpackages.
 *
 * @author		Bob Jacobsen  Copyright (C) 2009
 * @version     $Revision: 17977 $
 */
public class CbusConfigurationManager extends jmri.jmrix.can.ConfigurationManager{

    public CbusConfigurationManager(CanSystemConnectionMemo memo){
        super(memo);
        InstanceManager.store(this, CbusConfigurationManager.class);
        InstanceManager.store(cf = new jmri.jmrix.can.cbus.swing.CbusComponentFactory(adapterMemo), 
            jmri.jmrix.swing.ComponentFactory.class);
    }
    
    jmri.jmrix.swing.ComponentFactory cf = null;
    
    public void configureManagers(){
            
        InstanceManager.setPowerManager(
            getPowerManager());

        InstanceManager.setSensorManager(
            getSensorManager());
            
        InstanceManager.setTurnoutManager(
            getTurnoutManager());

        InstanceManager.setThrottleManager(
            getThrottleManager());

        jmri.InstanceManager.setProgrammerManager(
            getProgrammerManager());
            
        jmri.InstanceManager.setCommandStation(getCommandStation());
            
        jmri.jmrix.can.cbus.ActiveFlag.setActive();
    }
    
        
    /** 
     * Tells which managers this provides by class
     */
    public boolean provides(Class<?> type) {
        if (adapterMemo.getDisabled())
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
        if (type.equals(jmri.ReporterManager.class))
            return true;
        if (type.equals(jmri.CommandStation.class))
            return true;
        return false; // nothing, by default
    }
    
    @SuppressWarnings("unchecked")
    public <T> T get(Class<?> T) {
        if (adapterMemo.getDisabled())
            return null;
        if (T.equals(jmri.ProgrammerManager.class))
            return (T)getProgrammerManager();
        if (T.equals(jmri.ThrottleManager.class))
            return (T)getThrottleManager();
        if (T.equals(jmri.PowerManager.class))
            return (T)getPowerManager();
        if (T.equals(jmri.SensorManager.class))
            return (T)getSensorManager();
        if (T.equals(jmri.TurnoutManager.class))
            return (T)getTurnoutManager();
        if (T.equals(jmri.CommandStation.class))
            return (T)getCommandStation();

        return null; // nothing, by default
    }
    
    private ProgrammerManager programmerManager;
    
    public ProgrammerManager getProgrammerManager() {
        if (programmerManager == null)
            programmerManager = new CbusDccProgrammerManager(
                    new jmri.jmrix.can.cbus.CbusDccProgrammer(adapterMemo.getTrafficController()), adapterMemo);
        return programmerManager;
    }
    public void setProgrammerManager(ProgrammerManager p) {
        programmerManager = p;
    }
    
    protected CbusPowerManager powerManager;
    
    public CbusPowerManager getPowerManager() { 
        if (adapterMemo.getDisabled())
            return null;
        if (powerManager == null)
            powerManager = new CbusPowerManager(adapterMemo);
        return powerManager;
    }
    
    protected ThrottleManager throttleManager;
    
    public ThrottleManager getThrottleManager() { 
        if (adapterMemo.getDisabled())
            return null;
        if (throttleManager == null)
            throttleManager = new CbusThrottleManager(adapterMemo);
        return throttleManager;
    }
    
    public void setThrottleManager(ThrottleManager t) {
        throttleManager = t;
    }
    
    protected CbusTurnoutManager turnoutManager;
    
    public CbusTurnoutManager getTurnoutManager() { 
        if (adapterMemo.getDisabled())
            return null;
        if (turnoutManager == null)
            turnoutManager = new CbusTurnoutManager(adapterMemo);
        return turnoutManager;
    }
    
    protected CbusSensorManager sensorManager;
    
    public CbusSensorManager getSensorManager() { 
        if (adapterMemo.getDisabled())
            return null;
        if (sensorManager == null)
            sensorManager = new CbusSensorManager(adapterMemo);
        return sensorManager;
    }
    
    protected CbusCommandStation commandStation;
    
    public CbusCommandStation getCommandStation() { 
        if (adapterMemo.getDisabled())
            return null;
        if (commandStation == null)
            commandStation = new CbusCommandStation(adapterMemo);
        return commandStation;
    }
    
    public void dispose(){
        if (cf != null) 
            InstanceManager.deregister(cf, jmri.jmrix.swing.ComponentFactory.class);
        if (powerManager != null) 
            InstanceManager.deregister(powerManager, jmri.jmrix.can.cbus.CbusPowerManager.class);
        if (turnoutManager != null) 
            InstanceManager.deregister(turnoutManager, jmri.jmrix.can.cbus.CbusTurnoutManager.class);
        if (sensorManager != null) 
            InstanceManager.deregister(sensorManager, jmri.jmrix.can.cbus.CbusSensorManager.class);
        if (throttleManager != null) 
            InstanceManager.deregister((CbusThrottleManager)throttleManager, jmri.jmrix.can.cbus.CbusThrottleManager.class);
        InstanceManager.deregister(this, CbusConfigurationManager.class);
    }
    
    protected ResourceBundle getActionModelResourceBundle(){
        return ResourceBundle.getBundle("jmri.jmrix.can.CanActionListBundle");
    }

}

/* @(#)ConfigurationManager.java */

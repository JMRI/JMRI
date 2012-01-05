// ConfigurationManager.java

package jmri.jmrix.openlcb;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.InstanceManager;
import java.util.ResourceBundle;

/**
 * Does configuration for OpenLCB communications
 * implementations.
 *
 * @author		Bob Jacobsen  Copyright (C) 2010
 * @version     $Revision$
 */
public class ConfigurationManager extends jmri.jmrix.can.ConfigurationManager {

    static public void configure(String option) {
        // "OpenLCB CAN"
        ActiveFlag.setActive();
        
        InstanceManager.setTurnoutManager(new jmri.jmrix.openlcb.OlcbTurnoutManager());
        InstanceManager.setSensorManager(new jmri.jmrix.openlcb.OlcbSensorManager());
        
    }
    
    public ConfigurationManager(CanSystemConnectionMemo memo){
        super(memo);
        //At this stage without the multiple connections we can do this, but afterwards we can not.
        adapterMemo.setUserName("OpenLCB");
        adapterMemo.setSystemPrefix("M");
        
        InstanceManager.store(cf = new jmri.jmrix.openlcb.swing.OpenLcbComponentFactory(adapterMemo), 
            jmri.jmrix.swing.ComponentFactory.class);
        InstanceManager.store(this, ConfigurationManager.class);
    }
    
    jmri.jmrix.swing.ComponentFactory cf = null;
    
    public void configureManagers(){
    
        new org.openlcb.can.AliasMap();
            
        InstanceManager.setSensorManager(
            getSensorManager());
            
        InstanceManager.setTurnoutManager(
            getTurnoutManager());
            
        ActiveFlag.setActive();
    }
    
        
    /** 
     * Tells which managers this provides by class
     */
    public boolean provides(Class<?> type) {
        if (adapterMemo.getDisabled())
            return false;
        if (type.equals(jmri.SensorManager.class))
            return true;
        if (type.equals(jmri.TurnoutManager.class))
            return true;
        return false; // nothing, by default
    }
    
    @SuppressWarnings("unchecked")
    public <T> T get(Class<?> T) {
        if (adapterMemo.getDisabled())
            return null;
        if (T.equals(jmri.SensorManager.class))
            return (T)getSensorManager();
        if (T.equals(jmri.TurnoutManager.class))
            return (T)getTurnoutManager();
        return null; // nothing, by default
    }
    
    protected OlcbTurnoutManager turnoutManager;
    
    public OlcbTurnoutManager getTurnoutManager() { 
        if (adapterMemo.getDisabled())
            return null;
        if (turnoutManager == null)
            turnoutManager = new OlcbTurnoutManager(adapterMemo);
        return turnoutManager;
    }
    
    protected OlcbSensorManager sensorManager;
    
    public OlcbSensorManager getSensorManager() { 
        if (adapterMemo.getDisabled())
            return null;
        if (sensorManager == null)
            sensorManager = new OlcbSensorManager(adapterMemo);
        return sensorManager;
    }
    
    public void dispose(){
        if (turnoutManager != null) 
            InstanceManager.deregister(turnoutManager, jmri.jmrix.openlcb.OlcbTurnoutManager.class);
        if (sensorManager != null) 
            InstanceManager.deregister(sensorManager, jmri.jmrix.openlcb.OlcbSensorManager.class);
        if (cf != null) 
            InstanceManager.deregister(cf, jmri.jmrix.swing.ComponentFactory.class);
        InstanceManager.deregister(this, ConfigurationManager.class);
    }
    
    protected ResourceBundle getActionModelResourceBundle(){
        //No actions that can be loaded at startup
        return null;
    }


}

/* @(#)ConfigurationManager.java */

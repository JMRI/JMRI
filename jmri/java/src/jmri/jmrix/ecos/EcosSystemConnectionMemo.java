// EcosSystemConnectionMemo.java

package jmri.jmrix.ecos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ResourceBundle;

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
 * @version             $Revision$
 */
public class EcosSystemConnectionMemo extends jmri.jmrix.SystemConnectionMemo {

    public EcosSystemConnectionMemo(EcosTrafficController et) {
        super("U", "ECoS");
        this.et = et;
        et.setAdapterMemo(this);
        register();
        InstanceManager.store(this, EcosSystemConnectionMemo.class); // also register as specific type
        InstanceManager.store(cf = new jmri.jmrix.ecos.swing.EcosComponentFactory(this),
                jmri.jmrix.swing.ComponentFactory.class);
        prefManager = new jmri.jmrix.ecos.EcosPreferences(this);
    }
    
    public EcosSystemConnectionMemo() {
        super("U", "ECoS");
        register(); // registers general type
        InstanceManager.store(this, EcosSystemConnectionMemo.class); // also register as specific type
        //Needs to be implemented
        InstanceManager.store(cf = new jmri.jmrix.ecos.swing.EcosComponentFactory(this),
                        jmri.jmrix.swing.ComponentFactory.class);
        //jmri.InstanceManager.store(new jmri.jmrix.ecos.EcosPreferences(thie), jmri.jmrix.ecos.EcosPreferences.class);
        prefManager = new jmri.jmrix.ecos.EcosPreferences(this);
    }
    
    jmri.jmrix.swing.ComponentFactory cf = null;
    
     /**
     * Provides access to the TrafficController for this
     * particular connection.
     */
    public EcosTrafficController getTrafficController() { return et; }
    public void setEcosTrafficController(EcosTrafficController et) { 
        this.et = et;
        et.setAdapterMemo(this);
    }
    private EcosTrafficController et;

    /**
     * This puts the common manager config in one
     * place. 
     */
    public void configureManagers() {
      
        powerManager = new jmri.jmrix.ecos.EcosPowerManager(getTrafficController());
        jmri.InstanceManager.setPowerManager(powerManager);
        
        turnoutManager = new jmri.jmrix.ecos.EcosTurnoutManager(this);
        jmri.InstanceManager.setTurnoutManager(turnoutManager);
        
        locoManager = new jmri.jmrix.ecos.EcosLocoAddressManager(this);
        
        throttleManager = new jmri.jmrix.ecos.EcosDccThrottleManager(this);
        jmri.InstanceManager.setThrottleManager(throttleManager);

        sensorManager = new jmri.jmrix.ecos.EcosSensorManager(this);
        jmri.InstanceManager.setSensorManager(sensorManager);
        
        reporterManager = new jmri.jmrix.ecos.EcosReporterManager(this);
        jmri.InstanceManager.setReporterManager(reporterManager);
        
    }
    
    protected ResourceBundle getActionModelResourceBundle(){
        return ResourceBundle.getBundle("jmri.jmrix.ecos.EcosActionListBundle");
    }

    private EcosSensorManager sensorManager;
    private EcosTurnoutManager turnoutManager;
    private EcosLocoAddressManager locoManager;
    private EcosPreferences prefManager;
    private EcosDccThrottleManager throttleManager;
    private EcosPowerManager powerManager;
    private EcosReporterManager reporterManager;
    
    public EcosLocoAddressManager getLocoAddressManager() { return locoManager; }
    public EcosTurnoutManager getTurnoutManager() { return turnoutManager; }
    public EcosSensorManager getSensorManager() { return sensorManager; }
    public EcosPreferences getPreferenceManager() { return prefManager; }
    public EcosDccThrottleManager getThrottleManager() { return throttleManager; }
    public EcosPowerManager getPowerManager() { return powerManager; }
    public EcosReporterManager getReporterManager() { return reporterManager; }
    
    /** 
     * Tells which managers this provides by class
     */
    public boolean provides(Class<?> type) {
        if (getDisabled())
            return false;
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
        return false; // nothing, by default
    }
    
    @SuppressWarnings("unchecked")
    public <T> T get(Class<?> T) {
        if (getDisabled())
            return null;
        if (T.equals(jmri.ThrottleManager.class))
            return (T)getThrottleManager();
        if (T.equals(jmri.PowerManager.class))
            return (T)getPowerManager();
        if (T.equals(jmri.SensorManager.class))
            return (T)getSensorManager();
        if (T.equals(jmri.TurnoutManager.class))
            return (T)getTurnoutManager();
        if (T.equals(jmri.ReporterManager.class))
            return (T)getReporterManager();
        return null; // nothing, by default
    }
    
    @Override
    public void dispose(){
        if(sensorManager!=null){
            sensorManager.dispose();
            sensorManager=null;
        }
        if(turnoutManager!=null){
            turnoutManager.dispose();
            turnoutManager=null;
        }
        if(reporterManager!=null){
            reporterManager.dispose();
            reporterManager=null;
        }
        
        if (powerManager != null) 
            InstanceManager.deregister(powerManager, jmri.jmrix.ecos.EcosPowerManager.class);

        if (throttleManager != null) 
            InstanceManager.deregister(throttleManager, jmri.jmrix.ecos.EcosDccThrottleManager.class);
            
        et = null;
        InstanceManager.deregister(this, EcosSystemConnectionMemo.class);
        if (cf != null) 
            InstanceManager.deregister(cf, jmri.jmrix.swing.ComponentFactory.class);
        
        super.dispose();
    }
    
    static Logger log = LoggerFactory.getLogger(EcosSystemConnectionMemo.class.getName());
}


/* @(#)InternalSystemConnectionMemo.java */

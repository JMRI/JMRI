// EcosSystemConnectionMemo.java

package jmri.jmrix.ecos;

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
 * @version             $Revision: 1.6 $
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
      
        jmri.InstanceManager.setPowerManager(new jmri.jmrix.ecos.EcosPowerManager(getTrafficController()));
        
        turnoutManager = new jmri.jmrix.ecos.EcosTurnoutManager(this);
        jmri.InstanceManager.setTurnoutManager(turnoutManager);
        
        locoManager = new jmri.jmrix.ecos.EcosLocoAddressManager(this);
        
        throttleManager = new jmri.jmrix.ecos.EcosDccThrottleManager(this);
        jmri.InstanceManager.setThrottleManager(throttleManager);

        sensorManager = new jmri.jmrix.ecos.EcosSensorManager(getTrafficController(), getSystemPrefix());
        jmri.InstanceManager.setSensorManager(sensorManager);
    }
    

    private EcosSensorManager sensorManager;
    private EcosTurnoutManager turnoutManager;
    private EcosLocoAddressManager locoManager;
    private EcosPreferences prefManager;
    private EcosDccThrottleManager throttleManager;
    
    public EcosLocoAddressManager getLocoAddressManager() { return locoManager; }
    public EcosTurnoutManager getTurnoutManager() { return turnoutManager; }
    public EcosSensorManager getSensorManager() { return sensorManager; }
    public EcosPreferences getPreferenceManager() { return prefManager; }
    public EcosDccThrottleManager getThrottleManager() { return throttleManager; }
    
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
        et = null;
        InstanceManager.deregister(this, EcosSystemConnectionMemo.class);
        if (cf != null) 
            InstanceManager.deregister(cf, jmri.jmrix.swing.ComponentFactory.class);
        super.dispose();
    }
}


/* @(#)InternalSystemConnectionMemo.java */

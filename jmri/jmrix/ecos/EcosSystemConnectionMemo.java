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
 * @version             $Revision: 1.3 $
 */
public class EcosSystemConnectionMemo extends jmri.jmrix.SystemConnectionMemo {

    public EcosSystemConnectionMemo(EcosTrafficController et) {
        super("U", "ECoS");
        this.et = et;
        register();
        /*InstanceManager.store(cf = new jmri.jmrix.ecos.swing.ComponentFactory(this), 
                jmri.jmrix.swing.ComponentFactory.class);*/
    }
    
    public EcosSystemConnectionMemo() {
        super("U", "ECoS");
        register(); // registers general type
        InstanceManager.store(this, EcosSystemConnectionMemo.class); // also register as specific type
        //Needs to be implemented
        /*InstanceManager.store(cf = new jmri.jmrix.ecos.swing.ComponentFactory(this), 
                        jmri.jmrix.swing.ComponentFactory.class);*/
    }
    
    jmri.jmrix.swing.ComponentFactory cf = null;
    
     /**
     * Provides access to the TrafficController for this
     * particular connection.
     */
    public EcosTrafficController getTrafficController() { return et; }
    public void setEcosTrafficController(EcosTrafficController et) { this.et = et; }
    private EcosTrafficController et;
    
    /**
     * Configure the common managers for Internal connections.
     * This puts the common manager config in one
     * place.  This method is static so that it can be referenced
     * from classes that don't inherit, including hexfile.HexFileFrame
     * and locormi.LnMessageClient
     */
    public void configureManagers() {
      
        jmri.InstanceManager.setPowerManager(new jmri.jmrix.ecos.EcosPowerManager());

        jmri.InstanceManager.setTurnoutManager(jmri.jmrix.ecos.EcosTurnoutManager.instance());

        jmri.InstanceManager.store(
                new jmri.jmrix.ecos.EcosLocoAddressManager(),
                jmri.jmrix.ecos.EcosLocoAddressManager.class);

        //jmri.jmrix.ecos.EcosLocoAddressManager.instance();
        jmri.jmrix.ecos.EcosPreferences.instance();

        /*jmri.InstanceManager.store(
                new jmri.jmrix.ecos.EcosPreferences(),
                jmri.jmrix.ecos.EcosPreferences.class);*/


        jmri.InstanceManager.setThrottleManager(new jmri.jmrix.ecos.EcosDccThrottleManager());

        jmri.InstanceManager.setSensorManager(new jmri.managers.InternalSensorManager());

        jmri.InstanceManager.setSensorManager(jmri.jmrix.ecos.EcosSensorManager.instance());
    }
    
    public void dispose(){
        et = null;
        InstanceManager.deregister(this, EcosSystemConnectionMemo.class);
        if (cf != null) 
            InstanceManager.deregister(cf, jmri.jmrix.swing.ComponentFactory.class);
        super.dispose();
    }
}


/* @(#)InternalSystemConnectionMemo.java */

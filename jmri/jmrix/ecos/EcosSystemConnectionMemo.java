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
 * @version             $Revision: 1.1 $
 */
public class EcosSystemConnectionMemo extends jmri.jmrix.SystemConnectionMemo {

    public EcosSystemConnectionMemo(EcosTrafficController et) {
        super("U"+(instanceCount>1?""+instanceCount:""), "ECoS"+(instanceCount>1?""+instanceCount:""));
        this.et = et;
        count = instanceCount++;
        register();
    }
    
    private static int instanceCount = 1;
    
    private int count;

    String suffix() { return count>1?""+count:""; }
    
     /**
     * Provides access to the TrafficController for this
     * particular connection.
     */
    public EcosTrafficController getTrafficController() { return et; }
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

        jmri.InstanceManager.setTurnoutManager(new jmri.jmrix.ecos.EcosTurnoutManager());

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

        jmri.InstanceManager.setSensorManager(new jmri.jmrix.ecos.EcosSensorManager());

    }
}


/* @(#)InternalSystemConnectionMemo.java */

// InternalSystemConnectionMemo.java

package jmri.jmrix.internal;

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
public class InternalSystemConnectionMemo extends jmri.jmrix.SystemConnectionMemo {

    public InternalSystemConnectionMemo() {
        super("I", "Internal");
        register();
    }
    
    private static int instanceCount = 1;
    
    private int count;

    String suffix() { return count>1?""+count:""; }
    
    /**
     * Configure the common managers for Internal connections.
     * This puts the common manager config in one
     * place.  This method is static so that it can be referenced
     * from classes that don't inherit, including hexfile.HexFileFrame
     * and locormi.LnMessageClient
     */
    public void configureManagers() {
      
        InstanceManager.setTurnoutManager(new jmri.managers.InternalTurnoutManager());

        InstanceManager.setSensorManager(new jmri.managers.InternalSensorManager());

    }
}


/* @(#)InternalSystemConnectionMemo.java */

package jmri.jmrix.powerline.insteon2412s;

import jmri.InstanceManager;

/**
 * Lightweight class to denote that a system is active, and provide general
 * information.
 * <p>
 * Objects of specific subtypes are registered in the instance manager to
 * activate their particular system.
 *
 * @author	Bob Jacobsen Copyright (C) 2010 copied from powerline class as part
 * of the multiple connections
 * @author	Ken Cameron Copyright (C) 2011
 */
public class SpecificSystemConnectionMemo extends jmri.jmrix.powerline.SerialSystemConnectionMemo {

    public SpecificSystemConnectionMemo() {
        super();
    }

    /**
     * Configure the common managers for Powerline connections. This puts the
     * common manager config in one place.
     */
    public void configureManagers() {
        InstanceManager.setLightManager(new jmri.jmrix.powerline.insteon2412s.SpecificLightManager(getTrafficController()));
        InstanceManager.setSensorManager(new jmri.jmrix.powerline.insteon2412s.SpecificSensorManager(getTrafficController()));
        InstanceManager.setTurnoutManager(new jmri.jmrix.powerline.SerialTurnoutManager(getTrafficController()));
    }

    public void dispose() {
        InstanceManager.deregister(this, SpecificSystemConnectionMemo.class);
        super.dispose();
    }

}



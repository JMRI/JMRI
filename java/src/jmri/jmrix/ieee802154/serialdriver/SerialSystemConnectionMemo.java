package jmri.jmrix.ieee802154.serialdriver;

import java.util.ResourceBundle;
import jmri.InstanceManager;

/**
 * Lightweight class to denote that a system is active, and provide general
 * information.
 * <p>
 * Objects of specific subtypes are registered in the instance manager to
 * activate their particular system.
 *
 * @author Bob Jacobsen Copyright (C) 2010 copied from NCE into powerline for
 * multiple connections by
 * @author Ken Cameron Copyright (C) 2011 copied from powerline into IEEE802154
 * for multiple connections by
 * @author Paul Bender Copyright (C) 2013
 */
public class SerialSystemConnectionMemo extends jmri.jmrix.ieee802154.IEEE802154SystemConnectionMemo {

    public SerialSystemConnectionMemo() {
        super();
    }

    /**
     * Tells which managers this class provides.
     */
    @Override
    public boolean provides(Class<?> type) {
        if (getDisabled()) {
            return false;
        }
        return false; // nothing, by default
    }

    /**
     * Provide manager by class
     */
    @Override
    public <T> T get(Class<?> T) {
        if (getDisabled()) {
            return null;
        }
        return null; // nothing, by default
    }

    /**
     * Configure the common managers for Powerline connections. This puts the
     * common manager config in one place.
     */
    @Override
    public void configureManagers() {
        // now does nothing here, it's done by the specific class
    }

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("jmri.jmrix.ieee802154.IEEE802154ActionListBundle");
    }

    @Override
    public void dispose() {
        InstanceManager.deregister(this, SerialSystemConnectionMemo.class);
        super.dispose();
    }

}




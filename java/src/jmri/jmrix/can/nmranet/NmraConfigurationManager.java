// ConfigurationManager.java
package jmri.jmrix.can.nmranet;

import java.util.ResourceBundle;
import jmri.InstanceManager;
import jmri.jmrix.can.CanSystemConnectionMemo;

/**
 * Does configuration for Nmra Net communications implementations.
 *
 * @author	Bob Jacobsen Copyright (C) 2010
 * @version $Revision: 17977 $
 */
public class NmraConfigurationManager extends jmri.jmrix.can.ConfigurationManager {

    public NmraConfigurationManager(CanSystemConnectionMemo memo) {
        super(memo);
        InstanceManager.store(cf = new jmri.jmrix.can.nmranet.swing.NmraNetComponentFactory(adapterMemo),
                jmri.jmrix.swing.ComponentFactory.class);
        InstanceManager.store(this, NmraConfigurationManager.class);
    }

    jmri.jmrix.swing.ComponentFactory cf = null;

    public void configureManagers() {

        ActiveFlag.setActive();
    }

    /**
     * Tells which managers this provides by class
     */
    public boolean provides(Class<?> type) {
        if (adapterMemo.getDisabled()) {
            return false;
        }
        return false; // nothing, by default
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Class<?> T) {
        if (adapterMemo.getDisabled()) {
            return null;
        }
        return null; // nothing, by default
    }

    public void dispose() {
        if (cf != null) {
            InstanceManager.deregister(cf, jmri.jmrix.swing.ComponentFactory.class);
        }
        InstanceManager.deregister(this, NmraConfigurationManager.class);
    }

    protected ResourceBundle getActionModelResourceBundle() {
        //No actions that can be loaded at startup
        return null;
    }

}

/* @(#)ConfigurationManager.java */

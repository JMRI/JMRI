package jmri.jmrix.can;

import java.util.ResourceBundle;
import jmri.InstanceManager;

/**
 * Does configuration for CAN communications implementations.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 */
public class CanConfigurationManager extends ConfigurationManager {

    public CanConfigurationManager(CanSystemConnectionMemo memo) {
        super(memo);
        InstanceManager.store(cf = new jmri.jmrix.can.swing.CanComponentFactory(adapterMemo),
                jmri.jmrix.swing.ComponentFactory.class);
        InstanceManager.store(this, CanConfigurationManager.class);
    }

    jmri.jmrix.swing.ComponentFactory cf = null;

    @Override
    public void configureManagers() {
    }

    /**
     * Tells which managers this class provides.
     */
    @Override
    public boolean provides(Class<?> type) {
        if (adapterMemo.getDisabled()) {
            return false;
        }
        return false; // nothing, by default
    }

    @Override
    public <T> T get(Class<?> T) {
        if (adapterMemo.getDisabled()) {
            return null;
        }
        return null; // nothing, by default
    }

    @Override
    public void dispose() {
        if (cf != null) {
            InstanceManager.deregister(cf, jmri.jmrix.swing.ComponentFactory.class);
        }
        InstanceManager.deregister(this, CanConfigurationManager.class);
    }

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        //No actions that can be loaded at startup
        return null;
    }

}

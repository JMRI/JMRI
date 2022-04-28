package jmri.jmrix.can;

import java.util.ResourceBundle;
import jmri.InstanceManager;

/**
 * Does configuration for CAN communications implementations.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 */
public class CanConfigurationManager extends ConfigurationManager {

    /**
     * Create a new CanConfigurationManager.
     * @param memo System Connection
     */
    public CanConfigurationManager(CanSystemConnectionMemo memo) {
        super(memo);
        InstanceManager.store(cf = new jmri.jmrix.can.swing.CanComponentFactory(adapterMemo),
            jmri.jmrix.swing.ComponentFactory.class);
        addToCanConfigMgr();
    }

    private final jmri.jmrix.swing.ComponentFactory cf;
    
    protected final void addToCanConfigMgr() {
        InstanceManager.store(this, CanConfigurationManager.class);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void configureManagers() {
    }

    /**
     * Tells which managers this class provides.
     * {@inheritDoc} 
     */
    @Override
    public boolean provides(Class<?> type) {
        return false; // nothing, by default
    }

    /**
     * Returns nothing by default.
     * {@inheritDoc}
     */
    @Override
    public <T> T get(Class<?> T) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        if (cf != null) {
            InstanceManager.deregister(cf, jmri.jmrix.swing.ComponentFactory.class);
        }
        InstanceManager.deregister(this, CanConfigurationManager.class);
    }

    /**
     * No actions that can be loaded at startup.
     * {@inheritDoc}
     */
    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return null;
    }

}

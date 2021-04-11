package jmri.jmrit.logixng.actions.swing;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.JPanel;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.swing.AbstractSwingConfigurator;

/**
 * Abstract class for SwingConfiguratorInterface
 * 
 * @author Daniel Bergqvist Copyright 2021
 */
public abstract class AbstractAnalogActionSwing extends AbstractSwingConfigurator {

    protected JPanel panel;
    
    /** {@inheritDoc} */
    @Override
    public BaseManager<? extends NamedBean> getManager() {
        return InstanceManager.getDefault(AnalogActionManager.class);
    }
    
    /** {@inheritDoc} */
    @Override
    public JPanel getConfigPanel(@Nonnull JPanel buttonPanel) throws IllegalArgumentException {
        createPanel(null, buttonPanel);
        return panel;
    }
    
    /** {@inheritDoc} */
    @Override
    public JPanel getConfigPanel(@Nonnull Base object, @Nonnull JPanel buttonPanel) throws IllegalArgumentException {
        createPanel(object, buttonPanel);
        return panel;
    }
    
    protected abstract void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel);
    
    /** {@inheritDoc} */
    @Override
    public String getExampleSystemName() {
        return InstanceManager.getDefault(AnalogActionManager.class).getSystemNamePrefix() + "AA10";
    }
    
    /** {@inheritDoc} */
    @Override
    public String getAutoSystemName() {
        return InstanceManager.getDefault(AnalogActionManager.class).getAutoSystemName();
    }
    
}

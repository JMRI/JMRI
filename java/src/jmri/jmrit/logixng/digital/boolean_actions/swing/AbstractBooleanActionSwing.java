package jmri.jmrit.logixng.digital.boolean_actions.swing;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.JPanel;
import jmri.InstanceManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.DigitalBooleanActionManager;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;

/**
 * Abstract class for SwingConfiguratorInterface
 */
public abstract class AbstractBooleanActionSwing implements SwingConfiguratorInterface {

    protected JPanel panel;
    
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
    public String getAutoSystemName() {
        return InstanceManager.getDefault(DigitalBooleanActionManager.class).getAutoSystemName();
    }
    
}

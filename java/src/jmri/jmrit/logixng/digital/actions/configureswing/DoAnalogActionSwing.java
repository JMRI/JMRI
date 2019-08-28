package jmri.jmrit.logixng.digital.actions.configureswing;

import javax.annotation.Nonnull;
import javax.swing.JPanel;
import jmri.InstanceManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.digital.actions.DoAnalogAction;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configures an ActionTurnout object with a Swing JPanel.
 */
public class DoAnalogActionSwing implements SwingConfiguratorInterface {

    private JPanel panel;
    
    
    /** {@inheritDoc} */
    @Override
    public JPanel getConfigPanel() throws IllegalArgumentException {
        createPanel();
        return panel;
    }
    
    /** {@inheritDoc} */
    @Override
    public JPanel getConfigPanel(@Nonnull Base object) throws IllegalArgumentException {
        createPanel();
        return panel;
    }
    
    private void createPanel() {
        panel = new JPanel();
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull StringBuilder errorMessage) {
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName) {
        DoAnalogAction action = new DoAnalogAction(systemName);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @Nonnull String userName) {
        DoAnalogAction action = new DoAnalogAction(systemName, userName);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }
    
    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        // Do nothing
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("DoAnalogAction_Short");
    }
    
    @Override
    public void dispose() {
    }
    
}

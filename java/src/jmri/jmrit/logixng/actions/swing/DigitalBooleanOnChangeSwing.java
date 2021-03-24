package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.JPanel;

import jmri.InstanceManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.actions.DigitalBooleanOnChange;
import jmri.jmrit.logixng.DigitalBooleanActionManager;

/**
 * Configures an ActionTurnout object with a Swing JPanel.
 */
public class DigitalBooleanOnChangeSwing extends AbstractBooleanActionSwing {

    DigitalBooleanOnChange.Trigger type = DigitalBooleanOnChange.Trigger.CHANGE;
    
    
    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        panel = new JPanel();
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        DigitalBooleanOnChange action = new DigitalBooleanOnChange(systemName, userName, type);
        return InstanceManager.getDefault(DigitalBooleanActionManager.class).registerAction(action);
    }
    
    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        // Do nothing
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("DigitalBooleanOnChange_Short");
    }
    
    @Override
    public void dispose() {
    }
    
}

package jmri.jmrit.logixng.digital.boolean_actions.swing;

import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.JPanel;
import jmri.InstanceManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.digital.boolean_actions.OnChange;
import jmri.jmrit.logixng.DigitalBooleanActionManager;

/**
 * Configures an ActionTurnout object with a Swing JPanel.
 */
public class OnChangeSwing extends AbstractBooleanActionSwing {

    OnChange.ChangeType type = OnChange.ChangeType.CHANGE;
    
    
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
        OnChange action = new OnChange(systemName, userName, type);
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
        return Bundle.getMessage("OnChange_Short");
    }
    
    @Override
    public void dispose() {
    }
    
}

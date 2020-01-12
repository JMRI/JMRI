package jmri.jmrit.logixng.digital.actions.swing;

import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.JPanel;
import jmri.InstanceManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.digital.actions.IfThenElse;

/**
 * Configures an ActionTurnout object with a Swing JPanel.
 */
public class IfThenElseSwing extends AbstractActionSwing {

    IfThenElse.Type type = IfThenElse.Type.TRIGGER_ACTION;
    
    
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
        IfThenElse action = new IfThenElse(systemName, userName, type);
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
        return Bundle.getMessage("IfThenElse_Short");
    }
    
    @Override
    public void dispose() {
    }
    
}

package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.JPanel;

import jmri.InstanceManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.DigitalBooleanActionManager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.actions.DigitalBooleanMany;

/**
 * Configures an ActionTurnout object with a Swing JPanel.
 * 
 * @author Daniel Bergqvist Copyright 2021
 */
public class DigitalBooleanManySwing extends AbstractBooleanActionSwing {

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
        DigitalBooleanMany action = new DigitalBooleanMany(systemName, userName);
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
        return Bundle.getMessage("Many_Short");
    }
    
    @Override
    public void dispose() {
    }
    
}

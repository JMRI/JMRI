package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.JLabel;
import javax.swing.JPanel;

import jmri.InstanceManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.actions.SimulateTurnoutFeedback;

/**
 * Configures an SimulateTurnoutFeedback object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class SimulateTurnoutFeedbackSwing extends AbstractDigitalActionSwing {

    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        panel = new JPanel();
        panel.add(new JLabel(Bundle.getMessage("SimulateTurnoutFeedback_Info")));
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        SimulateTurnoutFeedback action = new SimulateTurnoutFeedback(systemName, userName);
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
        return Bundle.getMessage("SimulateTurnoutFeedback_Short");
    }

    @Override
    public void dispose() {
    }

}

package jmri.jmrit.logixng.actions.swing;

import java.util.*;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.Error;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectStringSwing;

/**
 * Configures an Error object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2022
 */
public class ErrorSwing extends AbstractDigitalActionSwing {

    private JPanel _panelMessage;
    private LogixNG_SelectStringSwing _selectMessageSwing;

    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        if ((object != null) && (! (object instanceof Error))) {
            throw new IllegalArgumentException("object is not a Error: " + object.getClass().getName());
        }
        Error action = (Error)object;

        panel = new JPanel();

        _panelMessage = new JPanel();
        _selectMessageSwing = new LogixNG_SelectStringSwing(getJDialog(), this);
        if (action != null) {
            _panelMessage = _selectMessageSwing.createPanel(action.getSelectMessage());
        } else {
            _panelMessage = _selectMessageSwing.createPanel(null);
        }

        panel.add(new JLabel(Bundle.getMessage("Error_Message")));
        panel.add(_panelMessage);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary action to test formula
        Error action = new Error("IQDA1", null);
        _selectMessageSwing.validate(action.getSelectMessage(), errorMessages);
        return errorMessages.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        Error action = new Error(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof Error)) {
            throw new IllegalArgumentException("object is not a Error: " + object.getClass().getName());
        }
        Error action = (Error)object;
        _selectMessageSwing.updateObject(action.getSelectMessage());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("Error_Short");
    }

    @Override
    public void dispose() {
        _selectMessageSwing.dispose();
    }

}

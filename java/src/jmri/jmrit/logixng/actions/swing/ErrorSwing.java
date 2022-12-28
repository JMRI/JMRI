package jmri.jmrit.logixng.actions.swing;

import java.util.*;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.Error;

/**
 * Configures an Error object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2022
 */
public class ErrorSwing extends AbstractDigitalActionSwing {

    private JTextField _format;

    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        if ((object != null) && (! (object instanceof Error))) {
            throw new IllegalArgumentException("object is not a Error: " + object.getClass().getName());
        }
        Error error = (Error)object;

        panel = new JPanel();

        _format = new JTextField(20);
        panel.add(new JLabel(Bundle.getMessage("Error_Message")));
        panel.add(_format);


        if (error != null) {
            _format.setText(error.getMessage());
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        return true;
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
        Error error = (Error)object;
        error.setMessage(_format.getText());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("Error_Short");
    }

    @Override
    public void dispose() {
    }

}

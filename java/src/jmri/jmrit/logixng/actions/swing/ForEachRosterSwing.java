package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ForEachRoster;

/**
 * Configures an ForEachRoster object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2026
 */
public class ForEachRosterSwing extends AbstractDigitalActionSwing {

    private JTextField _localVariable;

    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        if ((object != null) && !(object instanceof ForEachRoster)) {
            throw new IllegalArgumentException("object must be an ForEachRoster but is a: "+object.getClass().getName());
        }

        ForEachRoster action = (ForEachRoster)object;

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel localVariablePanel = new JPanel();
        localVariablePanel.add(new JLabel(Bundle.getMessage("ForEachRosterSwing_LocalVariable")));
        _localVariable = new JTextField(20);
        localVariablePanel.add(_localVariable);
        panel.add(localVariablePanel);

        if (action != null) {
            _localVariable.setText(action.getLocalVariableName());
        }

        panel.add(localVariablePanel);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        ForEachRoster action = new ForEachRoster(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (!(object instanceof ForEachRoster)) {
            throw new IllegalArgumentException("object must be an ForEachRoster but is a: "+object.getClass().getName());
        }

        ForEachRoster action = (ForEachRoster)object;

        action.setLocalVariableName(_localVariable.getText());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("ForEachRoster_Short");
    }

    @Override
    public void dispose() {
    }

//    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ForEachSwing.class);

}

package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import jmri.InstanceManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.actions.LogLocalVariables;

/**
 * Configures an LogLocalVariables object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class LogLocalVariablesSwing extends AbstractDigitalActionSwing {

    private JCheckBox _includeGlobalVariables;

    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        LogLocalVariables action = (LogLocalVariables)object;

        panel = new JPanel();

        _includeGlobalVariables = new JCheckBox(Bundle.getMessage("LogLocalVariablesSwing_IncludeGlobalVariables"));
        panel.add(_includeGlobalVariables);

        if (action != null) {
            _includeGlobalVariables.setSelected(action.isIncludeGlobalVariables());
        } else {
            _includeGlobalVariables.setSelected(true);
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
        LogLocalVariables action = new LogLocalVariables(systemName, userName);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (!(object instanceof LogLocalVariables)) {
            throw new IllegalArgumentException("object must be an LogLocalVariables but is a: "+object.getClass().getName());
        }

        LogLocalVariables action = (LogLocalVariables)object;

        action.setIncludeGlobalVariables(_includeGlobalVariables.isSelected());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("LogLocalVariables_Short");
    }

    @Override
    public void dispose() {
    }

}

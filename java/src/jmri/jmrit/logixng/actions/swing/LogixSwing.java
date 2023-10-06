package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import jmri.InstanceManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.actions.Logix;
import jmri.jmrit.logixng.actions.Logix.ExecuteType;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an ActionTurnout object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class LogixSwing extends AbstractDigitalActionSwing {

    private JComboBox<ExecuteType> _executeTypeComboBox;

    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        if ((object != null) && !(object instanceof Logix)) {
            throw new IllegalArgumentException("object must be an Logix but is a: "+object.getClass().getName());
        }

        Logix action = (Logix)object;

        _executeTypeComboBox = new JComboBox<>();
        for (ExecuteType type : ExecuteType.values()) _executeTypeComboBox.addItem(type);
        JComboBoxUtil.setupComboBoxMaxRows(_executeTypeComboBox);
        if (action != null) _executeTypeComboBox.setSelectedItem(action.getExecuteType());

        panel = new JPanel();
        panel.add(_executeTypeComboBox);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        Logix action = new Logix(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (!(object instanceof Logix)) {
            throw new IllegalArgumentException("object must be an Logix but is a: "+object.getClass().getName());
        }

        Logix action = (Logix)object;

        action.setExecuteType(_executeTypeComboBox.getItemAt(_executeTypeComboBox.getSelectedIndex()));
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("Logix_Short");
    }

    @Override
    public void dispose() {
    }

}

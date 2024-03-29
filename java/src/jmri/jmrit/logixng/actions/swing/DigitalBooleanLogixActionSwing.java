package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import jmri.InstanceManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.DigitalBooleanActionManager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.actions.DigitalBooleanLogixAction;
import jmri.jmrit.logixng.actions.DigitalBooleanLogixAction.When;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an ActionTurnout object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class DigitalBooleanLogixActionSwing extends AbstractBooleanActionSwing {

    DigitalBooleanLogixAction.When type = DigitalBooleanLogixAction.When.Either;
    private JComboBox<DigitalBooleanLogixAction.When> _triggerComboBox;

    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        DigitalBooleanLogixAction action = (DigitalBooleanLogixAction)object;

        panel = new JPanel();
        _triggerComboBox = new JComboBox<>();
        for (When e : When.values()) {
            _triggerComboBox.addItem(e);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_triggerComboBox);
        panel.add(_triggerComboBox);
        if (action != null) {
            _triggerComboBox.setSelectedItem(action.getTrigger());
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
        DigitalBooleanLogixAction action = new DigitalBooleanLogixAction(systemName, userName, type);
        updateObject(action);
        return InstanceManager.getDefault(DigitalBooleanActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof DigitalBooleanLogixAction)) {
            throw new IllegalArgumentException("object must be an DigitalBooleanLogixAction but is a: "+object.getClass().getName());
        }
        DigitalBooleanLogixAction action = (DigitalBooleanLogixAction)object;
        action.setTrigger(_triggerComboBox.getItemAt(_triggerComboBox.getSelectedIndex()));
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("DigitalBooleanLogixAction_Short");
    }

    @Override
    public void dispose() {
    }

}

package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.actions.IfThenElse;
import jmri.jmrit.logixng.actions.IfThenElse.Type;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an ActionTurnout object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class IfThenElseSwing extends AbstractDigitalActionSwing {

    private JComboBox<Type> _typeComboBox;


    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        if ((object != null) && !(object instanceof IfThenElse)) {
            throw new IllegalArgumentException("object must be an IfThenElse but is a: "+object.getClass().getName());
        }

        IfThenElse action = (IfThenElse)object;

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        _typeComboBox = new JComboBox<>();
        for (Type type : Type.values()) _typeComboBox.addItem(type);
        JComboBoxUtil.setupComboBoxMaxRows(_typeComboBox);
        if (action != null) _typeComboBox.setSelectedItem(action.getType());

        JPanel typePanel = new JPanel();
        typePanel.add(_typeComboBox);
        panel.add(typePanel);

        JPanel labelPanel = new JPanel();
        labelPanel.add(new JLabel(Bundle.getMessage("IfThenElse_Info")));
        panel.add(labelPanel);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        IfThenElse action = new IfThenElse(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (!(object instanceof IfThenElse)) {
            throw new IllegalArgumentException("object must be an IfThenElse but is a: "+object.getClass().getName());
        }

        IfThenElse action = (IfThenElse)object;

        action.setType(_typeComboBox.getItemAt(_typeComboBox.getSelectedIndex()));
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

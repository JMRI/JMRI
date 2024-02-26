package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.IfThenElse;
import jmri.jmrit.logixng.actions.IfThenElse.ExecuteType;
import jmri.jmrit.logixng.actions.IfThenElse.EvaluateType;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an ActionTurnout object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class IfThenElseSwing extends AbstractDigitalActionSwing {

    private JComboBox<ExecuteType> _executeTypeComboBox;
    private JComboBox<EvaluateType> _evaluateTypeComboBox;


    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        if ((object != null) && !(object instanceof IfThenElse)) {
            throw new IllegalArgumentException("object must be an IfThenElse but is a: "+object.getClass().getName());
        }

        IfThenElse action = (IfThenElse)object;

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        _executeTypeComboBox = new JComboBox<>();
        for (ExecuteType type : ExecuteType.values()) _executeTypeComboBox.addItem(type);
        JComboBoxUtil.setupComboBoxMaxRows(_executeTypeComboBox);
        if (action != null) {
            _executeTypeComboBox.setSelectedItem(action.getExecuteType());
        } else {
            LogixNGPreferences prefs = InstanceManager.getDefault(LogixNGPreferences.class);
            _executeTypeComboBox.setSelectedItem(prefs.getIfThenElseExecuteTypeDefault());
        }

        _evaluateTypeComboBox = new JComboBox<>();
        for (EvaluateType type : EvaluateType.values()) _evaluateTypeComboBox.addItem(type);
        JComboBoxUtil.setupComboBoxMaxRows(_evaluateTypeComboBox);
        if (action != null) _evaluateTypeComboBox.setSelectedItem(action.getEvaluateType());

        JPanel typeOuterPanel = new JPanel();
        JPanel typePanel = new JPanel();
        java.awt.GridLayout layout = new java.awt.GridLayout(0,1);
        typePanel.setLayout(layout);
        layout.setVgap(15);
        typePanel.add(_executeTypeComboBox);
        typePanel.add(_evaluateTypeComboBox);
        typeOuterPanel.add(typePanel);
        panel.add(typeOuterPanel);

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

        action.setExecuteType(_executeTypeComboBox.getItemAt(_executeTypeComboBox.getSelectedIndex()));
        action.setEvaluateType(_evaluateTypeComboBox.getItemAt(_evaluateTypeComboBox.getSelectedIndex()));
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

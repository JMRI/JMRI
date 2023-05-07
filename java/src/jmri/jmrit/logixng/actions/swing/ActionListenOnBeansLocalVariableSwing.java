package jmri.jmrit.logixng.actions.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionListenOnBeansLocalVariable;
import jmri.jmrit.logixng.actions.NamedBeanType;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an ActionListenOnBeansLocalVariable object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2022
 */
public class ActionListenOnBeansLocalVariableSwing extends AbstractDigitalActionSwing {

    private JComboBox<NamedBeanType> _namedBeanTypeComboBox;
    private JCheckBox _listenOnAllPropertiesCheckBox;
    private JTextField _localVariableBeanToListenOn;
    private JTextField _localVariableNamedBean;
    private JTextField _localVariableEvent;
    private JTextField _localVariableNewValue;

    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        if ((object != null) && !(object instanceof ActionListenOnBeansLocalVariable)) {
            throw new IllegalArgumentException("object must be an ActionListenOnBeansLocalVariable but is a: "+object.getClass().getName());
        }

        ActionListenOnBeansLocalVariable action = (ActionListenOnBeansLocalVariable)object;

        JLabel namedBeanTypeLabel = new JLabel(Bundle.getMessage("ActionListenOnBeansLocalVariableSwing_NamedBeanType"));
        _namedBeanTypeComboBox = new JComboBox<>();
        for (NamedBeanType item : NamedBeanType.values()) {
            _namedBeanTypeComboBox.addItem(item);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_namedBeanTypeComboBox);

        JLabel listenOnAllPropertiesLabel = new JLabel(Bundle.getMessage("ActionListenOnBeansLocalVariableSwing_ListenOnAllPropertiesCheckBox"));
        _listenOnAllPropertiesCheckBox = new JCheckBox();

        JLabel localVariableBeanToListenOnLabel = new JLabel(Bundle.getMessage("ActionListenOnBeansLocalVariableSwing_LocalVariableBeanToListenOn"));
        _localVariableBeanToListenOn = new JTextField(20);

        JLabel localVariableNamedBeanLabel = new JLabel(Bundle.getMessage("ActionListenOnBeansSwing_LocalVariableNamedBean"));
        _localVariableNamedBean = new JTextField(20);

        JLabel localVariableEventLabel = new JLabel(Bundle.getMessage("ActionListenOnBeansSwing_LocalVariableEvent"));
        _localVariableEvent = new JTextField(20);

        JLabel localVariableNewValueLabel = new JLabel(Bundle.getMessage("ActionListenOnBeansSwing_LocalVariableNewValue"));
        _localVariableNewValue = new JTextField(20);

        panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints constraint = new GridBagConstraints();
        constraint.gridwidth = 1;
        constraint.gridheight = 1;
        constraint.gridx = 0;
        constraint.gridy = 0;
        constraint.anchor = GridBagConstraints.EAST;
        panel.add(namedBeanTypeLabel, constraint);
        namedBeanTypeLabel.setLabelFor(_namedBeanTypeComboBox);
        constraint.gridy = 1;
        panel.add(listenOnAllPropertiesLabel, constraint);
        listenOnAllPropertiesLabel.setLabelFor(_listenOnAllPropertiesCheckBox);
        constraint.gridy = 2;
        panel.add(localVariableBeanToListenOnLabel, constraint);
        localVariableBeanToListenOnLabel.setLabelFor(_localVariableBeanToListenOn);
        constraint.gridy = 3;
        panel.add(localVariableNamedBeanLabel, constraint);
        localVariableNamedBeanLabel.setLabelFor(_localVariableNamedBean);
        constraint.gridy = 4;
        panel.add(localVariableEventLabel, constraint);
        localVariableEventLabel.setLabelFor(_localVariableEvent);
        constraint.gridy = 5;
        panel.add(localVariableNewValueLabel, constraint);
        localVariableNewValueLabel.setLabelFor(_localVariableNewValue);

        // Add some space
        constraint.gridx = 1;
        constraint.gridy = 0;
        panel.add(new JLabel(" "), constraint);

        constraint.gridx = 2;
        constraint.gridy = 0;
        constraint.anchor = GridBagConstraints.WEST;
        panel.add(_namedBeanTypeComboBox, constraint);
        constraint.gridy = 1;
        panel.add(_listenOnAllPropertiesCheckBox, constraint);
        constraint.gridy = 2;
        panel.add(_localVariableBeanToListenOn, constraint);
        constraint.gridy = 3;
        panel.add(_localVariableNamedBean, constraint);
        constraint.gridy = 4;
        panel.add(_localVariableEvent, constraint);
        constraint.gridy = 5;
        panel.add(_localVariableNewValue, constraint);

        if (action != null) {
            _namedBeanTypeComboBox.setSelectedItem(action.getNamedBeanType());
            _listenOnAllPropertiesCheckBox.setSelected(action.getListenOnAllProperties());

            _localVariableBeanToListenOn.setText(action.getLocalVariableBeanToListenOn());

            _localVariableNamedBean.setText(action.getLocalVariableNamedBean());
            _localVariableEvent.setText(action.getLocalVariableEvent());
            _localVariableNewValue.setText(action.getLocalVariableNewValue());
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
        ActionListenOnBeansLocalVariable action = new ActionListenOnBeansLocalVariable(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (!(object instanceof ActionListenOnBeansLocalVariable)) {
            throw new IllegalArgumentException("object must be an ActionListenOnBeansLocalVariable but is a: "+object.getClass().getName());
        }

        ActionListenOnBeansLocalVariable action = (ActionListenOnBeansLocalVariable)object;
        if (_namedBeanTypeComboBox.getSelectedIndex() != -1) {
            action.setNamedBeanType(_namedBeanTypeComboBox.getItemAt(_namedBeanTypeComboBox.getSelectedIndex()));
        }
        action.setListenOnAllProperties(_listenOnAllPropertiesCheckBox.isSelected());

        action.setLocalVariableBeanToListenOn(_localVariableBeanToListenOn.getText());

        action.setLocalVariableNamedBean(_localVariableNamedBean.getText());
        action.setLocalVariableEvent(_localVariableEvent.getText());
        action.setLocalVariableNewValue(_localVariableNewValue.getText());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("ActionListenOnBeansLocalVariable_Short");
    }

    @Override
    public void dispose() {
    }


//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionListenOnBeansLocalVariableSwing.class);

}

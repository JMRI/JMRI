package jmri.jmrit.logixng.actions.swing;

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

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel namedBeanTypePanel = new JPanel();
        namedBeanTypePanel.add(new JLabel(Bundle.getMessage("ActionListenOnBeansLocalVariableSwing_NamedBeanType")));
        _namedBeanTypeComboBox = new JComboBox<>();
        for (NamedBeanType item : NamedBeanType.values()) {
            _namedBeanTypeComboBox.addItem(item);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_namedBeanTypeComboBox);
        namedBeanTypePanel.add(_namedBeanTypeComboBox);
        panel.add(namedBeanTypePanel);

        JPanel listenOnAllPropertiesPanel = new JPanel();
        listenOnAllPropertiesPanel.add(new JLabel(Bundle.getMessage("ActionListenOnBeansLocalVariableSwing_ListenOnAllPropertiesCheckBox")));
        _listenOnAllPropertiesCheckBox = new JCheckBox();
        listenOnAllPropertiesPanel.add(_listenOnAllPropertiesCheckBox);
        panel.add(listenOnAllPropertiesPanel);

        JPanel localVariableBeanToListenOnPanel = new JPanel();
        localVariableBeanToListenOnPanel.add(new JLabel(Bundle.getMessage("ActionListenOnBeansLocalVariableSwing_LocalVariableBeanToListenOn")));
        _localVariableBeanToListenOn = new JTextField(20);
        localVariableBeanToListenOnPanel.add(_localVariableBeanToListenOn);
        panel.add(localVariableBeanToListenOnPanel);

        JPanel localVariableNamedBeanPanel = new JPanel();
        localVariableNamedBeanPanel.add(new JLabel(Bundle.getMessage("ActionListenOnBeansSwing_LocalVariableNamedBean")));
        _localVariableNamedBean = new JTextField(20);
        localVariableNamedBeanPanel.add(_localVariableNamedBean);
        panel.add(localVariableNamedBeanPanel);

        JPanel localVariableNamedEventPanel = new JPanel();
        localVariableNamedEventPanel.add(new JLabel(Bundle.getMessage("ActionListenOnBeansSwing_LocalVariableEvent")));
        _localVariableEvent = new JTextField(20);
        localVariableNamedEventPanel.add(_localVariableEvent);
        panel.add(localVariableNamedEventPanel);

        JPanel localVariableNewValuePanel = new JPanel();
        localVariableNewValuePanel.add(new JLabel(Bundle.getMessage("ActionListenOnBeansSwing_LocalVariableNewValue")));
        _localVariableNewValue = new JTextField(20);
        localVariableNewValuePanel.add(_localVariableNewValue);
        panel.add(localVariableNewValuePanel);

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

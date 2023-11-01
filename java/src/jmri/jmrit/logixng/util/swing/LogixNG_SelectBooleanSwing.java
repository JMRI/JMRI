package jmri.jmrit.logixng.util.swing;

import java.util.List;

import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.LogixNG_SelectBoolean;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.util.swing.BeanSelectPanel;

/**
 * Swing class for jmri.jmrit.logixng.util.LogixNG_SelectBoolean.
 *
 * @author Daniel Bergqvist (C) 2023
 */
public class LogixNG_SelectBooleanSwing {

    private final JDialog _dialog;
    private final LogixNG_SelectTableSwing _selectTableSwing;

    private JTabbedPane _tabbedPane;
    private JCheckBox _valueCheckBox;
    private JPanel _panelDirect;
    private JPanel _panelReference;
    private JPanel _panelMemory;
    private JPanel _panelLocalVariable;
    private JPanel _panelFormula;
    private JPanel _panelTable;
    private JTextField _referenceTextField;
    private BeanSelectPanel<Memory> _memoryPanel;
    private JCheckBox _listenToMemoryCheckBox;
    private JTextField _localVariableTextField;
    private JTextField _formulaTextField;


    public LogixNG_SelectBooleanSwing(
            @Nonnull JDialog dialog,
            @Nonnull SwingConfiguratorInterface swi) {
        _dialog = dialog;
        _selectTableSwing = new LogixNG_SelectTableSwing(_dialog, swi);
    }

    public JPanel createPanel(@Nonnull LogixNG_SelectBoolean selectBoolean) {

        JPanel panel = new JPanel();

        _tabbedPane = new JTabbedPane();
        _panelDirect = new javax.swing.JPanel();
        _panelReference = new javax.swing.JPanel();
        _panelMemory = new JPanel();
        _panelLocalVariable = new javax.swing.JPanel();
        _panelFormula = new javax.swing.JPanel();
        _panelTable = _selectTableSwing.createPanel(selectBoolean.getSelectTable());

        _memoryPanel = new BeanSelectPanel<>(InstanceManager.getDefault(MemoryManager.class), null);
        _listenToMemoryCheckBox = new JCheckBox(Bundle.getMessage("ListenToMemory"));

        _panelMemory.setLayout(new BoxLayout(_panelMemory, BoxLayout.Y_AXIS));
        _panelMemory.add(_memoryPanel);
        _panelMemory.add(_listenToMemoryCheckBox);

        _tabbedPane.addTab(NamedBeanAddressing.Direct.toString(), _panelDirect);
        _tabbedPane.addTab(NamedBeanAddressing.Reference.toString(), _panelReference);
        _tabbedPane.addTab(NamedBeanAddressing.Memory.toString(), _panelMemory);
        _tabbedPane.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelLocalVariable);
        _tabbedPane.addTab(NamedBeanAddressing.Formula.toString(), _panelFormula);
        _tabbedPane.addTab(NamedBeanAddressing.Table.toString(), _panelTable);

        _valueCheckBox = new JCheckBox();
        _panelDirect.add(_valueCheckBox);

        _referenceTextField = new JTextField();
        _referenceTextField.setColumns(30);
        _panelReference.add(_referenceTextField);

        _localVariableTextField = new JTextField();
        _localVariableTextField.setColumns(30);
        _panelLocalVariable.add(_localVariableTextField);

        _formulaTextField = new JTextField();
        _formulaTextField.setColumns(30);
        _panelFormula.add(_formulaTextField);


        switch (selectBoolean.getAddressing()) {
            case Direct: _tabbedPane.setSelectedComponent(_panelDirect); break;
            case Reference: _tabbedPane.setSelectedComponent(_panelReference); break;
            case Memory: _tabbedPane.setSelectedComponent(_panelMemory); break;
            case LocalVariable: _tabbedPane.setSelectedComponent(_panelLocalVariable); break;
            case Formula: _tabbedPane.setSelectedComponent(_panelFormula); break;
            case Table: _tabbedPane.setSelectedComponent(_panelTable); break;
            default: throw new IllegalArgumentException("invalid _addressing state: " + selectBoolean.getAddressing().name());
        }
        _valueCheckBox.setSelected(selectBoolean.getValue());
        _referenceTextField.setText(selectBoolean.getReference());
        _memoryPanel.setDefaultNamedBean(selectBoolean.getMemory());
        _listenToMemoryCheckBox.setSelected(selectBoolean.getListenToMemory());
        _localVariableTextField.setText(selectBoolean.getLocalVariable());
        _formulaTextField.setText(selectBoolean.getFormula());

        panel.add(_tabbedPane);
        return panel;
    }

    public boolean validate(
            @Nonnull LogixNG_SelectBoolean selectBoolean,
            @Nonnull List<String> errorMessages) {

        try {
            if (_tabbedPane.getSelectedComponent() == _panelReference) {
                selectBoolean.setReference(_referenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }

        try {
            selectBoolean.setFormula(_formulaTextField.getText());
            if (_tabbedPane.getSelectedComponent() == _panelDirect) {
                selectBoolean.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPane.getSelectedComponent() == _panelReference) {
                selectBoolean.setAddressing(NamedBeanAddressing.Reference);
            } else if (_tabbedPane.getSelectedComponent() == _panelMemory) {
                selectBoolean.setAddressing(NamedBeanAddressing.Memory);
            } else if (_tabbedPane.getSelectedComponent() == _panelLocalVariable) {
                selectBoolean.setAddressing(NamedBeanAddressing.LocalVariable);
            } else if (_tabbedPane.getSelectedComponent() == _panelFormula) {
                selectBoolean.setAddressing(NamedBeanAddressing.Formula);
            } else if (_tabbedPane.getSelectedComponent() == _panelTable) {
                selectBoolean.setAddressing(NamedBeanAddressing.Table);
            } else {
                throw new IllegalArgumentException("_tabbedPane has unknown selection");
            }
        } catch (ParserException e) {
            errorMessages.add("Cannot parse formula: " + e.getMessage());
            return false;
        }

        _selectTableSwing.validate(selectBoolean.getSelectTable(), errorMessages);

        return errorMessages.isEmpty();
    }

    public void updateObject(@Nonnull LogixNG_SelectBoolean selectBoolean) {

        if (_tabbedPane.getSelectedComponent() == _panelDirect) {
            selectBoolean.setValue(_valueCheckBox.isSelected());
        }

        try {
            if (_tabbedPane.getSelectedComponent() == _panelDirect) {
                selectBoolean.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPane.getSelectedComponent() == _panelReference) {
                selectBoolean.setAddressing(NamedBeanAddressing.Reference);
                selectBoolean.setReference(_referenceTextField.getText());
            } else if (_tabbedPane.getSelectedComponent() == _panelMemory) {
                selectBoolean.setAddressing(NamedBeanAddressing.Memory);
                selectBoolean.setMemory(_memoryPanel.getNamedBean());
                selectBoolean.setListenToMemory(_listenToMemoryCheckBox.isSelected());
            } else if (_tabbedPane.getSelectedComponent() == _panelLocalVariable) {
                selectBoolean.setAddressing(NamedBeanAddressing.LocalVariable);
                selectBoolean.setLocalVariable(_localVariableTextField.getText());
            } else if (_tabbedPane.getSelectedComponent() == _panelFormula) {
                selectBoolean.setAddressing(NamedBeanAddressing.Formula);
                selectBoolean.setFormula(_formulaTextField.getText());
            } else if (_tabbedPane.getSelectedComponent() == _panelTable) {
                selectBoolean.setAddressing(NamedBeanAddressing.Table);
            } else {
                throw new IllegalArgumentException("_tabbedPaneEnum has unknown selection");
            }
        } catch (ParserException e) {
            throw new RuntimeException("ParserException: "+e.getMessage(), e);
        }

        _selectTableSwing.updateObject(selectBoolean.getSelectTable());
    }

    public void setEnabled(boolean enabled) {
        _tabbedPane.setEnabled(enabled);
        _valueCheckBox.setEnabled(enabled);
        _referenceTextField.setEnabled(enabled);
        _memoryPanel.getBeanCombo().setEnabled(enabled);
        _listenToMemoryCheckBox.setEnabled(enabled);
        _localVariableTextField.setEnabled(enabled);
        _formulaTextField.setEnabled(enabled);
        _selectTableSwing.setEnabled(enabled);
    }

    public void dispose() {
        _selectTableSwing.dispose();
    }

}

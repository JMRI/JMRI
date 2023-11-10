package jmri.jmrit.logixng.util.swing;

import java.util.List;

import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.LogixNG_SelectDouble;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.util.swing.BeanSelectPanel;

/**
 * Swing class for jmri.jmrit.logixng.util.LogixNG_SelectDouble.
 *
 * @author Daniel Bergqvist (C) 2022
 */
public class LogixNG_SelectDoubleSwing {

    private final JDialog _dialog;
    private final LogixNG_SelectTableSwing _selectTableSwing;

    private JTabbedPane _tabbedPane;
    private JTextField _valueTextField;
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


    public LogixNG_SelectDoubleSwing(
            @Nonnull JDialog dialog,
            @Nonnull SwingConfiguratorInterface swi) {
        _dialog = dialog;
        _selectTableSwing = new LogixNG_SelectTableSwing(_dialog, swi);
    }

    public JPanel createPanel(@Nonnull LogixNG_SelectDouble selectDouble) {

        LogixNG_SelectDouble.FormatterParserValidator _formatterParserValidator
                = selectDouble.getFormatterParserValidator();

        JPanel panel = new JPanel();

        _tabbedPane = new JTabbedPane();
        _panelDirect = new javax.swing.JPanel();
        _panelReference = new javax.swing.JPanel();
        _panelMemory = new JPanel();
        _panelLocalVariable = new javax.swing.JPanel();
        _panelFormula = new javax.swing.JPanel();
        _panelTable = _selectTableSwing.createPanel(selectDouble.getSelectTable());

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

        _valueTextField = new JTextField(30);
        _valueTextField.setText(_formatterParserValidator.format(
                _formatterParserValidator.getInitialValue()));
        _panelDirect.add(_valueTextField);

        _referenceTextField = new JTextField();
        _referenceTextField.setColumns(30);
        _panelReference.add(_referenceTextField);

        _localVariableTextField = new JTextField();
        _localVariableTextField.setColumns(30);
        _panelLocalVariable.add(_localVariableTextField);

        _formulaTextField = new JTextField();
        _formulaTextField.setColumns(30);
        _panelFormula.add(_formulaTextField);


        switch (selectDouble.getAddressing()) {
            case Direct: _tabbedPane.setSelectedComponent(_panelDirect); break;
            case Reference: _tabbedPane.setSelectedComponent(_panelReference); break;
            case Memory: _tabbedPane.setSelectedComponent(_panelMemory); break;
            case LocalVariable: _tabbedPane.setSelectedComponent(_panelLocalVariable); break;
            case Formula: _tabbedPane.setSelectedComponent(_panelFormula); break;
            case Table: _tabbedPane.setSelectedComponent(_panelTable); break;
            default: throw new IllegalArgumentException("invalid _addressing state: " + selectDouble.getAddressing().name());
        }
        _valueTextField.setText(_formatterParserValidator.format(selectDouble.getValue()));
        _referenceTextField.setText(selectDouble.getReference());
        _memoryPanel.setDefaultNamedBean(selectDouble.getMemory());
        _listenToMemoryCheckBox.setSelected(selectDouble.getListenToMemory());
        _localVariableTextField.setText(selectDouble.getLocalVariable());
        _formulaTextField.setText(selectDouble.getFormula());

        panel.add(_tabbedPane);
        return panel;
    }

    public boolean validate(
            @Nonnull LogixNG_SelectDouble selectDouble,
            @Nonnull List<String> errorMessages) {

        if (_tabbedPane.getSelectedComponent() == _panelDirect) {
            String result = selectDouble.getFormatterParserValidator()
                    .validate(_valueTextField.getText());
            if (result != null) errorMessages.add(result);
        }

        try {
            if (_tabbedPane.getSelectedComponent() == _panelReference) {
                selectDouble.setReference(_referenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }

        try {
            selectDouble.setFormula(_formulaTextField.getText());
            if (_tabbedPane.getSelectedComponent() == _panelDirect) {
                selectDouble.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPane.getSelectedComponent() == _panelReference) {
                selectDouble.setAddressing(NamedBeanAddressing.Reference);
            } else if (_tabbedPane.getSelectedComponent() == _panelMemory) {
                selectDouble.setAddressing(NamedBeanAddressing.Memory);
            } else if (_tabbedPane.getSelectedComponent() == _panelLocalVariable) {
                selectDouble.setAddressing(NamedBeanAddressing.LocalVariable);
            } else if (_tabbedPane.getSelectedComponent() == _panelFormula) {
                selectDouble.setAddressing(NamedBeanAddressing.Formula);
            } else if (_tabbedPane.getSelectedComponent() == _panelTable) {
                selectDouble.setAddressing(NamedBeanAddressing.Table);
            } else {
                throw new IllegalArgumentException("_tabbedPane has unknown selection");
            }
        } catch (ParserException e) {
            errorMessages.add("Cannot parse formula: " + e.getMessage());
            return false;
        }

        _selectTableSwing.validate(selectDouble.getSelectTable(), errorMessages);

        return errorMessages.isEmpty();
    }

    public void updateObject(@Nonnull LogixNG_SelectDouble selectDouble) {

        if (_tabbedPane.getSelectedComponent() == _panelDirect) {
            selectDouble.setValue(selectDouble.getFormatterParserValidator()
                    .parse(_valueTextField.getText()));
        }

        try {
            if (_tabbedPane.getSelectedComponent() == _panelDirect) {
                selectDouble.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPane.getSelectedComponent() == _panelReference) {
                selectDouble.setAddressing(NamedBeanAddressing.Reference);
                selectDouble.setReference(_referenceTextField.getText());
            } else if (_tabbedPane.getSelectedComponent() == _panelMemory) {
                selectDouble.setAddressing(NamedBeanAddressing.Memory);
                selectDouble.setMemory(_memoryPanel.getNamedBean());
                selectDouble.setListenToMemory(_listenToMemoryCheckBox.isSelected());
            } else if (_tabbedPane.getSelectedComponent() == _panelLocalVariable) {
                selectDouble.setAddressing(NamedBeanAddressing.LocalVariable);
                selectDouble.setLocalVariable(_localVariableTextField.getText());
            } else if (_tabbedPane.getSelectedComponent() == _panelFormula) {
                selectDouble.setAddressing(NamedBeanAddressing.Formula);
                selectDouble.setFormula(_formulaTextField.getText());
            } else if (_tabbedPane.getSelectedComponent() == _panelTable) {
                selectDouble.setAddressing(NamedBeanAddressing.Table);
            } else {
                throw new IllegalArgumentException("_tabbedPaneEnum has unknown selection");
            }
        } catch (ParserException e) {
            throw new RuntimeException("ParserException: "+e.getMessage(), e);
        }

        _selectTableSwing.updateObject(selectDouble.getSelectTable());
    }

    public void dispose() {
        _selectTableSwing.dispose();
    }

}

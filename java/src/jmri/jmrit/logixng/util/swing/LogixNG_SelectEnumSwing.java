package jmri.jmrit.logixng.util.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.LogixNG_SelectEnum;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.util.swing.JComboBoxUtil;

/**
 * Swing class for jmri.jmrit.logixng.util.LogixNG_SelectEnum.
 *
 * @param <E> the type of enum
 *
 * @author Daniel Bergqvist (C) 2022
 */
public class LogixNG_SelectEnumSwing<E extends Enum<?>> {

    private final JDialog _dialog;
    private final LogixNG_SelectTableSwing _selectTableSwing;

    private JTabbedPane _tabbedPane;
    private JComboBox<E> _enumComboBox;
    private JPanel _panelDirect;
    private JPanel _panelReference;
    private JPanel _panelLocalVariable;
    private JPanel _panelFormula;
    private JPanel _panelTable;
    private JTextField _referenceTextField;
    private JTextField _localVariableTextField;
    private JTextField _formulaTextField;


    public LogixNG_SelectEnumSwing(
            @Nonnull JDialog dialog,
            @Nonnull SwingConfiguratorInterface swi) {
        _dialog = dialog;
        _selectTableSwing = new LogixNG_SelectTableSwing(_dialog, swi);
    }

    public JPanel createPanel(
            @CheckForNull LogixNG_SelectEnum<E> selectEnum, E[] enumArray) {

        JPanel panel = new JPanel();

        _tabbedPane = new JTabbedPane();
        _panelDirect = new javax.swing.JPanel();
        _panelReference = new javax.swing.JPanel();
        _panelLocalVariable = new javax.swing.JPanel();
        _panelFormula = new javax.swing.JPanel();
        if (selectEnum != null) {
            _panelTable = _selectTableSwing.createPanel(selectEnum.getSelectTable());
        } else {
            _panelTable = _selectTableSwing.createPanel(null);
        }

        _tabbedPane.addTab(NamedBeanAddressing.Direct.toString(), _panelDirect);
        _tabbedPane.addTab(NamedBeanAddressing.Reference.toString(), _panelReference);
        _tabbedPane.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelLocalVariable);
        _tabbedPane.addTab(NamedBeanAddressing.Formula.toString(), _panelFormula);
        _tabbedPane.addTab(NamedBeanAddressing.Table.toString(), _panelTable);

        _enumComboBox = new JComboBox<>();
        for (E e : enumArray) {
            _enumComboBox.addItem(e);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_enumComboBox);
        _panelDirect.add(_enumComboBox);

        _referenceTextField = new JTextField();
        _referenceTextField.setColumns(30);
        _panelReference.add(_referenceTextField);

        _localVariableTextField = new JTextField();
        _localVariableTextField.setColumns(30);
        _panelLocalVariable.add(_localVariableTextField);

        _formulaTextField = new JTextField();
        _formulaTextField.setColumns(30);
        _panelFormula.add(_formulaTextField);


        if (selectEnum != null) {
            switch (selectEnum.getAddressing()) {
                case Direct: _tabbedPane.setSelectedComponent(_panelDirect); break;
                case Reference: _tabbedPane.setSelectedComponent(_panelReference); break;
                case LocalVariable: _tabbedPane.setSelectedComponent(_panelLocalVariable); break;
                case Formula: _tabbedPane.setSelectedComponent(_panelFormula); break;
                case Table: _tabbedPane.setSelectedComponent(_panelTable); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + selectEnum.getAddressing().name());
            }
            if (selectEnum.getEnum() != null) {
                _enumComboBox.setSelectedItem(selectEnum.getEnum());
            }
            _referenceTextField.setText(selectEnum.getReference());
            _localVariableTextField.setText(selectEnum.getLocalVariable());
            _formulaTextField.setText(selectEnum.getFormula());
        }

        panel.add(_tabbedPane);
        return panel;
    }

    public boolean validate(
            @Nonnull LogixNG_SelectEnum<E> selectEnum,
            @Nonnull List<String> errorMessages) {
        try {
            if (_tabbedPane.getSelectedComponent() == _panelReference) {
                selectEnum.setReference(_referenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }

        try {
            selectEnum.setFormula(_formulaTextField.getText());
            if (_tabbedPane.getSelectedComponent() == _panelDirect) {
                selectEnum.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPane.getSelectedComponent() == _panelReference) {
                selectEnum.setAddressing(NamedBeanAddressing.Reference);
            } else if (_tabbedPane.getSelectedComponent() == _panelLocalVariable) {
                selectEnum.setAddressing(NamedBeanAddressing.LocalVariable);
            } else if (_tabbedPane.getSelectedComponent() == _panelFormula) {
                selectEnum.setAddressing(NamedBeanAddressing.Formula);
            } else if (_tabbedPane.getSelectedComponent() == _panelTable) {
                selectEnum.setAddressing(NamedBeanAddressing.Table);
            } else {
                throw new IllegalArgumentException("_tabbedPane has unknown selection");
            }
        } catch (ParserException e) {
            errorMessages.add("Cannot parse formula: " + e.getMessage());
            return false;
        }

        _selectTableSwing.validate(selectEnum.getSelectTable(), errorMessages);

        return errorMessages.isEmpty();
    }

    public void updateObject(@Nonnull LogixNG_SelectEnum<E> selectEnum) {

        if (_tabbedPane.getSelectedComponent() == _panelDirect) {
            selectEnum.setEnum(_enumComboBox.getItemAt(_enumComboBox.getSelectedIndex()));
        }

        try {
            if (_tabbedPane.getSelectedComponent() == _panelDirect) {
                selectEnum.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPane.getSelectedComponent() == _panelReference) {
                selectEnum.setAddressing(NamedBeanAddressing.Reference);
                selectEnum.setReference(_referenceTextField.getText());
            } else if (_tabbedPane.getSelectedComponent() == _panelLocalVariable) {
                selectEnum.setAddressing(NamedBeanAddressing.LocalVariable);
                selectEnum.setLocalVariable(_localVariableTextField.getText());
            } else if (_tabbedPane.getSelectedComponent() == _panelFormula) {
                selectEnum.setAddressing(NamedBeanAddressing.Formula);
                selectEnum.setFormula(_formulaTextField.getText());
            } else if (_tabbedPane.getSelectedComponent() == _panelTable) {
                selectEnum.setAddressing(NamedBeanAddressing.Table);
            } else {
                throw new IllegalArgumentException("_tabbedPaneEnum has unknown selection");
            }
        } catch (ParserException e) {
            throw new RuntimeException("ParserException: "+e.getMessage(), e);
        }

        _selectTableSwing.updateObject(selectEnum.getSelectTable());
    }

    public void dispose() {
        _selectTableSwing.dispose();
    }

}

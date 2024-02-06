package jmri.jmrit.logixng.util.swing;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.util.List;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.event.ChangeListener;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.LogixNG_SelectComboBox;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.util.swing.BeanSelectPanel;
import jmri.util.swing.JComboBoxUtil;

/**
 * Swing class for jmri.jmrit.logixng.util.LogixNG_SelectComboBox.
 *
 * @author Daniel Bergqvist (C) 2024
 */
public class LogixNG_SelectComboBoxSwing {

    private final JDialog _dialog;
    private final LogixNG_SelectTableSwing _selectTableSwing;

    private JTabbedPane _tabbedPane;
    private JComboBox<String> _valuesComboBox;
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


    public LogixNG_SelectComboBoxSwing(
            @Nonnull JDialog dialog,
            @Nonnull SwingConfiguratorInterface swi) {
        _dialog = dialog;
        _selectTableSwing = new LogixNG_SelectTableSwing(_dialog, swi);
    }

    public JPanel createPanel(
            @Nonnull LogixNG_SelectComboBox selectComboBox) {
        return createPanel(selectComboBox, null);
    }

    public JPanel createPanel(
            @Nonnull LogixNG_SelectComboBox selectComboBox, String defaultValue) {

        JPanel panel = new JPanel();

        _tabbedPane = new JTabbedPane();
        _panelDirect = new javax.swing.JPanel();
        _panelReference = new javax.swing.JPanel();
        _panelMemory = new JPanel();
        _panelLocalVariable = new javax.swing.JPanel();
        _panelFormula = new javax.swing.JPanel();
        _panelTable = _selectTableSwing.createPanel(selectComboBox.getSelectTable());

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

        _valuesComboBox = new JComboBox<>();
        for (String value : selectComboBox.getValues()) {
            _valuesComboBox.addItem(value);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_valuesComboBox);
        _valuesComboBox.setRenderer(new ComboBoxRenderer<>(_valuesComboBox.getRenderer()));
        _panelDirect.add(_valuesComboBox);

        _referenceTextField = new JTextField();
        _referenceTextField.setColumns(30);
        _panelReference.add(_referenceTextField);

        _localVariableTextField = new JTextField();
        _localVariableTextField.setColumns(30);
        _panelLocalVariable.add(_localVariableTextField);

        _formulaTextField = new JTextField();
        _formulaTextField.setColumns(30);
        _panelFormula.add(_formulaTextField);


        if (defaultValue != null) {
            _valuesComboBox.setSelectedItem(defaultValue);
        }

        switch (selectComboBox.getAddressing()) {
            case Direct: _tabbedPane.setSelectedComponent(_panelDirect); break;
            case Reference: _tabbedPane.setSelectedComponent(_panelReference); break;
            case Memory: _tabbedPane.setSelectedComponent(_panelMemory); break;
            case LocalVariable: _tabbedPane.setSelectedComponent(_panelLocalVariable); break;
            case Formula: _tabbedPane.setSelectedComponent(_panelFormula); break;
            case Table: _tabbedPane.setSelectedComponent(_panelTable); break;
            default: throw new IllegalArgumentException("invalid _addressing state: " + selectComboBox.getAddressing().name());
        }
        if (selectComboBox.getValue() != null) {
            _valuesComboBox.setSelectedItem(selectComboBox.getValue());
        }
        _referenceTextField.setText(selectComboBox.getReference());
        _memoryPanel.setDefaultNamedBean(selectComboBox.getMemory());
        _listenToMemoryCheckBox.setSelected(selectComboBox.getListenToMemory());
        _localVariableTextField.setText(selectComboBox.getLocalVariable());
        _formulaTextField.setText(selectComboBox.getFormula());

        panel.add(_tabbedPane);
        return panel;
    }

    public void addAddressingListener(ChangeListener listener) {
        _tabbedPane.addChangeListener(listener);
    }

    public void addEnumListener(ActionListener listener) {
        _valuesComboBox.addActionListener(listener);
    }

    public boolean validate(
            @Nonnull LogixNG_SelectComboBox selectComboBox,
            @Nonnull List<String> errorMessages) {
        try {
            if (_tabbedPane.getSelectedComponent() == _panelReference) {
                selectComboBox.setReference(_referenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }

        try {
            selectComboBox.setFormula(_formulaTextField.getText());
            selectComboBox.setAddressing(getAddressing());
        } catch (ParserException e) {
            errorMessages.add("Cannot parse formula: " + e.getMessage());
            return false;
        }

        _selectTableSwing.validate(selectComboBox.getSelectTable(), errorMessages);

        return errorMessages.isEmpty();
    }

    public void updateObject(@Nonnull LogixNG_SelectComboBox selectComboBox) {

        if (_tabbedPane.getSelectedComponent() == _panelDirect) {
            selectComboBox.setValue(_valuesComboBox.getItemAt(_valuesComboBox.getSelectedIndex()));
        }

        try {
            if (_tabbedPane.getSelectedComponent() == _panelDirect) {
                selectComboBox.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPane.getSelectedComponent() == _panelReference) {
                selectComboBox.setAddressing(NamedBeanAddressing.Reference);
                selectComboBox.setReference(_referenceTextField.getText());
            } else if (_tabbedPane.getSelectedComponent() == _panelMemory) {
                selectComboBox.setAddressing(NamedBeanAddressing.Memory);
                selectComboBox.setMemory(_memoryPanel.getNamedBean());
                selectComboBox.setListenToMemory(_listenToMemoryCheckBox.isSelected());
            } else if (_tabbedPane.getSelectedComponent() == _panelLocalVariable) {
                selectComboBox.setAddressing(NamedBeanAddressing.LocalVariable);
                selectComboBox.setLocalVariable(_localVariableTextField.getText());
            } else if (_tabbedPane.getSelectedComponent() == _panelFormula) {
                selectComboBox.setAddressing(NamedBeanAddressing.Formula);
                selectComboBox.setFormula(_formulaTextField.getText());
            } else if (_tabbedPane.getSelectedComponent() == _panelTable) {
                selectComboBox.setAddressing(NamedBeanAddressing.Table);
            } else {
                throw new IllegalArgumentException("_tabbedPaneEnum has unknown selection");
            }
        } catch (ParserException e) {
            throw new RuntimeException("ParserException: "+e.getMessage(), e);
        }

        _selectTableSwing.updateObject(selectComboBox.getSelectTable());
    }

    public boolean isValueSelected(String value) {
        if (_tabbedPane.getSelectedComponent() == _panelDirect) {
            return _valuesComboBox.getItemAt(_valuesComboBox.getSelectedIndex()).equals(value);
        } else {
            return false;
        }
    }

    public boolean isValueSelectedOrIndirectAddressing(String value) {
        if (_tabbedPane.getSelectedComponent() == _panelDirect) {
            return _valuesComboBox.getItemAt(_valuesComboBox.getSelectedIndex()).equals(value);
        } else {
            return true;
        }
    }

    public NamedBeanAddressing getAddressing() {
        if (_tabbedPane.getSelectedComponent() == _panelDirect) {
            return NamedBeanAddressing.Direct;
        } else if (_tabbedPane.getSelectedComponent() == _panelReference) {
            return NamedBeanAddressing.Reference;
        } else if (_tabbedPane.getSelectedComponent() == _panelMemory) {
            return NamedBeanAddressing.Memory;
        } else if (_tabbedPane.getSelectedComponent() == _panelLocalVariable) {
            return NamedBeanAddressing.LocalVariable;
        } else if (_tabbedPane.getSelectedComponent() == _panelFormula) {
            return NamedBeanAddressing.Formula;
        } else if (_tabbedPane.getSelectedComponent() == _panelTable) {
            return NamedBeanAddressing.Table;
        } else {
            throw new IllegalArgumentException("_tabbedPane has unknown selection");
        }
    }

    public String getValue() {
        return _valuesComboBox.getItemAt(_valuesComboBox.getSelectedIndex());
    }

    public void setValue(String value) {
        _valuesComboBox.setSelectedItem(value);
    }

    public void setValues(String[] valuesArray) {
        String selectedValue = _valuesComboBox.getItemAt(_valuesComboBox.getSelectedIndex());

        _valuesComboBox.removeAllItems();
        for (String value : valuesArray) {
            _valuesComboBox.addItem(value);
        }

        _valuesComboBox.setSelectedItem(selectedValue);
    }

    public void dispose() {
        _selectTableSwing.dispose();
    }


    private static class ComboBoxRenderer<E> extends JLabel implements ListCellRenderer<E> {

        private final JSeparator _separator = new JSeparator(JSeparator.HORIZONTAL);
        private final ListCellRenderer<E> _old;

        private ComboBoxRenderer(ListCellRenderer<E> old) {
            this._old = old;
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends E> list,
                E value, int index, boolean isSelected, boolean cellHasFocus) {
            if (Base.SEPARATOR.equals(value)) {
                return _separator;
            } else {
                return _old.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        }
    }

}

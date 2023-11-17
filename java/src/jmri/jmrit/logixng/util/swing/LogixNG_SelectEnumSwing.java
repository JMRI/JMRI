package jmri.jmrit.logixng.util.swing;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.event.ChangeListener;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.LogixNG_SelectEnum;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.util.swing.BeanSelectPanel;
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
    private JPanel _panelMemory;
    private JPanel _panelLocalVariable;
    private JPanel _panelFormula;
    private JPanel _panelTable;
    private JTextField _referenceTextField;
    private BeanSelectPanel<Memory> _memoryPanel;
    private JCheckBox _listenToMemoryCheckBox;
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
        return createPanel(selectEnum, enumArray, null);
    }

    public JPanel createPanel(
            @CheckForNull LogixNG_SelectEnum<E> selectEnum, E[] enumArray, E defaultValue) {

        JPanel panel = new JPanel();

        _tabbedPane = new JTabbedPane();
        _panelDirect = new javax.swing.JPanel();
        _panelReference = new javax.swing.JPanel();
        _panelMemory = new JPanel();
        _panelLocalVariable = new javax.swing.JPanel();
        _panelFormula = new javax.swing.JPanel();
        if (selectEnum != null) {
            _panelTable = _selectTableSwing.createPanel(selectEnum.getSelectTable());
        } else {
            _panelTable = _selectTableSwing.createPanel(null);
        }

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

        _enumComboBox = new JComboBox<>();
        for (E e : enumArray) {
            _enumComboBox.addItem(e);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_enumComboBox);
        _enumComboBox.setRenderer(new ComboBoxRenderer<>(_enumComboBox.getRenderer()));
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


        if (defaultValue != null) {
            _enumComboBox.setSelectedItem(defaultValue);
        }

        if (selectEnum != null) {
            switch (selectEnum.getAddressing()) {
                case Direct: _tabbedPane.setSelectedComponent(_panelDirect); break;
                case Reference: _tabbedPane.setSelectedComponent(_panelReference); break;
                case Memory: _tabbedPane.setSelectedComponent(_panelMemory); break;
                case LocalVariable: _tabbedPane.setSelectedComponent(_panelLocalVariable); break;
                case Formula: _tabbedPane.setSelectedComponent(_panelFormula); break;
                case Table: _tabbedPane.setSelectedComponent(_panelTable); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + selectEnum.getAddressing().name());
            }
            if (selectEnum.getEnum() != null) {
                _enumComboBox.setSelectedItem(selectEnum.getEnum());
            }
            _referenceTextField.setText(selectEnum.getReference());
            _memoryPanel.setDefaultNamedBean(selectEnum.getMemory());
            _listenToMemoryCheckBox.setSelected(selectEnum.getListenToMemory());
            _localVariableTextField.setText(selectEnum.getLocalVariable());
            _formulaTextField.setText(selectEnum.getFormula());
        }

        panel.add(_tabbedPane);
        return panel;
    }

    public void addAddressingListener(ChangeListener listener) {
        _tabbedPane.addChangeListener(listener);
    }

    public void addEnumListener(ActionListener listener) {
        _enumComboBox.addActionListener(listener);
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
            selectEnum.setAddressing(getAddressing());
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
            } else if (_tabbedPane.getSelectedComponent() == _panelMemory) {
                selectEnum.setAddressing(NamedBeanAddressing.Memory);
                selectEnum.setMemory(_memoryPanel.getNamedBean());
                selectEnum.setListenToMemory(_listenToMemoryCheckBox.isSelected());
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

    public boolean isEnumSelected(E e) {
        if (_tabbedPane.getSelectedComponent() == _panelDirect) {
            return _enumComboBox.getItemAt(_enumComboBox.getSelectedIndex()) == e;
        } else {
            return false;
        }
    }

    public boolean isEnumSelectedOrIndirectAddressing(E e) {
        if (_tabbedPane.getSelectedComponent() == _panelDirect) {
            return _enumComboBox.getItemAt(_enumComboBox.getSelectedIndex()) == e;
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

    public E getEnum() {
        return _enumComboBox.getItemAt(_enumComboBox.getSelectedIndex());
    }

    public void setEnum(E e) {
        _enumComboBox.setSelectedItem(e);
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
            if (Base.SEPARATOR.equals(value.toString())) {
                return _separator;
            } else {
                return _old.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        }
    }

}

package jmri.jmrit.logixng.util.swing;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.table.*;

import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.LogixNG_SelectStringList;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.util.swing.JComboBoxUtil;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

/**
 * Swing class for jmri.jmrit.logixng.util.LogixNG_SelectStringList.
 *
 * @author Daniel Bergqvist (C) 2025
 */
public class LogixNG_SelectStringListSwing {

    private JTabbedPane _tabbedPane;
    private JTable _dataTable;
    private StringListTableModel _dataTableModel;
    private JPanel _panelDirect;
    private JPanel _panelLocalVariable;
    private JPanel _panelFormula;
    private JTextField _localVariableTextField;
    private JTextField _formulaTextField;
    private JButton _addButton;


    public JPanel createPanel(@CheckForNull LogixNG_SelectStringList selectStrList) {

        JPanel panel = new JPanel();

        _tabbedPane = new JTabbedPane();
        _panelDirect = new javax.swing.JPanel();
        _panelLocalVariable = new javax.swing.JPanel();
        _panelFormula = new javax.swing.JPanel();
        if (selectStrList != null) {
            if (selectStrList.isOnlyDirectAddressingAllowed()) {
                _tabbedPane.setEnabled(false);
                _panelLocalVariable.setEnabled(false);
                _panelFormula.setEnabled(false);
            }
        }

        _tabbedPane.addTab(NamedBeanAddressing.Direct.toString(), _panelDirect);
        _tabbedPane.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelLocalVariable);
        _tabbedPane.addTab(NamedBeanAddressing.Formula.toString(), _panelFormula);

        _dataTable = new JTable() {
            //workaround for  https://bugs.openjdk.org/browse/JDK-4127936
            @Override
            public boolean getScrollableTracksViewportWidth() {
                return getRowCount() == 0 ? super.getScrollableTracksViewportWidth()
                        : getPreferredSize().width < getParent().getWidth();
            }
        };

        if (selectStrList != null) {
            _dataTableModel = new StringListTableModel(selectStrList.getList());
        } else {
            _dataTableModel = new StringListTableModel(null);
        }
        _dataTable.setModel(_dataTableModel);
        _dataTable.getColumnModel().getColumn(0).setPreferredWidth(200);
        _dataTable.getColumnModel().getColumn(1).setPreferredWidth(
                new JButton(Bundle.getMessage("ButtonMoveUp")).getPreferredSize().width);
        _dataTable.getColumnModel().getColumn(2).setPreferredWidth(
                new JButton(Bundle.getMessage("ButtonMoveDown")).getPreferredSize().width);
        _dataTable.getColumnModel().getColumn(3).setPreferredWidth(
                new JButton(Bundle.getMessage("ButtonDelete")).getPreferredSize().width);
        ButtonRenderer buttonRenderer = new ButtonRenderer();
        _dataTable.setDefaultRenderer(JButton.class, buttonRenderer);
        TableCellEditor buttonEditor = new ButtonEditor(new JButton());
        _dataTable.setDefaultEditor(JButton.class, buttonEditor);
        JScrollPane scrollPane = new JScrollPane(_dataTable);
        scrollPane.setPreferredSize(new java.awt.Dimension(600,200));
        JPanel tablePanel = new JPanel();
        tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS));
        tablePanel.add(scrollPane);
        _addButton = new JButton(Bundle.getMessage("ButtonAdd"));
        _addButton.addActionListener(_dataTableModel::add);
        tablePanel.add(_addButton);
        _panelDirect.add(tablePanel);

        _localVariableTextField = new JTextField();
        _localVariableTextField.setColumns(30);
        _panelLocalVariable.add(_localVariableTextField);

        _formulaTextField = new JTextField();
        _formulaTextField.setColumns(30);
        _panelFormula.add(_formulaTextField);


        if (selectStrList != null) {
            switch (selectStrList.getAddressing()) {
                case Direct: _tabbedPane.setSelectedComponent(_panelDirect); break;
                case LocalVariable: _tabbedPane.setSelectedComponent(_panelLocalVariable); break;
                case Formula: _tabbedPane.setSelectedComponent(_panelFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + selectStrList.getAddressing().name());
            }
//DANIEL            if (selectStr.getValue() != null) {
//DANIEL                _valueTextField.setText(selectStr.getValue());
//DANIEL            }
            _localVariableTextField.setText(selectStrList.getLocalVariable());
            _formulaTextField.setText(selectStrList.getFormula());
        }

        panel.add(_tabbedPane);
        return panel;
    }

    public boolean validate(
            @Nonnull LogixNG_SelectStringList selectStrList,
            @Nonnull List<String> errorMessages) {
        try {
            if (_tabbedPane.getSelectedComponent() == _panelDirect) {
//DANIEL                selectStr.setValue(_valueTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }

        try {
            selectStrList.setFormula(_formulaTextField.getText());
            if (_tabbedPane.getSelectedComponent() == _panelDirect) {
                selectStrList.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPane.getSelectedComponent() == _panelLocalVariable) {
                selectStrList.setAddressing(NamedBeanAddressing.LocalVariable);
            } else if (_tabbedPane.getSelectedComponent() == _panelFormula) {
                selectStrList.setAddressing(NamedBeanAddressing.Formula);
            } else {
                throw new IllegalArgumentException("_tabbedPane has unknown selection");
            }
        } catch (ParserException e) {
            errorMessages.add("Cannot parse formula: " + e.getMessage());
            return false;
        }

        return errorMessages.isEmpty();
    }

    public void setEnabled(boolean value) {
        _tabbedPane.setEnabled(value);
        _dataTable.setEnabled(value);
        _localVariableTextField.setEnabled(value);
        _formulaTextField.setEnabled(value);
    }

    public void updateObject(@Nonnull LogixNG_SelectStringList selectStrList) {

        if (_tabbedPane.getSelectedComponent() == _panelDirect) {
            List<String> list = selectStrList.getList();
            list.clear();
            list.addAll(_dataTableModel.getData());
        }

        try {
            if (_tabbedPane.getSelectedComponent() == _panelDirect) {
                selectStrList.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPane.getSelectedComponent() == _panelLocalVariable) {
                selectStrList.setAddressing(NamedBeanAddressing.LocalVariable);
                selectStrList.setLocalVariable(_localVariableTextField.getText());
            } else if (_tabbedPane.getSelectedComponent() == _panelFormula) {
                selectStrList.setAddressing(NamedBeanAddressing.Formula);
                selectStrList.setFormula(_formulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneEnum has unknown selection");
            }
        } catch (ParserException e) {
            throw new RuntimeException("ParserException: "+e.getMessage(), e);
        }
    }

    public void dispose() {
    }




    private static class StringListTableModel extends AbstractTableModel {

        public static final int COLUMN_DATA = 0;
        public static final int COLUMN_MOVE_UP = 1;
        public static final int COLUMN_MOVE_DOWN = 2;
        public static final int COLUMN_REMOVE = 3;

        private final List<String> _data = new ArrayList<>();


        public StringListTableModel(List<String> data) {
            if (data != null) {
                this._data.addAll(data);
            }
        }

        /** {@inheritDoc} */
        @Override
        public int getRowCount() {
            return _data.size();
        }

        /** {@inheritDoc} */
        @Override
        public int getColumnCount() {
            return 4;
        }

        /** {@inheritDoc} */
        @Override
        public String getColumnName(int col) {
            switch (col) {
                case COLUMN_DATA:
                    return Bundle.getMessage("LogixNG_SelectStringListSwing_ColumnVariableData");
                case COLUMN_MOVE_UP:
                    return Bundle.getMessage("LogixNG_SelectStringListSwing_ColumnVariableMoveUp");
                case COLUMN_MOVE_DOWN:
                    return Bundle.getMessage("LogixNG_SelectStringListSwing_ColumnVariableMoveDown");
                case COLUMN_REMOVE:
                    return Bundle.getMessage("LogixNG_SelectStringListSwing_ColumnVariableRemove");
                default:
                    throw new IllegalArgumentException("Invalid column");
            }
        }

        /** {@inheritDoc} */
        @Override
        public Class<?> getColumnClass(int col) {
            switch (col) {
                case COLUMN_DATA:
                    return String.class;
                case COLUMN_MOVE_UP:
                case COLUMN_MOVE_DOWN:
                case COLUMN_REMOVE:
                    return JButton.class;
                default:
                    throw new IllegalArgumentException("Invalid column");
            }
        }

        /** {@inheritDoc} */
        @Override
        public boolean isCellEditable(int row, int col) {
            return true;
        }

        /** {@inheritDoc} */
        @Override
        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case COLUMN_DATA:
                    if (value != null) {
                        _data.set(rowIndex, value.toString());
                    } else {
                        _data.set(rowIndex, "");
                    }
                    break;
                case COLUMN_MOVE_UP:
                    if (rowIndex > 0) {
                        String s = _data.get(rowIndex-1);
                        _data.set(rowIndex-1, _data.get(rowIndex));
                        _data.set(rowIndex, s);
                        fireTableRowsUpdated(rowIndex-1, rowIndex);
                    }
                    break;
                case COLUMN_MOVE_DOWN:
                    if (rowIndex < _data.size()-1) {
                        String s = _data.get(rowIndex+1);
                        _data.set(rowIndex+1, _data.get(rowIndex));
                        _data.set(rowIndex, s);
                        fireTableRowsUpdated(rowIndex, rowIndex+1);
                    }
                    break;
                case COLUMN_REMOVE:
                    _data.remove(rowIndex);
                    fireTableRowsDeleted(rowIndex, rowIndex);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid column");
            }
        }

        /** {@inheritDoc} */
        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (rowIndex >= _data.size()) throw new IllegalArgumentException("Invalid row");

            switch (columnIndex) {
                case COLUMN_DATA:
                    return _data.get(rowIndex);
                case COLUMN_MOVE_UP:
                    return Bundle.getMessage("ButtonMoveUp");
                case COLUMN_MOVE_DOWN:
                    return Bundle.getMessage("ButtonMoveDown");
                case COLUMN_REMOVE:
                    return Bundle.getMessage("ButtonDelete");
                default:
                    throw new IllegalArgumentException("Invalid column");
            }
        }
/*
        public void setColumnForMenu(JTable table) {
            JComboBox<Menu> comboBox = new JComboBox<>();
            table.setRowHeight(comboBox.getPreferredSize().height);
            table.getColumnModel().getColumn(COLUMN_REMOVE)
                    .setPreferredWidth((comboBox.getPreferredSize().width) + 4);
        }
*/
        public void add(ActionEvent evt) {
            int row = _data.size();
            _data.add("");
            fireTableRowsInserted(row, row);
        }

        public List<String> getData() {
            return _data;
        }


        public static class TypeCellRenderer extends DefaultTableCellRenderer {

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {

                if (value == null) value = SymbolTable.InitialValueType.None;

                if (! (value instanceof SymbolTable.InitialValueType)) {
                    throw new IllegalArgumentException("value is not an InitialValueType: " + value.getClass().getName());
                }
                setText(((SymbolTable.InitialValueType) value).toString());
                return this;
            }
        }


        public static class TypeCellEditor extends AbstractCellEditor
                implements TableCellEditor, ActionListener {

            private SymbolTable.InitialValueType _type;

            @Override
            public Object getCellEditorValue() {
                return this._type;
            }

            @Override
            public Component getTableCellEditorComponent(JTable table, Object value,
                    boolean isSelected, int row, int column) {

                if (value == null) value = SymbolTable.InitialValueType.None;

                if (! (value instanceof SymbolTable.InitialValueType)) {
                    throw new IllegalArgumentException("value is not an InitialValueType: " + value.getClass().getName());
                }

                JComboBox<SymbolTable.InitialValueType> typeComboBox = new JComboBox<>();

                for (SymbolTable.InitialValueType type : SymbolTable.InitialValueType.values()) {
                    typeComboBox.addItem(type);
                }
                JComboBoxUtil.setupComboBoxMaxRows(typeComboBox);

                typeComboBox.setSelectedItem(value);
                typeComboBox.addActionListener(this);

                return typeComboBox;
            }

            @Override
            @SuppressWarnings("unchecked")  // Not possible to check that event.getSource() is instanceof JComboBox<InitialValueType>
            public void actionPerformed(ActionEvent event) {
                if (! (event.getSource() instanceof JComboBox)) {
                    throw new IllegalArgumentException("value is not an InitialValueType: " + event.getSource().getClass().getName());
                }
                JComboBox<SymbolTable.InitialValueType> typeComboBox =
                        (JComboBox<SymbolTable.InitialValueType>) event.getSource();
                _type = typeComboBox.getItemAt(typeComboBox.getSelectedIndex());
            }

        }

/*
        public static class MenuCellEditor extends AbstractCellEditor
                implements TableCellEditor, ActionListener {

            JTable _table;
            StringListTableModel _tableModel;

            public MenuCellEditor(JTable table, StringListTableModel tableModel) {
                _table = table;
                _tableModel = tableModel;
            }

            @Override
            public Object getCellEditorValue() {
                return Menu.Select;
            }

            @Override
            public Component getTableCellEditorComponent(JTable table, Object value,
                    boolean isSelected, int row, int column) {

                if (value == null) value = Menu.Select;

                if (! (value instanceof Menu)) {
                    throw new IllegalArgumentException("value is not an Menu: " + value.getClass().getName());
                }

                JComboBox<Menu> menuComboBox = new JComboBox<>();

                for (Menu menu : Menu.values()) {
                    if ((menu == Menu.MoveUp) && (row == 0)) continue;
                    if ((menu == Menu.MoveDown) && (row+1 == _tableModel._variables.size())) continue;
                    menuComboBox.addItem(menu);
                }
                JComboBoxUtil.setupComboBoxMaxRows(menuComboBox);

                menuComboBox.setSelectedItem(value);
                menuComboBox.addActionListener(this);

                return menuComboBox;
            }

            @Override
            @SuppressWarnings("unchecked")  // Not possible to check that event.getSource() is instanceof JComboBox<Menu>
            public void actionPerformed(ActionEvent event) {
                if (! (event.getSource() instanceof JComboBox)) {
                    throw new IllegalArgumentException("value is not an InitialValueType: " + event.getSource().getClass().getName());
                }
                JComboBox<Menu> menuComboBox =
                        (JComboBox<Menu>) event.getSource();
                int row = _table.getSelectedRow();
                Menu menu = menuComboBox.getItemAt(menuComboBox.getSelectedIndex());

                switch (menu) {
                    case Delete:
                        delete(row);
                        break;
                    case MoveUp:
                        if ((row) > 0) moveUp(row);
                        break;
                    case MoveDown:
                        if ((row+1) < _tableModel._variables.size()) moveUp(row+1);
                        break;
                    default:
                        // Do nothing
                }
                // Remove focus from combo box
                if (_tableModel._variables.size() > 0) _table.editCellAt(row, COLUMN_DATA);
            }

            private void delete(int row) {
                _tableModel._variables.remove(row);
                _tableModel.fireTableRowsDeleted(row, row);
            }

            private void moveUp(int row) {
                SymbolTable.VariableData temp = _tableModel._variables.get(row-1);
                _tableModel._variables.set(row-1, _tableModel._variables.get(row));
                _tableModel._variables.set(row, temp);
                _tableModel.fireTableRowsUpdated(row-1, row);
            }

        }
*/
    }








    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LogixNG_SelectStringListSwing.class);
}

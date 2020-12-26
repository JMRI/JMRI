package jmri.jmrit.logixng.tools.swing;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;

import jmri.jmrit.logixng.Module;
import jmri.jmrit.logixng.Module.Parameter;
import jmri.jmrit.logixng.Module.ParameterData;
import jmri.jmrit.logixng.Module.ReturnValueType;
import jmri.jmrit.logixng.SymbolTable.InitialValueType;

/**
 * Table model for local variables
 * @author Daniel Bergqvist Copyright 2018
 */
public class CallModuleParameterTableModel extends AbstractTableModel {

    public static final int COLUMN_NAME = 0;
    public static final int COLUMN_INPUT_TYPE = 1;
    public static final int COLUMN_INPUT_DATA = 2;
    public static final int COLUMN_OUTPUT_TYPE = 3;
    public static final int COLUMN_OUTPUT_DATA = 4;
//    public static final int COLUMN_MENU = 5;
    
    private final Map<String, Parameter> _parameters = new HashMap<>();
    private final List<ParameterData> _parameterData = new ArrayList<>();
    
    
    public CallModuleParameterTableModel(Module module, List<ParameterData> parameterData) {
        if (module != null) {
            for (Parameter p : module.getParameters()) {
                _parameters.put(p.getName(), p);
            }
            for (ParameterData v : parameterData) {
                _parameterData.add(new ParameterData(v));
            }
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public int getRowCount() {
        return _parameterData.size();
    }

    /** {@inheritDoc} */
    @Override
    public int getColumnCount() {
        return 5;
    }

    /** {@inheritDoc} */
    @Override
    public String getColumnName(int col) {
        switch (col) {
            case COLUMN_NAME:
                return Bundle.getMessage("ColumnParameterName");
            case COLUMN_INPUT_TYPE:
                return Bundle.getMessage("ColumnInputParameterType");
            case COLUMN_INPUT_DATA:
                return Bundle.getMessage("ColumnInputParameterData");
            case COLUMN_OUTPUT_TYPE:
                return Bundle.getMessage("ColumnOutputParameterType");
            case COLUMN_OUTPUT_DATA:
                return Bundle.getMessage("ColumnOutputParameterData");
//            case COLUMN_MENU:
//                return Bundle.getMessage("ColumnParameterMenu");
            default:
                throw new IllegalArgumentException("Invalid column");
        }
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> getColumnClass(int col) {
        switch (col) {
            case COLUMN_INPUT_TYPE:
                return InitialValueType.class;
            case COLUMN_OUTPUT_TYPE:
                return ReturnValueType.class;
//            case COLUMN_MENU:
//                return Menu.class;
            case COLUMN_NAME:
            case COLUMN_INPUT_DATA:
            case COLUMN_OUTPUT_DATA:
                return String.class;
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
        ParameterData variable = _parameterData.get(rowIndex);
        
        switch (columnIndex) {
            case COLUMN_NAME:
                variable._name = (String) value;
                break;
            case COLUMN_INPUT_TYPE:
                variable._initalValueType = (InitialValueType) value;
                break;
            case COLUMN_INPUT_DATA:
                variable._initialValueData = (String) value;
                break;
            case COLUMN_OUTPUT_TYPE:
                variable._initalValueType = (InitialValueType) value;
                break;
            case COLUMN_OUTPUT_DATA:
                variable._initialValueData = (String) value;
                break;
//            case COLUMN_MENU:
//                // Do nothing
//                break;
            default:
                throw new IllegalArgumentException("Invalid column");
        }      
    }
    
    /** {@inheritDoc} */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex >= _parameterData.size()) throw new IllegalArgumentException("Invalid row");
        
        switch (columnIndex) {
            case COLUMN_NAME:
                return _parameterData.get(rowIndex).getName();
            case COLUMN_INPUT_TYPE:
                return _parameterData.get(rowIndex).getInitalValueType();
            case COLUMN_INPUT_DATA:
                return _parameterData.get(rowIndex).getInitialValueData();
//            case COLUMN_MENU:
//                return Menu.Select;
            default:
                throw new IllegalArgumentException("Invalid column");
        }
    }
/*    
    public void setColumnForMenu(JTable table) {
        JComboBox<Menu> comboBox = new JComboBox<>();
        table.setRowHeight(comboBox.getPreferredSize().height);
        table.getColumnModel().getColumn(COLUMN_MENU)
                .setPreferredWidth((comboBox.getPreferredSize().width) + 4);
    }
*/    
    public void add() {
        int row = _parameterData.size();
        _parameterData.add(new ParameterData("", InitialValueType.None, "", ReturnValueType.None, ""));
        fireTableRowsInserted(row, row);
    }
    
    public List<ParameterData> getParameters() {
        return _parameterData;
    }
    
/*    
    public static enum Menu {
        Select(Bundle.getMessage("TableMenuSelect")),
        Delete(Bundle.getMessage("TableMenuDelete")),
        MoveUp(Bundle.getMessage("TableMenuMoveUp")),
        MoveDown(Bundle.getMessage("TableMenuMoveDown"));
        
        private final String _descr;
        
        private Menu(String descr) {
            _descr = descr;
        }
        
        @Override
        public String toString() {
            return _descr;
        }
    }
*/    
    
    public static class TypeCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            if (value == null) value = InitialValueType.None;
            
            if (! (value instanceof InitialValueType)) {
                throw new IllegalArgumentException("value is not an InitialValueType: " + value.getClass().getName());
            }
            setText(((InitialValueType) value).getDescr());
            return this;
        }
    }
    
/*    
    public static class MenuCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            if (value == null) value = Menu.Select;
            
            if (! (value instanceof Menu)) {
                throw new IllegalArgumentException("value is not an Menu: " + value.getClass().getName());
            }
            setText(((Menu) value).toString());
            return this;
        }
    }
*/    
    
    public static class TypeCellEditor extends AbstractCellEditor
            implements TableCellEditor, ActionListener {

        private InitialValueType _type;
        
        @Override
        public Object getCellEditorValue() {
            return this._type;
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            
            if (value == null) value = InitialValueType.None;
            
            if (! (value instanceof InitialValueType)) {
                throw new IllegalArgumentException("value is not an InitialValueType: " + value.getClass().getName());
            }
            
            JComboBox<InitialValueType> typeComboBox = new JComboBox<>();
            
            for (InitialValueType type : InitialValueType.values()) {
                typeComboBox.addItem(type);
            }
            
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
            JComboBox<InitialValueType> typeComboBox =
                    (JComboBox<InitialValueType>) event.getSource();
            _type = typeComboBox.getItemAt(typeComboBox.getSelectedIndex());
        }
        
    }
    
/*    
    public static class MenuCellEditor extends AbstractCellEditor
            implements TableCellEditor, ActionListener {

        JTable _table;
        CallModuleParameterTableModel _tableModel;
        
        public MenuCellEditor(JTable table, CallModuleParameterTableModel tableModel) {
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
                if ((menu == Menu.MoveDown) && (row+1 == _tableModel._parameterData.size())) continue;
                menuComboBox.addItem(menu);
            }
            
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
                    if ((row+1) < _tableModel._parameterData.size()) moveUp(row+1);
                    break;
                default:
                    // Do nothing
            }
            // Remove focus from combo box
            if (_tableModel._parameterData.size() > 0) _table.editCellAt(row, COLUMN_NAME);
        }
        
        private void delete(int row) {
            _tableModel._parameterData.remove(row);
            _tableModel.fireTableRowsDeleted(row, row);
        }
        
        private void moveUp(int row) {
            VariableData temp = _tableModel._parameterData.get(row-1);
            _tableModel._parameterData.set(row-1, _tableModel._parameterData.get(row));
            _tableModel._parameterData.set(row, temp);
            _tableModel.fireTableRowsUpdated(row-1, row);
        }
        
    }
*/
}

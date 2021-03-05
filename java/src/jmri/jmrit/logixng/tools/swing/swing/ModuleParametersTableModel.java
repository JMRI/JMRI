package jmri.jmrit.logixng.tools.swing.swing;

import jmri.jmrit.logixng.tools.swing.*;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;

import jmri.jmrit.logixng.Module;
import jmri.jmrit.logixng.Module.Parameter;
import jmri.jmrit.logixng.SymbolTable.InitialValueType;
import jmri.jmrit.logixng.implementation.DefaultSymbolTable.DefaultParameter;
import jmri.util.swing.JComboBoxUtil;

/**
 * Table model for local variables
 * @author Daniel Bergqvist Copyright 2018
 */
public class ModuleParametersTableModel extends AbstractTableModel {

    public static final int COLUMN_NAME = 0;
    public static final int COLUMN_IS_INPUT = 1;
    public static final int COLUMN_IS_OUTPUT = 2;
    public static final int COLUMN_MENU = 3;
    
    private final List<DefaultParameter> _parameters = new ArrayList<>();
    
    
    public ModuleParametersTableModel(@Nonnull Module module) {
        for (Parameter v : module.getParameters()) {
            _parameters.add(new DefaultParameter(v));
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public int getRowCount() {
        return _parameters.size();
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
            case COLUMN_NAME:
                return Bundle.getMessage("ColumnParameterName");
            case COLUMN_IS_INPUT:
                return Bundle.getMessage("ColumnParameterIsInput");
            case COLUMN_IS_OUTPUT:
                return Bundle.getMessage("ColumnParameterIsOutput");
            case COLUMN_MENU:
                return Bundle.getMessage("ColumnParameterMenu");
            default:
                throw new IllegalArgumentException("Invalid column");
        }
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> getColumnClass(int col) {
        switch (col) {
            case COLUMN_NAME:
                return String.class;
            case COLUMN_IS_INPUT:
                return Boolean.class;
            case COLUMN_IS_OUTPUT:
                return Boolean.class;
            case COLUMN_MENU:
                return Menu.class;
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
        if (rowIndex >= _parameters.size()) return;
        
        DefaultParameter parameter = _parameters.get(rowIndex);
        
        switch (columnIndex) {
            case COLUMN_NAME:
                parameter.setName((String) value);
                break;
            case COLUMN_IS_INPUT:
                parameter.setIsInput((Boolean) value);
                break;
            case COLUMN_IS_OUTPUT:
                parameter.setIsOutput((Boolean) value);
                break;
            case COLUMN_MENU:
                // Do nothing
                break;
            default:
                throw new IllegalArgumentException("Invalid column");
        }      
    }
    
    /** {@inheritDoc} */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex >= _parameters.size()) throw new IllegalArgumentException("Invalid row");
        
        switch (columnIndex) {
            case COLUMN_NAME:
                return _parameters.get(rowIndex).getName();
            case COLUMN_IS_INPUT:
                return _parameters.get(rowIndex).isInput();
            case COLUMN_IS_OUTPUT:
                return _parameters.get(rowIndex).isOutput();
            case COLUMN_MENU:
                return Menu.Select;
            default:
                throw new IllegalArgumentException("Invalid column");
        }
    }
    
    public void setColumnForMenu(JTable table) {
        JComboBox<Menu> comboBox = new JComboBox<>();
        table.setRowHeight(comboBox.getPreferredSize().height);
        table.getColumnModel().getColumn(COLUMN_MENU)
                .setPreferredWidth((comboBox.getPreferredSize().width) + 4);
    }
    
    public void add() {
        int row = _parameters.size();
        _parameters.add(new DefaultParameter("", false, false));
        fireTableRowsInserted(row, row);
    }
    
    public List<DefaultParameter> getParameters() {
        return _parameters;
    }
    
    
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
    
    
    public static class TypeCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            if (value == null) value = InitialValueType.None;
            
            if (! (value instanceof InitialValueType)) {
                throw new IllegalArgumentException("value is not an InitialValueType: " + value.getClass().getName());
            }
            setText(((InitialValueType) value).toString());
            return this;
        }
    }
    
    
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
            JComboBox<InitialValueType> typeComboBox =
                    (JComboBox<InitialValueType>) event.getSource();
            _type = typeComboBox.getItemAt(typeComboBox.getSelectedIndex());
        }
        
    }
    
    
    public static class MenuCellEditor extends AbstractCellEditor
            implements TableCellEditor, ActionListener {

        JTable _table;
        ModuleParametersTableModel _tableModel;
        
        public MenuCellEditor(JTable table, ModuleParametersTableModel tableModel) {
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
                if ((menu == Menu.MoveDown) && (row+1 == _tableModel._parameters.size())) continue;
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
                    if ((row+1) < _tableModel._parameters.size()) moveUp(row+1);
                    break;
                default:
                    // Do nothing
            }
            // Remove focus from combo box
            if (_tableModel._parameters.size() > 0) _table.editCellAt(row, COLUMN_NAME);
        }
        
        private void delete(int row) {
            _tableModel._parameters.remove(row);
            _tableModel.fireTableRowsDeleted(row, row);
        }
        
        private void moveUp(int row) {
            DefaultParameter temp = _tableModel._parameters.get(row-1);
            _tableModel._parameters.set(row-1, _tableModel._parameters.get(row));
            _tableModel._parameters.set(row, temp);
            _tableModel.fireTableRowsUpdated(row-1, row);
        }
        
    }

}

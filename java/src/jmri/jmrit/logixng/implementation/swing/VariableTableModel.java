package jmri.jmrit.logixng.implementation.swing;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;

import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.SymbolTable.InitialValueType;
import jmri.jmrit.logixng.implementation.DefaultSymbolTable.DefaultVariableData;

/**
 *
 * @author daniel
 */
public class VariableTableModel extends AbstractTableModel implements PropertyChangeListener {

    public static final int COLUMN_NAME = 0;
    public static final int COLUMN_TYPE = 1;
    public static final int COLUMN_DATA = 2;
    
    List<DefaultVariableData> _variables = new ArrayList<>();
    JComboBox<InitialValueType> _initialComboBox = new JComboBox<>();
    
    
    public VariableTableModel(MaleSocket maleSocket) {
        _variables.add(new DefaultVariableData("Abc", InitialValueType.Formula, "a+b"));
        _variables.add(new DefaultVariableData("Hello", InitialValueType.Formula, "a+b"));
        _variables.add(new DefaultVariableData("Something", InitialValueType.LocalVariable, "Abc"));
        _variables.add(new DefaultVariableData("Abc", InitialValueType.Formula, "a+b"));
        _variables.add(new DefaultVariableData("Hello", InitialValueType.Formula, "a+b"));
        _variables.add(new DefaultVariableData("Something", InitialValueType.LocalVariable, "Abc"));
        _variables.add(new DefaultVariableData("Abc", InitialValueType.Formula, "a+b"));
        _variables.add(new DefaultVariableData("Hello", InitialValueType.Formula, "a+b"));
        _variables.add(new DefaultVariableData("Something", InitialValueType.LocalVariable, "Abc"));
        _variables.add(new DefaultVariableData("Abc", InitialValueType.Formula, "a+b"));
        _variables.add(new DefaultVariableData("Hello", InitialValueType.Formula, "a+b"));
        _variables.add(new DefaultVariableData("Something", InitialValueType.LocalVariable, "Abc"));
        _variables.add(new DefaultVariableData("Abc", InitialValueType.Formula, "a+b"));
        _variables.add(new DefaultVariableData("Hello", InitialValueType.Formula, "a+b"));
        _variables.add(new DefaultVariableData("Something", InitialValueType.LocalVariable, "Abc"));
        _variables.add(new DefaultVariableData("Abc", InitialValueType.Formula, "a+b"));
        _variables.add(new DefaultVariableData("Hello", InitialValueType.Formula, "a+b"));
        _variables.add(new DefaultVariableData("Something", InitialValueType.LocalVariable, "Abc"));
    }
    
    /** {@inheritDoc} */
    @Override
    public int getRowCount() {
        return _variables.size();
    }

    /** {@inheritDoc} */
    @Override
    public int getColumnCount() {
        return 3;
    }

    /** {@inheritDoc} */
    @Override
    public String getColumnName(int col) {
        switch (col) {
            case COLUMN_NAME:
                return Bundle.getMessage("ColumnVariableName");
            case COLUMN_TYPE:
                return Bundle.getMessage("ColumnVariableType");
            case COLUMN_DATA:
                return Bundle.getMessage("ColumnVariableData");
            default:
                throw new IllegalArgumentException("Invalid column");
        }
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> getColumnClass(int col) {
        switch (col) {
            case COLUMN_TYPE:
                return InitialValueType.class;
            case COLUMN_NAME:
            case COLUMN_DATA:
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
        DefaultVariableData variable = _variables.get(rowIndex);
        
        switch (columnIndex) {
            case COLUMN_NAME:
                variable._name = (String) value;
                break;
            case COLUMN_TYPE:
                variable._initalValueType = (InitialValueType) value;
                break;
            case COLUMN_DATA:
                variable._initialValueData = (String) value;
                break;
            default:
                throw new IllegalArgumentException("Invalid column");
        }      
    }
    
    /** {@inheritDoc} */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex >= _variables.size()) throw new IllegalArgumentException("Invalid row");
        
        switch (columnIndex) {
            case COLUMN_NAME:
                return _variables.get(rowIndex).getName();
            case COLUMN_TYPE:
                return _variables.get(rowIndex).getInitalValueType();
            case COLUMN_DATA:
                return _variables.get(rowIndex).getInitialValueData();
            default:
                throw new IllegalArgumentException("Invalid column");
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    
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


    public static class TypeCellEditor extends AbstractCellEditor
            implements TableCellEditor, ActionListener {

        private InitialValueType type;
        
        @Override
        public Object getCellEditorValue() {
            return this.type;
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            
            if (value == null) value = InitialValueType.None;
            
            if (! (value instanceof InitialValueType)) {
                throw new IllegalArgumentException("value is not an InitialValueType: " + value.getClass().getName());
            }
            
            JComboBox<InitialValueType> typeComboBox = new JComboBox<>();
            
            for (InitialValueType aCountry : InitialValueType.values()) {
                typeComboBox.addItem(aCountry);
            }
            
            typeComboBox.setSelectedItem((InitialValueType) value);
            typeComboBox.addActionListener(this);
            
            return typeComboBox;
        }
        
        @Override
        public void actionPerformed(ActionEvent event) {
            JComboBox<InitialValueType> typeComboBox =
                    (JComboBox<InitialValueType>) event.getSource();
            type = typeComboBox.getItemAt(typeComboBox.getSelectedIndex());
        }
        
    }

}

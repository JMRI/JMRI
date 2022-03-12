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
import jmri.util.swing.JComboBoxUtil;

/**
 * Table model for CallModule parameters
 * @author Daniel Bergqvist Copyright 2020
 */
public class CallModuleParameterTableModel extends AbstractTableModel {

    public static final int COLUMN_NAME = 0;
    public static final int COLUMN_INPUT_TYPE = 1;
    public static final int COLUMN_INPUT_DATA = 2;
    public static final int COLUMN_OUTPUT_TYPE = 3;
    public static final int COLUMN_OUTPUT_DATA = 4;
    
    private final List<ParameterData> _parameterData = new ArrayList<>();
    
    
    public CallModuleParameterTableModel(Module module, List<ParameterData> parameterData) {
        if (module != null) {
            Map<String, ParameterData> parameterDataMap = new HashMap<>();
            for (ParameterData pd : parameterData) {
                parameterDataMap.put(pd._name, new ParameterData(pd));
            }
            for (Parameter p : module.getParameters()) {
                if (parameterDataMap.containsKey(p.getName())) {
                    _parameterData.add(parameterDataMap.get(p.getName()));
                } else {
                    _parameterData.add(new ParameterData(
                            p.getName(), InitialValueType.None, "",
                            ReturnValueType.None, ""));
                }
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
        return col != COLUMN_NAME;
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
                variable._initialValueType = (InitialValueType) value;
                break;
            case COLUMN_INPUT_DATA:
                variable._initialValueData = (String) value;
                break;
            case COLUMN_OUTPUT_TYPE:
                variable._returnValueType = (ReturnValueType) value;
                break;
            case COLUMN_OUTPUT_DATA:
                variable._returnValueData = (String) value;
                break;
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
                return _parameterData.get(rowIndex).getInitialValueType();
            case COLUMN_INPUT_DATA:
                return _parameterData.get(rowIndex).getInitialValueData();
            case COLUMN_OUTPUT_TYPE:
                return _parameterData.get(rowIndex).getReturnValueType();
            case COLUMN_OUTPUT_DATA:
                return _parameterData.get(rowIndex).getReturnValueData();
            default:
                throw new IllegalArgumentException("Invalid column");
        }
    }
    
    public void setColumnsForComboBoxes(JTable table) {
        JComboBox<InitialValueType> initValueComboBox = new JComboBox<>();
        JComboBox<ReturnValueType> returnValueComboBox = new JComboBox<>();
        table.setRowHeight(initValueComboBox.getPreferredSize().height);
        table.getColumnModel().getColumn(COLUMN_INPUT_TYPE)
                .setPreferredWidth((initValueComboBox.getPreferredSize().width) + 4);
        table.getColumnModel().getColumn(COLUMN_OUTPUT_TYPE)
                .setPreferredWidth((returnValueComboBox.getPreferredSize().width) + 4);
    }
    
    public void add() {
        int row = _parameterData.size();
        _parameterData.add(new ParameterData("", InitialValueType.None, "", ReturnValueType.None, ""));
        fireTableRowsInserted(row, row);
    }
    
    public List<ParameterData> getParameters() {
        return _parameterData;
    }
    
    
    public static class TypeCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            if (column == COLUMN_INPUT_TYPE) {
                if (value == null) value = InitialValueType.None;

                if (! (value instanceof InitialValueType)) {
                    throw new IllegalArgumentException("value is not an InitialValueType: " + value.getClass().getName());
                }
                setText(((InitialValueType) value).toString());
            }
            else if (column == COLUMN_OUTPUT_TYPE) {
                if (value == null) value = ReturnValueType.None;

                if (! (value instanceof ReturnValueType)) {
                    throw new IllegalArgumentException("value is not an ReturnValueType: " + value.getClass().getName());
                }
                setText(((ReturnValueType) value).getDescr());
            } else {
                throw new RuntimeException("Unknown column: "+Integer.toString(column));
            }
            return this;
        }
    }
    
    
    public static class InitialValueCellEditor extends AbstractCellEditor
            implements TableCellEditor, ActionListener {

        private InitialValueType _initialValueType;
        
        @Override
        public Object getCellEditorValue() {
            return this._initialValueType;
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            
            if (value == null) value = InitialValueType.None;
            
            if (! (value instanceof InitialValueType)) {
                throw new IllegalArgumentException("value is not an InitialValueType: " + value.getClass().getName());
            }
            
            JComboBox<InitialValueType> initialValueTypeComboBox = new JComboBox<>();
            
            for (InitialValueType type : InitialValueType.values()) {
                if (type.isValidAsParameter()) initialValueTypeComboBox.addItem(type);
            }
            JComboBoxUtil.setupComboBoxMaxRows(initialValueTypeComboBox);
            
            initialValueTypeComboBox.setSelectedItem(value);
            initialValueTypeComboBox.addActionListener(this);
            
            return initialValueTypeComboBox;
        }
        
        @Override
        @SuppressWarnings("unchecked")  // Not possible to check that event.getSource() is instanceof JComboBox<InitialValueType>
        public void actionPerformed(ActionEvent event) {
            if (! (event.getSource() instanceof JComboBox)) {
                throw new IllegalArgumentException("value is not an JComboBox: " + event.getSource().getClass().getName());
            }
            JComboBox<InitialValueType> initialValueTypeComboBox =
                    (JComboBox<InitialValueType>) event.getSource();
            _initialValueType = initialValueTypeComboBox.getItemAt(initialValueTypeComboBox.getSelectedIndex());
            
        }
        
    }
    
    
    public static class ReturnValueCellEditor extends AbstractCellEditor
            implements TableCellEditor, ActionListener {
        
        private ReturnValueType _returnValueType;
        
        @Override
        public Object getCellEditorValue() {
            return this._returnValueType;
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            
            if (value == null) value = ReturnValueType.None;
            
            if (! (value instanceof ReturnValueType)) {
                throw new IllegalArgumentException("value is not an ReturnValueType: " + value.getClass().getName());
            }
            
            JComboBox<ReturnValueType> returnValueTypeComboBox = new JComboBox<>();
            
            for (ReturnValueType type : ReturnValueType.values()) {
                returnValueTypeComboBox.addItem(type);
            }
            JComboBoxUtil.setupComboBoxMaxRows(returnValueTypeComboBox);
            
            returnValueTypeComboBox.setSelectedItem(value);
            returnValueTypeComboBox.addActionListener(this);
            
            return returnValueTypeComboBox;
        }
        
        @Override
        @SuppressWarnings("unchecked")  // Not possible to check that event.getSource() is instanceof JComboBox<ReturnValueType>
        public void actionPerformed(ActionEvent event) {
            if (! (event.getSource() instanceof JComboBox)) {
                throw new IllegalArgumentException("value is not an JComboBox: " + event.getSource().getClass().getName());
            }
            JComboBox<ReturnValueType> returnValueTypeComboBox =
                    (JComboBox<ReturnValueType>) event.getSource();
            _returnValueType = returnValueTypeComboBox.getItemAt(returnValueTypeComboBox.getSelectedIndex());
            
        }
        
    }
    
}

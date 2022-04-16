package jmri.jmrit.logixng.actions.swing;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;

import jmri.jmrit.logixng.actions.LogData;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.util.swing.JComboBoxUtil;

/**
 * Table model for LogData
 * 
 * @author Daniel Bergqvist Copyright 2021
 */
public class LogDataTableModel extends AbstractTableModel {

    // COLUMN_DUMMY is "hidden" but used when the panel is closed to
    // ensure that the last edited cell is saved.
    public static final int COLUMN_TYPE = 0;
    public static final int COLUMN_DATA = 1;
    public static final int COLUMN_DELETE = 2;
    public static final int COLUMN_DUMMY = 3;
    
    private final List<LogData.Data> _dataList = new ArrayList<>();
    
    
    public LogDataTableModel(Collection<LogData.Data> namedBeanReference) {
        if (namedBeanReference != null) _dataList.addAll(namedBeanReference);
    }
    
    /** {@inheritDoc} */
    @Override
    public int getRowCount() {
        return _dataList.size();
    }

    /** {@inheritDoc} */
    @Override
    public int getColumnCount() {
        return COLUMN_DUMMY+1;
    }

    /** {@inheritDoc} */
    @Override
    public String getColumnName(int col) {
        switch (col) {
            case COLUMN_TYPE:
                return Bundle.getMessage("LogData_ColumnType");
            case COLUMN_DATA:
                return Bundle.getMessage("LogData_ColumnData");
            case COLUMN_DELETE:
                return "";  // no label
            case COLUMN_DUMMY:
                return "";  // no label
            default:
                throw new IllegalArgumentException("Invalid column");
        }
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> getColumnClass(int col) {
        switch (col) {
            case COLUMN_TYPE:
                return LogData.DataType.class;
            case COLUMN_DATA:
                return String.class;
            case COLUMN_DELETE:
                return JButton.class;
            case COLUMN_DUMMY:
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
        LogData.Data ref = _dataList.get(rowIndex);
        
        switch (columnIndex) {
            case COLUMN_TYPE:
                LogData.DataType oldType = ref.getDataType();
                ref.setDataType((LogData.DataType) value);
                if (oldType != ref.getDataType()) ref.setData("");
                break;
            case COLUMN_DATA:
                ref.setData((String) value);
                break;
            case COLUMN_DELETE:
                delete(rowIndex);
                break;
            case COLUMN_DUMMY:
                break;
            default:
                throw new IllegalArgumentException("Invalid column");
        }      
    }
    
    /** {@inheritDoc} */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex >= _dataList.size()) throw new IllegalArgumentException("Invalid row");
        
        switch (columnIndex) {
            case COLUMN_TYPE:
                return _dataList.get(rowIndex).getDataType();
            case COLUMN_DATA:
                return _dataList.get(rowIndex).getData();
            case COLUMN_DELETE:
                return Bundle.getMessage("ButtonDelete");  // NOI18N
            case COLUMN_DUMMY:
                return "";
            default:
                throw new IllegalArgumentException("Invalid column");
        }
    }
    
    public void setColumnsForComboBoxes(JTable table) {
        JComboBox<LogData.DataType> beanTypeComboBox = new JComboBox<>();
        table.setRowHeight(beanTypeComboBox.getPreferredSize().height);
        table.getColumnModel().getColumn(COLUMN_TYPE)
                .setPreferredWidth((beanTypeComboBox.getPreferredSize().width) + 4);
    }
    
    public void add() {
        int row = _dataList.size();
        try {
            _dataList.add(new LogData.Data(LogData.DataType.LocalVariable, ""));
        } catch (ParserException e) {
            throw new RuntimeException(e);  // This should never happen
        }
        fireTableRowsInserted(row, row);
    }
    
    private void delete(int row) {
        _dataList.remove(row);
        fireTableRowsDeleted(row, row);
    }
    
    public List<LogData.Data> getDataList() {
        return _dataList;
    }
    
    
    public static class CellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            if (column == COLUMN_TYPE) {
                if (value == null) value = LogData.DataType.LocalVariable;
                
                if (! (value instanceof LogData.DataType)) {
                    throw new IllegalArgumentException("value is not an LogData.DataType: " + value.getClass().getName());
                }
                setText(((LogData.DataType) value).toString());
            } else {
                throw new RuntimeException("Unknown column: "+Integer.toString(column));
            }
            return this;
        }
    }
    
    
    public static class DataTypeCellEditor extends AbstractCellEditor
            implements TableCellEditor, ActionListener {
        
        private LogData.DataType _beanType;
        
        @Override
        public Object getCellEditorValue() {
            return this._beanType;
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            
            if (value == null) value = LogData.DataType.LocalVariable;
            
            if (! (value instanceof LogData.DataType)) {
                throw new IllegalArgumentException("value is not an LogData.DataType: " + value.getClass().getName());
            }
            
            JComboBox<LogData.DataType> returnValueTypeComboBox = new JComboBox<>();
            
            for (LogData.DataType type : LogData.DataType.values()) {
                returnValueTypeComboBox.addItem(type);
            }
            JComboBoxUtil.setupComboBoxMaxRows(returnValueTypeComboBox);
            
            returnValueTypeComboBox.setSelectedItem(value);
            returnValueTypeComboBox.addActionListener(this);
            
            return returnValueTypeComboBox;
        }
        
        @Override
        @SuppressWarnings("unchecked")  // Not possible to check that event.getSource() is instanceof JComboBox<LogData.DataType>
        public void actionPerformed(ActionEvent event) {
            if (! (event.getSource() instanceof JComboBox)) {
                throw new IllegalArgumentException("value is not an JComboBox: " + event.getSource().getClass().getName());
            }
            JComboBox<LogData.DataType> returnValueTypeComboBox =
                    (JComboBox<LogData.DataType>) event.getSource();
            _beanType = returnValueTypeComboBox.getItemAt(returnValueTypeComboBox.getSelectedIndex());
            
        }
        
    }
    
/*    
    public NamedBeanCellEditor getNamedBeanCellEditor() {
        return new NamedBeanCellEditor();
    }
    
    
    public class NamedBeanCellEditor extends AbstractCellEditor
            implements TableCellEditor, ActionListener {
        
        private String _namedBean;
        
        @Override
        public Object getCellEditorValue() {
            return this._namedBean;
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            
            if ((value != null) && (! (value instanceof String))) {
                throw new IllegalArgumentException("value is not a String: " + value.getClass().getName());
            }
            
            JComboBox<String> namedBeanComboBox = new JComboBox<>();
            SortedSet<? extends NamedBean> set =
                    _dataList.get(row).getDataType().getManager().getNamedBeanSet();
            
            String name = _dataList.get(row).getData();
            
            if (!set.isEmpty()) {
                for (NamedBean bean : set) {
                    namedBeanComboBox.addItem(bean.getDisplayName());
                    
                    if (name != null) {
                        if (name.equals(bean.getUserName()) || name.equals(bean.getSystemName())) {
                            namedBeanComboBox.setSelectedItem(bean.getDisplayName());
                        }
                    }
                }
                JComboBoxUtil.setupComboBoxMaxRows(namedBeanComboBox);
            } else {
                namedBeanComboBox.addItem("");
            }
            
//            namedBeanComboBox.setSelectedItem(value);
            namedBeanComboBox.addActionListener(this);
            
            return namedBeanComboBox;
        }
        
        @Override
        @SuppressWarnings("unchecked")  // Not possible to check that event.getSource() is instanceof JComboBox<LogData.DataType>
        public void actionPerformed(ActionEvent event) {
            if (! (event.getSource() instanceof JComboBox)) {
                throw new IllegalArgumentException("value is not an JComboBox: " + event.getSource().getClass().getName());
            }
            JComboBox<String> namedBeanComboBox = (JComboBox<String>) event.getSource();
            int index = namedBeanComboBox.getSelectedIndex();
            _namedBean = (index != -1) ? namedBeanComboBox.getItemAt(index) : null;
        }
        
    }
*/    
}

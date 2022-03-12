package jmri.jmrit.logixng.actions.swing;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;

import jmri.NamedBean;
import jmri.jmrit.logixng.actions.NamedBeanType;
import jmri.jmrit.logixng.actions.ActionListenOnBeans.NamedBeanReference;
import jmri.util.swing.JComboBoxUtil;

/**
 * Table model for ListenOnBeans named beans
 * 
 * @author Daniel Bergqvist Copyright 2020
 */
public class ListenOnBeansTableModel extends AbstractTableModel {

    // COLUMN_DUMMY is "hidden" but used when the panel is closed to
    // ensure that the last edited cell is saved.
    public static final int COLUMN_BEAN_TYPE = 0;
    public static final int COLUMN_BEAN_NAME = COLUMN_BEAN_TYPE + 1;
    public static final int COLUMN_BEAN_ALL = COLUMN_BEAN_NAME + 1;
    public static final int COLUMN_DELETE = COLUMN_BEAN_ALL + 1;
    public static final int COLUMN_DUMMY = COLUMN_DELETE + 1;
    
    private final List<NamedBeanReference> _namedBeanReference = new ArrayList<>();
    
    
    public ListenOnBeansTableModel(Collection<NamedBeanReference> namedBeanReference) {
        if (namedBeanReference != null) {
            for (NamedBeanReference ref : namedBeanReference) {
                _namedBeanReference.add(new NamedBeanReference(ref));
            }
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public int getRowCount() {
        return _namedBeanReference.size();
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
            case COLUMN_BEAN_TYPE:
                return Bundle.getMessage("ActionListenOnBeans_ColumnBeanType");
            case COLUMN_BEAN_NAME:
                return Bundle.getMessage("ActionListenOnBeans_ColumnBeanName");
            case COLUMN_BEAN_ALL:
                return Bundle.getMessage("ActionListenOnBeans_ColumnBeanAll");
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
            case COLUMN_BEAN_TYPE:
                return NamedBeanType.class;
            case COLUMN_BEAN_NAME:
                return String.class;
            case COLUMN_BEAN_ALL:
                return Boolean.class;
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
        NamedBeanReference ref = _namedBeanReference.get(rowIndex);
        
        switch (columnIndex) {
            case COLUMN_BEAN_TYPE:
                NamedBeanType oldType = ref.getType();
                ref.setType((NamedBeanType) value);
                if (oldType != ref.getType()) ref.setName("");
                break;
            case COLUMN_BEAN_NAME:
                ref.setName((String) value);
                break;
            case COLUMN_BEAN_ALL:
                ref.setListenOnAllProperties((boolean) value);
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
        if (rowIndex >= _namedBeanReference.size()) throw new IllegalArgumentException("Invalid row");
        
        switch (columnIndex) {
            case COLUMN_BEAN_TYPE:
                return _namedBeanReference.get(rowIndex).getType();
            case COLUMN_BEAN_NAME:
                return _namedBeanReference.get(rowIndex).getName();
            case COLUMN_BEAN_ALL:
                return _namedBeanReference.get(rowIndex).getListenOnAllProperties();
            case COLUMN_DELETE:
                return Bundle.getMessage("ButtonDelete");  // NOI18N
            case COLUMN_DUMMY:
                return "";
            default:
                throw new IllegalArgumentException("Invalid column");
        }
    }
    
    public void setColumnsForComboBoxes(JTable table) {
        JComboBox<NamedBeanType> beanTypeComboBox = new JComboBox<>();
        table.setRowHeight(beanTypeComboBox.getPreferredSize().height);
        table.getColumnModel().getColumn(COLUMN_BEAN_TYPE)
                .setPreferredWidth((beanTypeComboBox.getPreferredSize().width) + 4);
    }
    
    public void add() {
        int row = _namedBeanReference.size();
        _namedBeanReference.add(new NamedBeanReference("", NamedBeanType.Turnout, false));
        fireTableRowsInserted(row, row);
    }
    
    private void delete(int row) {
        _namedBeanReference.remove(row);
        fireTableRowsDeleted(row, row);
    }
    
    public List<NamedBeanReference> getReferences() {
        return _namedBeanReference;
    }
    
    
    public static class CellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            if (column == COLUMN_BEAN_TYPE) {
                if (value == null) value = NamedBeanType.Turnout;
                
                if (! (value instanceof NamedBeanType)) {
                    throw new IllegalArgumentException("value is not an NamedBeanType: " + value.getClass().getName());
                }
                setText(((NamedBeanType) value).toString());
            } else {
                throw new RuntimeException("Unknown column: "+Integer.toString(column));
            }
            return this;
        }
    }
    
    
    public static class NamedBeanTypeCellEditor extends AbstractCellEditor
            implements TableCellEditor, ActionListener {
        
        private NamedBeanType _beanType;
        
        @Override
        public Object getCellEditorValue() {
            return this._beanType;
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            
            if (value == null) value = NamedBeanType.Turnout;
            
            if (! (value instanceof NamedBeanType)) {
                throw new IllegalArgumentException("value is not an NamedBeanType: " + value.getClass().getName());
            }
            
            JComboBox<NamedBeanType> returnValueTypeComboBox = new JComboBox<>();
            
            for (NamedBeanType type : NamedBeanType.values()) {
                returnValueTypeComboBox.addItem(type);
            }
            JComboBoxUtil.setupComboBoxMaxRows(returnValueTypeComboBox);
            
            returnValueTypeComboBox.setSelectedItem(value);
            returnValueTypeComboBox.addActionListener(this);
            
            return returnValueTypeComboBox;
        }
        
        @Override
        @SuppressWarnings("unchecked")  // Not possible to check that event.getSource() is instanceof JComboBox<NamedBeanType>
        public void actionPerformed(ActionEvent event) {
            if (! (event.getSource() instanceof JComboBox)) {
                throw new IllegalArgumentException("value is not an JComboBox: " + event.getSource().getClass().getName());
            }
            JComboBox<NamedBeanType> returnValueTypeComboBox =
                    (JComboBox<NamedBeanType>) event.getSource();
            _beanType = returnValueTypeComboBox.getItemAt(returnValueTypeComboBox.getSelectedIndex());
            
        }
        
    }
    
    
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
                    _namedBeanReference.get(row).getType().getManager().getNamedBeanSet();
            
            String name = _namedBeanReference.get(row).getName();
            
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
        @SuppressWarnings("unchecked")  // Not possible to check that event.getSource() is instanceof JComboBox<NamedBeanType>
        public void actionPerformed(ActionEvent event) {
            if (! (event.getSource() instanceof JComboBox)) {
                throw new IllegalArgumentException("value is not an JComboBox: " + event.getSource().getClass().getName());
            }
            JComboBox<String> namedBeanComboBox = (JComboBox<String>) event.getSource();
            int index = namedBeanComboBox.getSelectedIndex();
            _namedBean = (index != -1) ? namedBeanComboBox.getItemAt(index) : null;
        }
        
    }
    
}

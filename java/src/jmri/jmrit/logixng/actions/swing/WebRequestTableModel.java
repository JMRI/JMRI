package jmri.jmrit.logixng.actions.swing;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;

import jmri.jmrit.logixng.SymbolTable.InitialValueType;
import jmri.jmrit.logixng.actions.WebRequest;
import jmri.util.swing.JComboBoxUtil;

/**
 * Table model for WebRequest
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class WebRequestTableModel extends AbstractTableModel {

    // COLUMN_DUMMY is "hidden" but used when the panel is closed to
    // ensure that the last edited cell is saved.
    public static final int COLUMN_NAME = 0;
    public static final int COLUMN_TYPE = 1;
    public static final int COLUMN_DATA = 2;
    public static final int COLUMN_DELETE = 3;
    public static final int COLUMN_DUMMY = 4;

    private final List<WebRequest.Parameter> _parameterList = new ArrayList<>();


    public WebRequestTableModel(Collection<WebRequest.Parameter> namedBeanReference) {
        if (namedBeanReference != null) _parameterList.addAll(namedBeanReference);
    }

    /** {@inheritDoc} */
    @Override
    public int getRowCount() {
        return _parameterList.size();
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
            case COLUMN_NAME:
                return Bundle.getMessage("WebRequest_ColumnName");
            case COLUMN_TYPE:
                return Bundle.getMessage("WebRequest_ColumnType");
            case COLUMN_DATA:
                return Bundle.getMessage("WebRequest_ColumnData");
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
            case COLUMN_NAME:
                return String.class;
            case COLUMN_TYPE:
                return InitialValueType.class;
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
        WebRequest.Parameter ref = _parameterList.get(rowIndex);

        switch (columnIndex) {
            case COLUMN_NAME:
                ref.setName((String) value);
                break;
            case COLUMN_TYPE:
                if (value != null) {
                    InitialValueType oldType = ref.getType();
                    ref.setType((InitialValueType) value);
                    if (oldType != ref.getType()) ref.setData("");
                } else {
                    // This happens if the combo box is open when the user clicks on another cell.
                    log.debug("Cannot set data type to null");
                }
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
        if (rowIndex >= _parameterList.size()) throw new IllegalArgumentException("Invalid row");

        switch (columnIndex) {
            case COLUMN_NAME:
                return _parameterList.get(rowIndex).getName();
            case COLUMN_TYPE:
                return _parameterList.get(rowIndex).getType();
            case COLUMN_DATA:
                return _parameterList.get(rowIndex).getData();
            case COLUMN_DELETE:
                return Bundle.getMessage("ButtonDelete");  // NOI18N
            case COLUMN_DUMMY:
                return "";
            default:
                throw new IllegalArgumentException("Invalid column");
        }
    }

    public void setColumnsForComboBoxes(JTable table) {
        JComboBox<InitialValueType> beanTypeComboBox = new JComboBox<>();
        table.setRowHeight(beanTypeComboBox.getPreferredSize().height);
        table.getColumnModel().getColumn(COLUMN_TYPE)
                .setPreferredWidth((beanTypeComboBox.getPreferredSize().width) + 4);
    }

    public void add() {
        int row = _parameterList.size();
        _parameterList.add(new WebRequest.Parameter("", InitialValueType.LocalVariable, ""));
        fireTableRowsInserted(row, row);
    }

    private void delete(int row) {
        _parameterList.remove(row);
        fireTableRowsDeleted(row, row);
    }

    public List<WebRequest.Parameter> getDataList() {
        return _parameterList;
    }


    public static class CellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            if (column == COLUMN_TYPE) {
                if (value == null) value = InitialValueType.LocalVariable;

                if (! (value instanceof InitialValueType)) {
                    throw new IllegalArgumentException("value is not an InitialValueType: " + value.getClass().getName());
                }
                setText(((InitialValueType) value).toString());
            } else {
                throw new RuntimeException("Unknown column: "+Integer.toString(column));
            }
            return this;
        }
    }


    public static class DataTypeCellEditor extends AbstractCellEditor
            implements TableCellEditor, ActionListener {

        private InitialValueType _beanType;

        @Override
        public Object getCellEditorValue() {
            return this._beanType;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {

            if (value == null) value = InitialValueType.LocalVariable;

            if (! (value instanceof InitialValueType)) {
                throw new IllegalArgumentException("value is not an InitialValueType: " + value.getClass().getName());
            }

            JComboBox<InitialValueType> returnValueTypeComboBox = new JComboBox<>();

            for (InitialValueType type : InitialValueType.values()) {
                returnValueTypeComboBox.addItem(type);
            }
            JComboBoxUtil.setupComboBoxMaxRows(returnValueTypeComboBox);

            returnValueTypeComboBox.setSelectedItem(value);
            returnValueTypeComboBox.addActionListener(this);

            return returnValueTypeComboBox;
        }

        @Override
        @SuppressWarnings("unchecked")  // Not possible to check that event.getSource() is instanceof JComboBox<InitialValueType>
        public void actionPerformed(ActionEvent event) {
            if (! (event.getSource() instanceof JComboBox)) {
                throw new IllegalArgumentException("value is not an JComboBox: " + event.getSource().getClass().getName());
            }
            JComboBox<InitialValueType> returnValueTypeComboBox =
                    (JComboBox<InitialValueType>) event.getSource();
            _beanType = returnValueTypeComboBox.getItemAt(returnValueTypeComboBox.getSelectedIndex());

        }

    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WebRequestTableModel.class);

}

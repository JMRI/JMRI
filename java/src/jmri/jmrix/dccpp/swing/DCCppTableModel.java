/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.jmrix.dccpp.swing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author munderwd
 */
public abstract class DCCppTableModel extends AbstractTableModel {

    protected List<List<Object>> rowData;
    protected List<List<Object>> deletedData;
    protected String[] columnNames;
    private int _dirtyCol = 0;
    private int _newCol = 0;
    private int _deleteCol = 0;
    protected int _lastDataCol = 0;

    public DCCppTableModel(int dc, int nc, int delc, int numc) {
        super();
        _dirtyCol = dc;
        _newCol = nc;
        _deleteCol = delc;
        _lastDataCol = numc - 4;
        rowData = new ArrayList<>();
        deletedData = new ArrayList<>();
    }

    public int getDeleteColumn() {
        return 3;
    }

    public boolean isDirty() {
        for (int i = 0; i < rowData.size(); i++) {
            if (isDirtyRow(i)) {
                return true;
            }
        }
        return false;
    }

    public boolean isNewRow(int row) {
        //return((boolean) isNew.get(row));
        return (boolean) rowData.get(row).get(_newCol);
    }

    public void setNewRow(int row, boolean n) {
        //isNew.set(row, n);
        rowData.get(row).set(_newCol, n);
    }

    public boolean isDirtyRow(int row) {
        //return((boolean)isDirty.get(row));
        return (boolean) rowData.get(row).get(_dirtyCol);
    }

    public void setDirtyRow(int row, boolean d) {
        //isDirty.set(row, d);
        rowData.get(row).set(_dirtyCol, d);
    }

    public boolean isMarkedForDelete(int row) {
        //return((boolean)markDelete.get(row));
        return (boolean) rowData.get(row).get(_deleteCol);
    }

    public void removeRow(int row) {
        deletedData.add(rowData.get(row));
        rowData.remove(row);
        fireTableRowsDeleted(row, row);
    }

    public void markForDelete(int row, boolean mark) {
        //markDelete.set(row, mark);
        rowData.get(row).set(_deleteCol, mark);
        if (mark) {
            removeRow(row);
        }
    }

    public boolean contains(List<Object> v) {
        Iterator<List<Object>> it = rowData.iterator();
        while (it.hasNext()) {
            List<Object> r = it.next();
            if (r.get(0) == v.get(0)) {
                return true;
            }
        }
        return false;
    }

    public void insertData(List<Object> v, boolean isnew) {
        if (!rowData.contains(v)) {
            v.add(Bundle.getMessage("ColumnDelete"));
            v.add(isnew); // is new
            v.add(false); // is dirty (no)
            v.add(false); // is marked for delete (of course not)
            rowData.add(v);
        }
        fireTableDataChanged();
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        rowData.get(row).set(col, value);
        if (col < _lastDataCol) {
            // Only set dirty if data changed, not state
            // Data is in columns 0-2
            setDirtyRow(row, true);
        }
        fireTableCellUpdated(row, col);
    }

    public List<List<Object>> getRowData() {
        return rowData;
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    @Override
    public int getRowCount() {
        return rowData.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public Object getValueAt(int row, int col) {
        if (row >= 0 && row < rowData.size()) {
            return rowData.get(row).get(col);
        } else {
            return 0;
        }
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return true;
    }

}

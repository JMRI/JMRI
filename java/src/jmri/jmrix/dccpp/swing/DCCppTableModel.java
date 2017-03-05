/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.jmrix.dccpp.swing;

import java.util.Iterator;
import javax.swing.table.AbstractTableModel;
import java.util.Vector;

/**
 *
 * @author munderwd
 */
public abstract class DCCppTableModel extends AbstractTableModel {
    
    protected Vector<Vector<Object>> rowData;
    protected Vector<Vector<Object>> deletedData;
    protected String[] columnNames;
    private int _dirtyCol = 0;
    private int _newCol = 0;
    private int _deleteCol = 0;
    private int _numCols = 0;
    protected int _lastDataCol = 0;
    
    public DCCppTableModel(int dc, int nc, int delc, int numc) {
        super();
        _dirtyCol = dc;
        _newCol = nc;
        _deleteCol = delc;
        _numCols = numc;
        _lastDataCol = numc - 4;
        rowData = new Vector<Vector<Object>>();
        deletedData = new Vector<Vector<Object>>();
    }
    
    public int getDeleteColumn() {
        return(3);
    }

    public boolean isDirty() {
        for (int i = 0; i < rowData.size(); i++) {
            if (isDirtyRow(i)) {
                return(true);
            }
        }
        return(false);
    };
    
    public boolean isNewRow(int row) {
        //return((boolean) isNew.elementAt(row));
        return((boolean)rowData.elementAt(row).elementAt(_newCol));
    }

    public void setNewRow(int row, boolean n) {
        //isNew.setElementAt(n, row);
        rowData.elementAt(row).setElementAt(n, _newCol);
    }

    public boolean isDirtyRow(int row) {
        //return((boolean)isDirty.elementAt(row));
        return((boolean)rowData.elementAt(row).elementAt(_dirtyCol));
    }

    public void setDirtyRow(int row, boolean d) {
        //isDirty.setElementAt(d, row);
        rowData.elementAt(row).setElementAt(d, _dirtyCol);
    }

    public boolean isMarkedForDelete(int row) {
        //return((boolean)markDelete.elementAt(row));
        return((boolean)rowData.elementAt(row).elementAt(_deleteCol));
    }

    public void removeRow(int row) {
        deletedData.add(rowData.elementAt(row));
        rowData.remove(row);
        fireTableRowsDeleted(row, row);
    }

    public void markForDelete(int row, boolean mark) {
        //markDelete.setElementAt(mark, row);
        rowData.elementAt(row).setElementAt(mark, _deleteCol);
        if (mark) {
            removeRow(row);
        }
    }

        public boolean contains(Vector<Object> v) {
            Iterator<Vector<Object>> it = rowData.iterator();
            while(it.hasNext()) {
                Vector<Object> r = it.next();
                if (r.firstElement() == v.firstElement()) {
                    return(true);
                }
            }
            return(false);
        }

        public void insertData(Vector<Object> v, boolean isnew) {
            if (!rowData.contains(v)) {
                v.add("Delete");
                v.add(isnew); // is new
                v.add(false); // is dirty (no)
                v.add(false); // is marked for delete (of course not)
                rowData.add(v);
            }
            fireTableDataChanged();
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            rowData.elementAt(row).setElementAt(value, col);
            if (col < _lastDataCol) {
                // Only set dirty if data changed, not state
                // Data is in columns 0-2
                setDirtyRow(row, true);
            }
            fireTableCellUpdated(row, col);
        }
        
        public Vector<Vector<Object>> getRowData() {
            return(rowData);
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
                return rowData.elementAt(row).elementAt(col);
            } else {
                return(new Integer(0));
            }
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return true;
        }
        
}

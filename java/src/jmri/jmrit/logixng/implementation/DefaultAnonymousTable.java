package jmri.jmrit.logixng.implementation;

import jmri.jmrit.logixng.AnonymousTable;

/**
 * Default implementation for anonymous tables
 */
public class DefaultAnonymousTable implements AnonymousTable {

    private final int _numRows;
    private final int _numColumns;
    private final Object[][] _data;
    
    public DefaultAnonymousTable(int numRows, int numColumns) {
        _numRows = numRows;
        _numColumns = numColumns;
        _data = new Object[numRows+1][numColumns+1];
    }
    
    /**
     * Create a new anonymous table with an existing array of cells.
     * Row 0 has the column names and that column 0 has the row names.
     * @param data the data in the table. Note that this data is not copied to
     * an new array but used by the table as is.
     */
    public DefaultAnonymousTable(Object[][] data) {
        // Row 0 has column names
        _numRows = data.length-1;
        // Column 0 has row names
        _numColumns = data[0].length-1;
        _data = data;
        
        for (int row=0; row < _data.length; row++) {
            if (_numColumns+1 != _data[row].length) {
                throw new IllegalArgumentException("All rows in table must have same number of columns");
            }
        }
        
        int i[][] = new int[15][];
        i[5] = new int[3];
        i[7] = new int[10];
        i[5][2] = 3;
//        i[5][8] = 4;
        i[7][2] = 5;
        i[7][8] = 6;
//        i[2][2] = 7;
//        i[2][8] = 8;
        
        i = new int[15][20];
//        i[5] = new int[3];
        i[7] = new int[10];
        i[5][2] = 3;
        i[5][8] = 4;
        i[7][2] = 5;
        i[7][8] = 6;
        i[2][2] = 7;
        i[2][8] = 8;
    }

    @Override
    public Object get(String row, String column) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setCell(Object value, String row) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setCell(Object value, String row, String column) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int numRows() {
        return _numRows;
    }

    @Override
    public int numColumns() {
        return _numColumns;
    }
    
}

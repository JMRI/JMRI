package jmri.jmrit.logixng.implementation;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import jmri.jmrit.logixng.AnonymousTable;
import jmri.jmrit.logixng.NamedTable;

/**
 * Default implementation for anonymous tables
 */
public class DefaultAnonymousTable implements AnonymousTable {

    private final int _numRows;
    private final int _numColumns;
    private final Object[][] _data;
    private final Map<String,Integer> rowNames = new HashMap<>();
    private final Map<String,Integer> columnNames = new HashMap<>();
    
    public DefaultAnonymousTable(int numRows, int numColumns) {
        _numRows = numRows;
        _numColumns = numColumns;
        _data = new Object[numRows+1][numColumns+1];
        setupTable();
    }
    
    /**
     * Create a new anonymous table with an existing array of cells.
     * Row 0 has the column names and column 0 has the row names.
     * @param data the data in the table. Note that this data is not copied to
     * an new array but used by the table as is.
     */
    public DefaultAnonymousTable(@Nonnull Object[][] data) {
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
        
        setupTable();
/*        
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
*/
    }
    
    private void setupTable() {
//    private final int _numRows;
//    private final int _numColumns;
//    private final Object[][] _data;
//    private final Map<String,Integer> rowNames = new HashMap<>();
//    private final Map<String,Integer> columnNames = new HashMap<>();
        for (int i=1; i <= _numRows; i++) {
            Object cell = _data[i][0];
            if (cell != null) {
                rowNames.put(cell.toString(), i);
            } else {
                rowNames.put(Integer.toString(i), i);
            }
        }
        
        for (int i=1; i <= _numColumns; i++) {
            Object cell = _data[0][i];
            if (cell != null && cell instanceof String) {
                columnNames.put(cell.toString(), i);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void storeTableAsCSV(@Nonnull File file) {
        storeTableAsCSV(file, null, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void storeTableAsCSV(
            @Nonnull File file,
            @CheckForNull String systemName, @CheckForNull String userName) {
        
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Object getCell(@Nonnull String row, @Nonnull String column) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCell(Object value, String row, String column) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int numRows() {
        return _numRows;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int numColumns() {
        return _numColumns;
    }
    
}

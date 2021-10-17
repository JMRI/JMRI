package jmri.jmrit.logixng.implementation;

import java.io.File;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import jmri.jmrit.logixng.AnonymousTable;

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
        for (int i=0; i <= _numRows; i++) {
            Object cell = _data[i][0];
            if (cell != null && cell instanceof String) {
                rowNames.put(cell.toString(), i);
            }
        }
        
        for (int i=0; i <= _numColumns; i++) {
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
    public void storeTableAsCSV(@Nonnull File file)
            throws FileNotFoundException {
        storeTableAsCSV(file, null, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void storeTableAsCSV(
            @Nonnull File file,
            @CheckForNull String systemName, @CheckForNull String userName)
            throws FileNotFoundException {
        
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)))) {
            writer.format("%s\t%s%n", systemName, userName);
            for (int row=0; row <= _numRows; row++) {
                for (int column=0; column <= _numColumns; column++) {
                    if (column > 0) writer.print("\t");
//                    System.out.format("%d, %d: %s%n", row, column, _data[row][column].toString());
                    if (_data[row][column] != null) writer.print(_data[row][column].toString());
                }
                writer.println();
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Object getCell(int row, int column) {
        return _data[row][column];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCell(Object value, int row, int column) {
        _data[row][column] = value;
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
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getRowNumber(String rowName) {
        Integer rowNumber = rowNames.get(rowName);
        if (rowNumber == null) {
            try {
                int row = Integer.parseInt(rowName);
                if (row >= 0 && row <= _numRows) return row;
            } catch (NumberFormatException e) {
                // Do nothing
            }
        } else {
            return rowNumber;
        }
        // If here, the row is not found
        return -1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getColumnNumber(String columnName) {
        Integer columnNumber = columnNames.get(columnName);
        if (columnNumber == null) {
            try {
                int column = Integer.parseInt(columnName);
                if (column >= 0 && column <= _numColumns) return column;
            } catch (NumberFormatException e) {
                // Do nothing
            }
        } else {
            return columnNumber;
        }
        // If here, the row is not found
        return -1;
    }

    @Override
    public void insertColumn(int col) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void deleteColumn(int col) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void insertRow(int row) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void deleteRow(int row) {
        throw new UnsupportedOperationException("Not supported");
    }

}

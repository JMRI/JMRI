package jmri.jmrit.logixng;

import java.io.File;
import java.io.FileNotFoundException;
import javax.annotation.CheckForNull;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Represent a Table.
 * A table is a two dimensional array where the rows and columns may have names.
 *
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public interface Table {

    /**
     * Get the value of a cell.
     * If the table has both rows and columns, the value of the first column
     * will be returned.
     * @param row the row of the cell or null if all rows should be returned
     * @return the value of the cell
     */
    @CheckReturnValue
    default public Object getCell(int row) {
        return getCell(row, 1);
    }
    
    /**
     * Get the value of a cell.
     * @param row the row of the cell
     * @param column the column of the cell
     * @return the value of the cell
     */
    @CheckReturnValue
    public Object getCell(int row, int column);
    
    /**
     * Get the value of a cell.
     * If the table has both rows and columns, the value of the first column
     * will be returned.
     * @param row the row of the cell or null if all rows should be returned
     * @return the value of the cell
     */
    @CheckReturnValue
    default public Object getCell(@Nonnull String row) {
        int rowNumber = getRowNumber(row);
        if (rowNumber == -1) {
            throw new IllegalArgumentException("Row '"+row+"' is not found");
        }
        
        return getCell(rowNumber, 1);
    }
    
    /**
     * Get the value of a cell.
     * @param row the row of the cell. If this string is a name of a row, that
     * row is used. If it's not a name of a row, but an integer value, that
     * index is used, where row 0 is the name of the row.
     * @param column the column of the cell. If this string is a name of a
     * column, that column is used. If it's not a name of a column, but an
     * integer value, that index is used, where column 0 is the name of the
     * column.
     * @return the value of the cell
     */
    default public Object getCell(@Nonnull String row, @Nonnull String column) {
        int rowNumber = getRowNumber(row);
        if (rowNumber == -1) {
            throw new IllegalArgumentException("Row '"+row+"' is not found");
        }
        
        int columnNumber = getColumnNumber(column);
        if (columnNumber == -1) {
            throw new IllegalArgumentException("Column '"+column+"' is not found");
        }
        
        return getCell(rowNumber, columnNumber);
    }
    
    /**
     * Get the value of a cell.
     * @param value the new value of the cell
     * @param row the row of the cell
     * @param column the column of the cell
     */
    @CheckReturnValue
    public void setCell(Object value, int row, int column);
    
    /**
     * Set the value of a cell.
     * If the table has both rows and columns, the value of the first column
     * will be returned.
     * @param value the new value of the cell
     * @param row the row of the cell
     */
    default public void setCell(Object value, String row) {
        int rowNumber = getRowNumber(row);
        if (rowNumber == -1) {
            throw new IllegalArgumentException("Row '"+row+"' is not found");
        }
        
        setCell(value, rowNumber, 1);
    }
    
    /**
     * Set the value of a cell.
     * @param value the new value of the cell
     * @param row the row of the cell. If this string is a name of a row, that
     * row is used. If it's not a name of a row, but an integer value, that
     * index is used, where row 0 is the name of the row.
     * @param column the column of the cell. If this string is a name of a
     * column, that column is used. If it's not a name of a column, but an
     * integer value, that index is used, where column 0 is the name of the column.
     */
    default public void setCell(Object value, String row, String column) {
        int rowNumber = getRowNumber(row);
        if (rowNumber == -1) {
            throw new IllegalArgumentException("Row '"+row+"' is not found");
        }
        
        int columnNumber = getColumnNumber(column);
        if (columnNumber == -1) {
            throw new IllegalArgumentException("Column '"+column+"' is not found");
        }
        
        setCell(value, rowNumber, columnNumber);
    }
    
    /**
     * Get the number of rows in the table.
     * @return the number of rows
     */
    public int numRows();
    
    /**
     * Get the number of columns in the table.
     * @return the number of columns
     */
    public int numColumns();

    /**
     * Get the row number by name of row.
     * @param rowName the name of the row. If there is no row with this name,
     * and rowName is a positive integer, that row number will be returned.
     * @return the row number or -1 if there is no row with that name
     */
    public int getRowNumber(String rowName);
    
    /**
     * Get the row number by name of row.
     * @param columnName the name of the column. If there is no column with
     * this name, and columnName is a positive integer, that column number will
     * be returned.
     * @return the column number or -1 if there is no column with that name
     */
    public int getColumnNumber(String columnName);
    
    /**
     * Store the table to a CSV file.
     * @param file the CSV file
     * @throws java.io.FileNotFoundException if file not found
     */
    public void storeTableAsCSV(@Nonnull File file)
            throws FileNotFoundException;

    /**
     * Store the table to a CSV file.
     * If system name and/or user name is not null, these values are used
     * instead of the tables own system name and user name. If no system name
     * and user name is given and the table is anonymous, no system name and
     * user name is stored in the file.
     * @param file the CSV file
     * @param systemName the system name of the table
     * @param userName the user name of the table
     * @throws java.io.FileNotFoundException if file not found
     */
    public void storeTableAsCSV(
            @Nonnull File file,
            @CheckForNull String systemName, @CheckForNull String userName)
            throws FileNotFoundException;

}

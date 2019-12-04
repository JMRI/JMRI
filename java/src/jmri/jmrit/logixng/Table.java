package jmri.jmrit.logixng;

import java.io.File;
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

    /*.*
     * If the method getCell(row) is called with the parameter COLUMN_NAMES,
     * a table with the column names will be returned.
     * If the table doesn't have any column names, integer values from 0 to n-1
     * will be returned, where n = number of columns in the table.
     *./
    public static final String COLUMN_NAMES = "__columns__";
    
    /*.*
     * If the method getCell(row) is called with the parameter ROW_NAMES,
     * a table with the row names will be returned.
     * If the table doesn't have any row names, integer values from 0 to n-1
     * will be returned, where n = number of rows in the table.
     *./
    public static final String ROW_NAMES = "__rows__";
    
    /*.*
     * If the method getCell(row) or getCell(row,column) is called with the
     * value ALL_ITEMS, all items in that row and/or column will be returned.
     *./
    public static final String ALL_ITEMS = "*";
*/    
    /**
     * Get the value of a cell.
     * If the table has both rows and columns, the value of the first column
     * will be returned.
     * @param row the row of the cell or null if all rows should be returned
     * @return the value of the cell
     */
    @CheckReturnValue
    default public Object getCell(@Nonnull String row) {
        return getCell(row, Integer.toString(1));
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
    public Object getCell(@Nonnull String row, @Nonnull String column);
    
    /**
     * Set the value of a cell.
     * If the table has both rows and columns, the value of the first column
     * will be returned.
     * @param value the new value of the cell
     * @param row the row of the cell
     */
    default public void setCell(Object value, String row) {
        setCell(value, row, Integer.toString(1));
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
    public void setCell(Object value, String row, String column);
    
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
     * Store the table to a CSV file.
     * @param file the CSV file
     */
    public void storeTableAsCSV(@Nonnull File file);

    /**
     * Store the table to a CSV file.
     * If system name and/or user name is not null, these values are used
     * instead of the tables own system name and user name. If no system name
     * and user name is given and the table is anonymous, no system name and
     * user name is stored in the file.
     * @param file the CSV file
     * @param systemName the system name of the table
     * @param userName the user name of the table
     */
    public void storeTableAsCSV(
            @Nonnull File file,
            @CheckForNull String systemName, @CheckForNull String userName);

}

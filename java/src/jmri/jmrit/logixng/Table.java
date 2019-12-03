package jmri.jmrit.logixng;

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
     * If the method getCell(row) is called with the parameter COLUMN_NAMES,
     * a table with the column names will be returned.
     * If the table doesn't have any column names, integer values from 0 to n-1
     * will be returned, where n = number of columns in the table.
     */
    public static final String COLUMN_NAMES = "__columns__";
    
    /**
     * If the method getCell(row) is called with the parameter ROW_NAMES,
     * a table with the row names will be returned.
     * If the table doesn't have any row names, integer values from 0 to n-1
     * will be returned, where n = number of rows in the table.
     */
    public static final String ROW_NAMES = "__rows__";
    
    /**
     * If the method getCell(row) or getCell(row,column) is called with the
     * value ALL_ITEMS, all items in that row and/or column will be returned.
     */
    public static final String ALL_ITEMS = "*";
    
    /**
     * Get the value of a cell.
     * If the table has both rows and columns, the value of the first column
     * will be returned.
     * @param row the row of the cell or null if all rows should be returned
     * @return the value of the cell
     */
    @CheckReturnValue
    default public Object get(@CheckForNull String row) { return get(row, null); }
    
    /**
     * Get the value of a cell.
     * @param row the row of the cell. If this value is an integer, it will be
     * used as an index, where row 0 is the name of the row. If this value is
     * null, all rows are returned as an anonymous table
     * @param column the column of the cell If this value is an integer, it will be
     * used as an index, where column 0 is the name of the column. If this value
     * is null, all collumns are returned as an anonymous table
     * @return the value of the cell
     */
    public Object get(@CheckForNull String row, @CheckForNull String column);
    
    /**
     * Set the value of a cell.
     * If the table has both rows and columns, the value of the first column
     * will be returned.
     * @param value the new value of the cell
     * @param row the row of the cell
     */
    public void setCell(Object value, String row);
    
    /**
     * Set the value of a cell.
     * @param value the new value of the cell
     * @param row the row of the cell. If this value is an integer, it will be
     * used as an index, where row 0 is the name of the row.
     * @param column the column of the cell If this value is an integer, it will be
     * used as an index, where column 0 is the name of the column.
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

}

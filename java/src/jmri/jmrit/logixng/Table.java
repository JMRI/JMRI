package jmri.jmrit.logixng;

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
     * @param row the row of the cell
     * @return the value of the cell
     */
    public Object getCell(String row);
    
    /**
     * Get the value of a cell.
     * @param row the row of the cell. If this value is an integer, it will be
     * used as an index, where row 0 is the name of the row.
     * @param column the column of the cell If this value is an integer, it will be
     * used as an index, where column 0 is the name of the column.
     * @return the value of the cell
     */
    public Object getCell(String row, String column);
    
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

}

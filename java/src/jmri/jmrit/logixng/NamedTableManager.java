package jmri.jmrit.logixng;

import jmri.Manager;

/**
 * A manager for a NamedTable
 */
public interface NamedTableManager extends Manager<NamedTable> {

    /**
     * Create a new anonymous table.
     * This table is not stored in the manager.
     * @param numRows number of rows in the table
     * @param numColumns number of columns in the table
     * @return the new table
     */
    public Table newTable(int numRows, int numColumns);
    
    /**
     * Create a new named table.
     * This table is stored in the manager.
     * @param systemName the system name of the table
     * @param userName the user name of the table, or null if no user name
     * @param numRows number of rows in the table
     * @param numColumns number of columns in the table
     * @return the new table
     */
    public Table newTable(String systemName, String userName, int numRows, int numColumns);
    
    /**
     * Load a table from a CSV finle.
     * @param filename the filename of the CSV file
     * @return the loaded table
     */
    public NamedTable loadTableFromCSV(String filename);
    
    /**
     * Store a table from to CSV finle.
     * @param filename the filename of the CSV file
     * @param table the table to store
     */
    public void storeTableAsCSV(String filename, Table table);
    
}

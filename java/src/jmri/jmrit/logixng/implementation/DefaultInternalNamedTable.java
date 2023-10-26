package jmri.jmrit.logixng.implementation;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * The default implementation of a NamedTable
 * 
 * @author Daniel Bergqvist 2018
 */
public class DefaultInternalNamedTable extends AbstractNamedTable {

    /**
     * Create a new named table.
     * @param sys the system name
     * @param user the user name or null if no user name
     * @param numRows the number or rows in the table
     * @param numColumns the number of columns in the table
     */
    public DefaultInternalNamedTable(
            @Nonnull String sys, @CheckForNull String user,
            int numRows, int numColumns)
            throws BadUserNameException, BadSystemNameException {
        super(sys,user,numRows,numColumns);
    }
    
    /**
     * Create a new named table with an existing array of cells.
     * Row 0 has the column names and column 0 has the row names.
     * @param systemName the system name
     * @param userName the user name
     * @param data the data in the table. Note that this data is not copied to
     * an new array but used by the table as is.
     */
    public DefaultInternalNamedTable(
            @Nonnull String systemName, @CheckForNull String userName,
            @Nonnull Object[][] data)
            throws BadUserNameException, BadSystemNameException {
        super(systemName,userName,data);
    }
    
}

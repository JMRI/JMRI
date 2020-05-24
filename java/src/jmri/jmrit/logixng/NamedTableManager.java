package jmri.jmrit.logixng;

import java.io.File;
import java.io.IOException;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import jmri.Manager;
import jmri.NamedBean;

/**
 * A manager for a NamedTable
 * 
 * @author Dave Duchamp       Copyright (C) 2007
 * @author Daniel Bergqvist   Copyright (C) 2018
 */
public interface NamedTableManager extends Manager<NamedTable> {

    /**
     * Create a new anonymous table.
     * This table is not stored in the manager.
     * @param numRows number of rows in the table
     * @param numColumns number of columns in the table
     * @return the new table
     */
    public AnonymousTable newAnonymousTable(int numRows, int numColumns);
    
    /**
     * Create a new named table.
     * This table is stored in the manager.
     * @param systemName the system name of the table
     * @param userName the user name of the table, or null if no user name
     * @param numRows number of rows in the table
     * @param numColumns number of columns in the table
     * @return the new table
     */
    public NamedTable newTable(String systemName, String userName, int numRows, int numColumns);
    
    /**
     * Load a table from a CSV text.
     * @param text the CSV text
     * @return the loaded table
     */
    public NamedTable loadTableFromCSV(@Nonnull String text)
            throws NamedBean.BadUserNameException, NamedBean.BadSystemNameException;
    
    /**
     * Load a table from a CSV file.
     * @param file the CSV file
     * @return the loaded table
     * @throws java.io.IOException on I/O error
     */
    public NamedTable loadTableFromCSV(@Nonnull File file)
            throws NamedBean.BadUserNameException, NamedBean.BadSystemNameException, IOException;
    
    /**
     * Load a table from a CSV finle.
     * @param file the CSV file
     * @param sys the system name of the new table
     * @param user the user name of the new table or null if no user name
     * @return the loaded table
     * @throws java.io.IOException in case of an exception
     */
    public NamedTable loadTableFromCSV(
            @Nonnull File file,
            @Nonnull String sys, @CheckForNull String user)
            throws NamedBean.BadUserNameException, NamedBean.BadSystemNameException, IOException;
    
    /**
     * Locate via user name, then system name if needed. Does not create a new
     * one if nothing found
     *
     * @param name User name or system name to match
     * @return null if no match found
     */
    public NamedTable getNamedTable(String name);
    
    /** {@inheritDoc} */
    @Override
    public NamedTable getByUserName(String name);
    
    /** {@inheritDoc} */
    @Override
    public NamedTable getBySystemName(String name);
    
    /**
     * Create a new system name for a LogixNG.
     * @return a new system name
     */
    public String getAutoSystemName();
    
    /**
     * {@inheritDoc}
     * 
     * The sub system prefix for the NamedTableManager is
     * {@link #getSystemNamePrefix() } and "T";
     */
    @Override
    public default String getSubSystemNamePrefix() {
        return getSystemNamePrefix() + "T";
    }
    
    /**
     * Delete NamedTable by removing it from the manager.
     *
     * @param x the NamedTable to delete
     */
    void deleteNamedTable(NamedTable x);

}

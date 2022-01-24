package jmri.jmrit.logixng;

import java.io.*;
import java.util.Locale;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.*;

import org.apache.commons.csv.CSVFormat;

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
     * Create a new CSV table.
     * This table is stored in the manager but it's contents does only exists
     * in the CSV file. If the CSV file is changed, the contents will be read
     * on the next start of the program.
     * @param systemName the system name of the table
     * @param userName the user name of the table, or null if no user name
     * @param fileName the file name of the CSV file
     * @param csvFormat the format of the CSV file
     * @return the new table
     */
    public NamedTable newCSVTable(
            @Nonnull String systemName,
            @CheckForNull String userName,
            @Nonnull String fileName,
            @Nonnull CSVFormat csvFormat);
    
    /**
     * Create a new CSV table.
     * This table is stored in the manager but it's contents does only exists
     * in the CSV file. If the CSV file is changed, the contents will be read
     * on the next start of the program.
     * @param systemName the system name of the table
     * @param userName the user name of the table, or null if no user name
     * @param fileName the file name of the CSV file
     * @param predefinedCsvFormat the format of the CSV file
     * @return the new table
     */
    public NamedTable newCSVTable(
            @Nonnull String systemName,
            @CheckForNull String userName,
            @Nonnull String fileName,
            @Nonnull CSVFormat.Predefined predefinedCsvFormat);
    
    /**
     * Create a new internal named table.
     * This table is stored in the manager together with its contents. Note
     * that a big table will take a lot of space in the panel file since the
     * storage of table data has a lot of overhead. For larger tables, a CSV
     * table is recommended.
     * @param systemName the system name of the table
     * @param userName the user name of the table, or null if no user name
     * @param numRows number of rows in the table
     * @param numColumns number of columns in the table
     * @return the new table
     */
    public NamedTable newInternalTable(String systemName, String userName, int numRows, int numColumns);
    
    /**
     * Load a table from a CSV text.
     * @param sys the system name of the new table
     * @param user the user name of the new table or null if no user name
     * @param text the CSV text
     * @param csvFormat the format of the CSV text
     * @return the loaded table
     * @throws java.io.IOException in case of an exception
     */
    public NamedTable loadTableFromCSVData(
            @Nonnull String sys,
            @CheckForNull String user,
            @Nonnull String text,
            @Nonnull CSVFormat csvFormat)
            throws NamedBean.BadUserNameException, NamedBean.BadSystemNameException, IOException;
    
    /**
     * Load a table from a CSV text.
     * @param sys the system name of the new table
     * @param user the user name of the new table or null if no user name
     * @param text the CSV text
     * @param predefinedCsvFormat the format of the CSV text
     * @return the loaded table
     * @throws java.io.IOException in case of an exception
     */
    public NamedTable loadTableFromCSVData(
            @Nonnull String sys,
            @CheckForNull String user,
            @Nonnull String text,
            @Nonnull CSVFormat.Predefined predefinedCsvFormat)
            throws NamedBean.BadUserNameException, NamedBean.BadSystemNameException, IOException;
    
    /**
     * Load a table from a CSV finle.
     * @param sys the system name of the new table
     * @param user the user name of the new table or null if no user name
     * @param fileName the file name of the CSV table
     * @param csvFormat the format of the CSV file
     * @return the loaded table
     * @throws java.io.IOException in case of an exception
     */
    public NamedTable loadTableFromCSV(
            @Nonnull String sys,
            @CheckForNull String user,
            @Nonnull String fileName,
            @Nonnull CSVFormat csvFormat)
            throws NamedBean.BadUserNameException, NamedBean.BadSystemNameException, IOException;
    
    /**
     * Load a table from a CSV finle.
     * @param sys the system name of the new table
     * @param user the user name of the new table or null if no user name
     * @param fileName the file name of the CSV table
     * @param predefinedCsvFormat the format of the CSV file
     * @return the loaded table
     * @throws java.io.IOException in case of an exception
     */
    public NamedTable loadTableFromCSV(
            @Nonnull String sys,
            @CheckForNull String user,
            @Nonnull String fileName,
            @Nonnull CSVFormat.Predefined predefinedCsvFormat)
            throws NamedBean.BadUserNameException, NamedBean.BadSystemNameException, IOException;
    
    /**
     * Load a table from a CSV finle.
     * @param sys the system name of the new table
     * @param user the user name of the new table or null if no user name
     * @param file the CSV file
     * @param csvFormat the format of the CSV file
     * @return the loaded table
     * @throws java.io.IOException in case of an exception
     */
    public NamedTable loadTableFromCSV(
            @Nonnull String sys,
            @CheckForNull String user,
            @Nonnull File file,
            @Nonnull CSVFormat csvFormat)
            throws NamedBean.BadUserNameException, NamedBean.BadSystemNameException, IOException;
    
    /**
     * Load a table from a CSV finle.
     * @param sys the system name of the new table
     * @param user the user name of the new table or null if no user name
     * @param file the CSV file
     * @param predefinedCsvFormat the format of the CSV file
     * @return the loaded table
     * @throws java.io.IOException in case of an exception
     */
    public NamedTable loadTableFromCSV(
            @Nonnull String sys,
            @CheckForNull String user,
            @Nonnull File file,
            @Nonnull CSVFormat.Predefined predefinedCsvFormat)
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

    /**
     * Print the tree to a stream.
     * 
     * @param writer the stream to print the tree to
     * @param indent the indentation of each level
     */
    public void printTree(PrintWriter writer, String indent);
    
    /**
     * Print the tree to a stream.
     * 
     * @param locale The locale to be used
     * @param writer the stream to print the tree to
     * @param indent the indentation of each level
     */
    public void printTree(Locale locale, PrintWriter writer, String indent);

}

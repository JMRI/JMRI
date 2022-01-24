package jmri.jmrit.logixng.implementation;

import java.io.*;
import java.util.*;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Manager;
import jmri.NamedBean;
import jmri.NamedBean.BadUserNameException;
import jmri.NamedBean.BadSystemNameException;
import jmri.implementation.AbstractNamedBean;
import jmri.jmrit.logixng.AnonymousTable;
import jmri.jmrit.logixng.NamedTable;
import jmri.jmrit.logixng.NamedTableManager;
import jmri.util.FileUtil;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 * The default implementation of a NamedTable
 * 
 * @author Daniel Bergqvist 2018
 */
public abstract class AbstractNamedTable extends AbstractNamedBean implements NamedTable {

    private int _state = NamedBean.UNKNOWN;
    protected final AnonymousTable _internalTable;
    
    /**
     * Create a new named table.
     * @param sys the system name
     * @param user the user name or null if no user name
     * @param numRows the number or rows in the table
     * @param numColumns the number of columns in the table
     */
    public AbstractNamedTable(
            @Nonnull String sys, @CheckForNull String user,
            int numRows, int numColumns)
            throws BadUserNameException, BadSystemNameException {
        super(sys,user);
        _internalTable = new DefaultAnonymousTable(numRows, numColumns);
    }
    
    /**
     * Create a new named table with an existing array of cells.
     * Row 0 has the column names and column 0 has the row names.
     * @param systemName the system name
     * @param userName the user name
     * @param data the data in the table. Note that this data is not copied to
     * an new array but used by the table as is.
     */
    public AbstractNamedTable(
            @Nonnull String systemName, @CheckForNull String userName,
            @Nonnull Object[][] data)
            throws BadUserNameException, BadSystemNameException {
        super(systemName,userName);
        
        // Do this test here to ensure all the tests are using correct system names
        Manager.NameValidity isNameValid = InstanceManager.getDefault(NamedTableManager.class).validSystemNameFormat(mSystemName);
        if (isNameValid != Manager.NameValidity.VALID) {
            throw new IllegalArgumentException("system name is not valid");
        }
        _internalTable = new DefaultAnonymousTable(data);
    }
    
    /**
     * Create a new named table with an existing array of cells.
     * Row 0 has the column names and column 0 has the row names.
     * @param systemName the system name
     * @param userName the user name
     * @param fileName the file name of the CSV table
     * @param data the data in the table. Note that this data is not copied to
     * an new array but used by the table as is.
     */
    public AbstractNamedTable(
            @Nonnull String systemName, @CheckForNull String userName,
            @Nonnull String fileName,
            @Nonnull Object[][] data)
            throws BadUserNameException, BadSystemNameException {
        super(systemName,userName);
        
        // Do this test here to ensure all the tests are using correct system names
        Manager.NameValidity isNameValid = InstanceManager.getDefault(NamedTableManager.class).validSystemNameFormat(mSystemName);
        if (isNameValid != Manager.NameValidity.VALID) {
            throw new IllegalArgumentException("system name is not valid");
        }
        _internalTable = new DefaultAnonymousTable(data);
    }
    
    @Nonnull
    private static NamedTable loadFromCSV(
            @Nonnull String systemName,
            @CheckForNull String userName,
            @CheckForNull String fileName,
            @Nonnull Reader reader,
            @CheckForNull CSVFormat csvFormat,
            @CheckForNull CSVFormat.Predefined predefinedCsvFormat,
            boolean registerInManager)
            throws NamedBean.BadUserNameException, NamedBean.BadSystemNameException, IOException {
        
        NamedTableManager manager = InstanceManager.getDefault(NamedTableManager.class);
        
        if (userName != null && userName.isEmpty()) userName = null;
        
        CSVFormat format = csvFormat;
        if (format == null) {
            if (predefinedCsvFormat == null) {
                throw new IllegalArgumentException("Both csvFormat and predefinedCsvFormat must not be null");
            }
            format = predefinedCsvFormat.getFormat();
        } else if (predefinedCsvFormat != null) {
            throw new IllegalArgumentException("Either csvFormat or predefinedCsvFormat must not be null");
        }
        CSVParser csvFile = new CSVParser(reader, format);
        List<CSVRecord> records = csvFile.getRecords();
        
        int numRows = records.size();
        
        String[][] csvCells = new String[numRows][];
        
        int numColumns = 0;
        int rowCnt = 0;
        for (CSVRecord record : records) {
            if (record.size() > 0) {
                if (numColumns < record.size()) numColumns = record.size();
                csvCells[rowCnt] = new String[numColumns];
                for (int col=0; col < record.size(); col++) {
                    csvCells[rowCnt][col] = record.get(col);
                }
            }
            rowCnt++;
        }
        
        // Ensure all rows have same number of columns
        for (int rowCount = 0; rowCount < numRows; rowCount++) {
            Object[] cells = csvCells[rowCount];
            if (cells.length < numColumns) {
                String[] newCells = new String[numColumns];
                System.arraycopy(cells, 0, newCells, 0, cells.length);
                csvCells[rowCount] = newCells;
                for (int i=cells.length; i < numColumns; i++) newCells[i] = "";
                csvCells[rowCount] = newCells;
            }
        }
        
        NamedTable table = new DefaultCsvNamedTable(systemName, userName, fileName, csvCells, csvFormat, predefinedCsvFormat);
        
        if (registerInManager) manager.register(table);
        
        return table;
    }
    
    @Nonnull
    public static NamedTable loadTableFromCSV_Text(
            @Nonnull String systemName,
            @CheckForNull String userName,
            @Nonnull String text,
            @Nonnull CSVFormat csvFormat,
            boolean registerInManager)
            throws BadUserNameException, BadSystemNameException, IOException {
        
        StringReader reader = new StringReader(text);
        return loadFromCSV(systemName, userName, null, reader, csvFormat, null, registerInManager);
    }
    
    @Nonnull
    public static NamedTable loadTableFromCSV_Text(
            @Nonnull String systemName,
            @CheckForNull String userName,
            @Nonnull String text,
            @Nonnull CSVFormat.Predefined predefinedCsvFormat,
            boolean registerInManager)
            throws BadUserNameException, BadSystemNameException, IOException {
        
        StringReader reader = new StringReader(text);
        return loadFromCSV(systemName, userName, null, reader, null, predefinedCsvFormat, registerInManager);
    }
    
    @Nonnull
    public static NamedTable loadTableFromCSV_File(
            @Nonnull String systemName,
            @CheckForNull String userName,
            @Nonnull String fileName,
            @Nonnull CSVFormat csvFormat,
            boolean registerInManager)
            throws NamedBean.BadUserNameException, NamedBean.BadSystemNameException, IOException {
        
        return loadFromCSV(
                systemName,
                userName,
                fileName,
                new FileReader(FileUtil.getFile(fileName)),
                csvFormat,
                null,
                registerInManager);
    }
    
    @Nonnull
    public static NamedTable loadTableFromCSV_File(
            @Nonnull String systemName,
            @CheckForNull String userName,
            @Nonnull String fileName,
            @Nonnull CSVFormat.Predefined predefinedCsvFormat,
            boolean registerInManager)
            throws NamedBean.BadUserNameException, NamedBean.BadSystemNameException, IOException {
        
        return loadFromCSV(
                systemName,
                userName,
                fileName,
                new FileReader(FileUtil.getFile(fileName)),
                null,
                predefinedCsvFormat,
                registerInManager);
    }
    
    @Nonnull
    public static NamedTable loadTableFromCSV_File(
            @Nonnull String systemName,
            @CheckForNull String userName,
            @Nonnull File file,
            @Nonnull CSVFormat csvFormat,
            boolean registerInManager)
            throws NamedBean.BadUserNameException, NamedBean.BadSystemNameException, IOException {
        
        return loadFromCSV(systemName, userName, file.getPath(), new FileReader(file), csvFormat, null, registerInManager);
    }
    
    @Nonnull
    public static NamedTable loadTableFromCSV_File(
            @Nonnull String systemName,
            @CheckForNull String userName,
            @Nonnull File file,
            @Nonnull CSVFormat.Predefined predefinedCsvFormat,
            boolean registerInManager)
            throws NamedBean.BadUserNameException, NamedBean.BadSystemNameException, IOException {
        
        return loadFromCSV(systemName, userName, file.getPath(), new FileReader(file), null, predefinedCsvFormat, registerInManager);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void storeTableAsCSV(@Nonnull File file)
            throws FileNotFoundException {
        _internalTable.storeTableAsCSV(file, getSystemName(), getUserName());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void storeTableAsCSV(
            @Nonnull File file,
            @CheckForNull String systemName, @CheckForNull String userName)
            throws FileNotFoundException {
        
        _internalTable.storeTableAsCSV(file, systemName, userName);
    }
    
    @Override
    public void setState(int s) throws JmriException {
        _state = s;
    }
    
    @Override
    public int getState() {
        return _state;
    }
    
    @Override
    public String getBeanType() {
        return Bundle.getMessage("BeanNameTable");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Object getCell(int row, int column) {
        return _internalTable.getCell(row, column);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setCell(Object value, int row, int column) {
        _internalTable.setCell(value, row, column);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int numRows() {
        return _internalTable.numRows();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int numColumns() {
        return _internalTable.numColumns();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getRowNumber(String rowName) {
        return _internalTable.getRowNumber(rowName);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getColumnNumber(String columnName) {
        return _internalTable.getColumnNumber(columnName);
    }
/*    
    protected void insertColumn(int col) {
        _internalTable.insertColumn(col);
    }
    
    protected void deleteColumn(int col) {
        _internalTable.deleteColumn(col);
    }
    
    protected void insertRow(int row) {
        _internalTable.insertRow(row);
    }
    
    protected void deleteRow(int row) {
        _internalTable.deleteRow(row);
    }
*/
}

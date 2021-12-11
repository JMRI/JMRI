package jmri.jmrit.logixng.implementation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

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
            @Nonnull String systemName, @CheckForNull String userName,
            @CheckForNull String fileName,
            @Nonnull List<String> lines,
            boolean registerInManager)
            throws NamedBean.BadUserNameException, NamedBean.BadSystemNameException {
        
        NamedTableManager manager = InstanceManager.getDefault(NamedTableManager.class);
        
        if (userName != null && userName.isEmpty()) userName = null;
        
        // First row is column names.
        int numRows = lines.size()-1;
        
        // If the last row is empty string, ignore it.
        if (lines.get(lines.size()-1).isEmpty()) numRows--;
        
        int numColumns = 0;
        
        String[][] csvCells = new String[numRows+1][];
        for (int rowCount = 0; rowCount < numRows+1; rowCount++) {
            String[] columns = lines.get(rowCount).split("\t");
            if (numColumns < columns.length) numColumns = columns.length;
            csvCells[rowCount] = columns;
        }
        
        // Ensure all rows have same number of columns
        for (int rowCount = 0; rowCount < numRows+1; rowCount++) {
            Object[] cells = csvCells[rowCount];
            if (cells.length < numColumns) {
                String[] newCells = new String[numColumns];
                System.arraycopy(cells, 0, newCells, 0, cells.length);
                csvCells[rowCount] = newCells;
                for (int i=cells.length; i < numColumns; i++) newCells[i] = "";
                csvCells[rowCount] = newCells;
            }
        }
        
        NamedTable table = new DefaultCsvNamedTable(systemName, userName, fileName, csvCells);
        
        if (registerInManager) manager.register(table);
        
        return table;
    }
    
    @Nonnull
    public static NamedTable loadTableFromCSV_Text(
            @Nonnull String systemName, @CheckForNull String userName,
            @Nonnull String text,
            boolean registerInManager)
            throws BadUserNameException, BadSystemNameException {
        
        List<String> lines = Arrays.asList(text.split("\\r?\\n",-1));
        return loadFromCSV(systemName, userName, null, lines, registerInManager);
    }
    
    @Nonnull
    public static NamedTable loadTableFromCSV_File(
            @Nonnull String systemName, @CheckForNull String userName,
            @Nonnull String fileName, boolean registerInManager)
            throws NamedBean.BadUserNameException, NamedBean.BadSystemNameException, IOException {
        
        List<String> lines = Files.readAllLines(FileUtil.getFile(fileName).toPath(), StandardCharsets.UTF_8);
        return loadFromCSV(systemName, userName, fileName, lines, registerInManager);
    }
    
    @Nonnull
    public static NamedTable loadTableFromCSV_File(
            @Nonnull String systemName, @CheckForNull String userName,
            @Nonnull File file, boolean registerInManager)
            throws NamedBean.BadUserNameException, NamedBean.BadSystemNameException, IOException {
        
        List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
        return loadFromCSV(systemName, userName, file.getPath(), lines, registerInManager);
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
//        return Manager.LOGIXNGS;
//        return NamedTable.class;
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

package jmri.jmrit.logixng.implementation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.NamedBean;
import jmri.NamedBean.BadUserNameException;
import jmri.NamedBean.BadSystemNameException;
import jmri.implementation.AbstractNamedBean;
import jmri.jmrit.logixng.AnonymousTable;
import jmri.jmrit.logixng.NamedTable;
import jmri.jmrit.logixng.NamedTableManager;

/**
 * The default implementation of a NamedTable
 */
public class DefaultNamedTable extends AbstractNamedBean implements NamedTable {

    private int _state = NamedBean.UNKNOWN;
    private final AnonymousTable _internalTable;
    
    /**
     * Create a new named table.
     * @param sys the system name
     * @param user the user name or null if no user name
     * @param numRows the number or rows in the table
     * @param numColumns the number of columns in the table
     */
    public DefaultNamedTable(
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
    public DefaultNamedTable(
            @Nonnull String systemName, @CheckForNull String userName,
            @Nonnull Object[][] data)
            throws BadUserNameException, BadSystemNameException {
        super(systemName,userName);
        _internalTable = new DefaultAnonymousTable(data);
    }
    
    private static NamedTable loadFromCSV(
            @Nonnull File file,
            @CheckForNull String systemName, @CheckForNull String userName)
            throws NamedBean.BadUserNameException, NamedBean.BadSystemNameException, IOException {
        
        NamedTableManager manager = InstanceManager.getDefault(NamedTableManager.class);
        
        List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
        
        if (systemName == null && userName == null) {
            String[] firstRow = lines.get(0).split("\t");
            if (systemName == null) systemName = manager.getAutoSystemName();
            System.out.format("firstRow: %s, %s%n", firstRow[0], firstRow[1]);
        }
        
        // First row is system name and user name. Second row is column names.
        int numRows = lines.size() - 2;
        int numColumns = 0;
        
        String[][] csvCells = new String[numRows+1][];
        for (int rowCount = 1; rowCount < lines.size(); rowCount++) {
            String[] columns = lines.get(rowCount).split("\t");
            if (numColumns+1 < columns.length) numColumns = columns.length-1;
            csvCells[rowCount-1] = columns;
        }
        
        for (int rowCount = 1; rowCount < lines.size(); rowCount++) {
//            String[] cells = lines.get(rowCount).split("\t");
            Object[] cells = csvCells[rowCount-1];
            if (cells.length <= numColumns) {
                String[] newCells = new String[numColumns+1];
                System.arraycopy(cells, 0, newCells, 0, cells.length);
                csvCells[rowCount-1] = newCells;
                for (int i=cells.length; i <= numColumns; i++) newCells[i] = "DANIEL";
            }
        }
        
        NamedTable table = new DefaultNamedTable(systemName, userName, csvCells);
        manager.register(table);
        
        return table;
    }
    
    public static NamedTable loadTableFromCSV_File(@Nonnull File file)
            throws BadUserNameException, BadSystemNameException, IOException {
        
        return loadFromCSV(file, null, null);
/*        
        String systemName = null;
        String userName = null;
        
        List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
        
        String[] firstRow = lines.get(0).split("\t");
        System.out.format("firstRow: %s, %s%n", firstRow[0], firstRow[1]);
        
        // First row is system name and user name. Second row is column names.
        int numRows = lines.size() - 2;
        int numColumns = 0;
        
        String[][] csvCells = new String[numRows+1][];
        for (int rowCount = 1; rowCount < lines.size(); rowCount++) {
            String[] columns = lines.get(rowCount).split("\t");
            if (numColumns+1 < columns.length) numColumns = columns.length-1;
            csvCells[rowCount-1] = columns;
        }
        
        for (int rowCount = 1; rowCount < lines.size(); rowCount++) {
//            String[] cells = lines.get(rowCount).split("\t");
            Object[] cells = csvCells[rowCount-1];
            if (cells.length <= numColumns) {
                String[] newCells = new String[numColumns+1];
                System.arraycopy(cells, 0, newCells, 0, cells.length);
                csvCells[rowCount-1] = newCells;
                for (int i=cells.length; i <= numColumns; i++) newCells[i] = "DANIEL";
            }
        }
        
        NamedTableManager manager = InstanceManager.getDefault(NamedTableManager.class);
        if (systemName == null) systemName = manager.getAutoSystemName();
        NamedTable table = new DefaultNamedTable(systemName, userName, csvCells);
        manager.register(table);
        
        return table;
*/
    }
    
    static public NamedTable loadTableFromCSV_File(
            @Nonnull File file,
            @Nonnull String systemName, @CheckForNull String userName)
            throws NamedBean.BadUserNameException, NamedBean.BadSystemNameException, IOException {
        
        return loadFromCSV(file, systemName, userName);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void storeTableAsCSV(@Nonnull File file) {
        storeTableAsCSV(file, getSystemName(), getUserName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void storeTableAsCSV(
            @Nonnull File file,
            @CheckForNull String systemName, @CheckForNull String userName) {
        
        throw new UnsupportedOperationException("Not supported yet.");
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
        return Bundle.getMessage("BeanNameLogixNG");
//        return Manager.LOGIXNGS;
//        return NamedTable.class;
    }

    @Override
    public Object getCell(String row, String column) {
        return _internalTable.getCell(row, column);
    }

    @Override
    public void setCell(Object value, String row, String column) {
        _internalTable.setCell(value, row, column);
    }

    @Override
    public int numRows() {
        return _internalTable.numRows();
    }

    @Override
    public int numColumns() {
        return _internalTable.numColumns();
    }

}

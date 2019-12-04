package jmri.jmrit.logixng.implementation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
    
    public DefaultNamedTable(
            @Nonnull String sys, @CheckForNull String user,
            int numRows, int numColumns)
            throws BadUserNameException, BadSystemNameException {
        super(sys,user);
        _internalTable = new DefaultAnonymousTable(numRows, numColumns);
    }
    
    public NamedTable LoadTableFromCSV_File(@Nonnull File file) throws BadUserNameException, BadSystemNameException, IOException {
        String systemName = null;
        String userName = null;
        int rows = 0;
        int columns = 0;
        Object[][] data = new Object[rows+1][columns+1];
        NamedTable table = new DefaultNamedTable(systemName,userName, rows, columns);
        InstanceManager.getDefault(NamedTableManager.class).register(table);
        return table;
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
    public Object get(String row, String column) {
        return _internalTable.get(row, column);
    }

    @Override
    public void setCell(Object value, String row) {
        _internalTable.setCell(value, row);
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

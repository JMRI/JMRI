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
import jmri.jmrit.logixng.Stack;

/**
 * The default implementation of a NamedTable
 * 
 * @author Daniel Bergqvist 2018
 */
public class DefaultStackTable extends AbstractNamedBean implements Stack {

    static final int INITIAL_SIZE = 100;
    static final int GROW_SIZE = 100;
    
    int _size;
    int _count;
    
    private int _state = NamedBean.UNKNOWN;
    private Object[] _stack = new Object[INITIAL_SIZE];
    
    /**
     * Create a new stack table.
     * @param sys the system name
     * @param user the user name or null if no user name
     */
    public DefaultStackTable(
            @Nonnull String sys, @CheckForNull String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys,user);
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
    
    @Override
    public void push(Object value) {
        if (_count+1 >= _size) {
            Object[] newStack = new Object[_size + GROW_SIZE];
            System.arraycopy(_stack, 0, newStack, 0, _size);
            _stack = newStack;
            _size += GROW_SIZE;
        }
        _stack[_count++] = value;
    }
    
    @Override
    public Object pop() {
        if (_count <= 0) throw new ArrayIndexOutOfBoundsException("Stack is empty");
        return _stack[--_count];
    }

    @Override
    public Object getCell(int row, int column) {
        if (column != 0) throw new ArrayIndexOutOfBoundsException("Column is not 0");
        return _stack[row];
    }

    @Override
    public void setCell(Object value, int row, int column) {
        if (column != 0) throw new ArrayIndexOutOfBoundsException("Column is not 0");
        _stack[row] = value;
    }

    @Override
    public int numRows() {
        return _size;
    }

    @Override
    public int numColumns() {
        return 1;
    }

    @Override
    public int getRowNumber(String rowName) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public int getColumnNumber(String columnName) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void storeTableAsCSV(File file) throws FileNotFoundException {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void storeTableAsCSV(File file, String systemName, String userName) throws FileNotFoundException {
        throw new UnsupportedOperationException("Not supported");
    }
    
}

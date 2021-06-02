package jmri.jmrit.logixng.actions;

import jmri.jmrit.logixng.TableRowOrColumn;

import java.beans.VetoableChangeListener;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.logixng.*;

/**
 * Executes an action when the expression is True.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public class TableForEach extends AbstractDigitalAction
        implements FemaleSocketListener, VetoableChangeListener {

    private NamedBeanHandle<NamedTable> _tableHandle;
    private TableRowOrColumn _tableRowOrColumn = TableRowOrColumn.Row;
    private String _rowOrColumnName = "";
    private String _variableName = "";
    private String _socketSystemName;
    private final FemaleDigitalActionSocket _socket;
    
    public TableForEach(String sys, String user) {
        super(sys, user);
        _socket = InstanceManager.getDefault(DigitalActionManager.class)
                .createFemaleSocket(this, this, "A1");
    }
    
    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        TableForEach copy = new TableForEach(sysName, userName);
        copy.setComment(getComment());
        copy.setTable(_tableHandle);
        copy.setTableRowOrColumn(_tableRowOrColumn);
        return manager.registerAction(copy).deepCopyChildren(this, systemNames, userNames);
    }
    
    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.COMMON;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isExternal() {
        return false;
    }
    
    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {
        
//        System.out.format("TableForEach.execute: %s%n", getLongDescription());
        
        if (_tableHandle == null) {
            log.error("tableHandle is null");
            return;
        }
        if (_rowOrColumnName.isEmpty()) {
            log.error("rowOrColumnName is empty string");
            return;
        }
        if (_variableName == null) {
            log.error("variableName is null");
            return;
        }
        if (!_socket.isConnected()) {
            log.error("socket is not connected");
            return;
        }
        
        SymbolTable symbolTable = getConditionalNG().getSymbolTable();
        
        NamedTable table = _tableHandle.getBean();
        
        if (_tableRowOrColumn == TableRowOrColumn.Row) {
            int row = table.getRowNumber(_rowOrColumnName);
            for (int column=1; column <= table.numColumns(); column++) {
                // If the header is null or empty, treat the row as a comment
                Object header = table.getCell(0, column);
//                System.out.format("Row header: %s%n", header);
                if ((header != null) && (!header.toString().isEmpty())) {
                    symbolTable.setValue(_variableName, table.getCell(row, column));
//                    System.out.format("Variable: %s, value: %s%n", _variableName, table.getCell(row, column));
                    _socket.execute();
                }
            }
        } else {
            int column = table.getRowNumber(_rowOrColumnName);
            for (int row=1; row <= table.numRows(); row++) {
                // If the header is null or empty, treat the row as a comment
                Object header = table.getCell(row, 0);
//                System.out.format("Column header: %s%n", header);
                if ((header != null) && (!header.toString().isEmpty())) {
                    symbolTable.setValue(_variableName, table.getCell(row, column));
//                    System.out.format("Variable: %s, value: %s%n", _variableName, table.getCell(row, column));
                    _socket.execute();
                }
            }
        }
    }

    public void setTable(@Nonnull String tableName) {
        assertListenersAreNotRegistered(log, "setTable");
        NamedTable table = InstanceManager.getDefault(NamedTableManager.class).getNamedTable(tableName);
        if (table != null) {
            setTable(table);
        } else {
            removeTable();
            log.error("turnout \"{}\" is not found", tableName);
        }
    }
    
    public void setTable(@Nonnull NamedBeanHandle<NamedTable> handle) {
        assertListenersAreNotRegistered(log, "setTable");
        _tableHandle = handle;
        InstanceManager.turnoutManagerInstance().addVetoableChangeListener(this);
    }
    
    public void setTable(@Nonnull NamedTable turnout) {
        assertListenersAreNotRegistered(log, "setTable");
        setTable(InstanceManager.getDefault(NamedBeanHandleManager.class)
                .getNamedBeanHandle(turnout.getDisplayName(), turnout));
    }
    
    public void removeTable() {
        assertListenersAreNotRegistered(log, "setTable");
        if (_tableHandle != null) {
            InstanceManager.turnoutManagerInstance().removeVetoableChangeListener(this);
            _tableHandle = null;
        }
    }
    
    public NamedBeanHandle<NamedTable> getTable() {
        return _tableHandle;
    }
    
    /**
     * Get tableRowOrColumn.
     * @return tableRowOrColumn
     */
    public TableRowOrColumn getTableRowOrColumn() {
        return _tableRowOrColumn;
    }
    
    /**
     * Set tableRowOrColumn.
     * @param tableRowOrColumn tableRowOrColumn
     */
    public void setTableRowOrColumn(@Nonnull TableRowOrColumn tableRowOrColumn) {
        _tableRowOrColumn = tableRowOrColumn;
    }
    
    /**
     * Get name of row or column
     * @return name of row or column
     */
    public String getRowOrColumnName() {
        return _rowOrColumnName;
    }
    
    /**
     * Set name of row or column
     * @param rowOrColumnName name of row or column
     */
    public void setRowOrColumnName(@Nonnull String rowOrColumnName) {
        if (rowOrColumnName == null) throw new RuntimeException("Daniel");
        _rowOrColumnName = rowOrColumnName;
    }
    
    /**
     * Get name of local variable
     * @return name of local variable
     */
    public String getLocalVariableName() {
        return _variableName;
    }
    
    /**
     * Set name of local variable
     * @param localVariableName name of local variable
     */
    public void setLocalVariableName(String localVariableName) {
        _variableName = localVariableName;
    }
    
    @Override
    public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        switch (index) {
            case 0:
                return _socket;
                
            default:
                throw new IllegalArgumentException(
                        String.format("index has invalid value: %d", index));
        }
    }

    @Override
    public int getChildCount() {
        return 1;
    }

    @Override
    public void connected(FemaleSocket socket) {
        if (socket == _socket) {
            _socketSystemName = socket.getConnectedSocket().getSystemName();
        } else {
            throw new IllegalArgumentException("unkown socket");
        }
    }

    @Override
    public void disconnected(FemaleSocket socket) {
        if (socket == _socket) {
            _socketSystemName = null;
        } else {
            throw new IllegalArgumentException("unkown socket");
        }
    }

    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "TableForEach_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        return Bundle.getMessage(locale, "TableForEach_Long",
                _tableRowOrColumn.toStringLowerCase(),
                _tableRowOrColumn.getOpposite().toStringLowerCase(),
                _rowOrColumnName,
                getTable() != null ? getTable().getName() : "",
                _variableName,
                _socket.getName());
    }

    public FemaleDigitalActionSocket getSocket() {
        return _socket;
    }

    public String getSocketSystemName() {
        return _socketSystemName;
    }

    public void setSocketSystemName(String systemName) {
        _socketSystemName = systemName;
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        try {
            if ( !_socket.isConnected()
                    || !_socket.getConnectedSocket().getSystemName()
                            .equals(_socketSystemName)) {
                
                String socketSystemName = _socketSystemName;
                _socket.disconnect();
                if (socketSystemName != null) {
                    MaleSocket maleSocket =
                            InstanceManager.getDefault(DigitalActionManager.class)
                                    .getBySystemName(socketSystemName);
                    _socket.disconnect();
                    if (maleSocket != null) {
                        _socket.connect(maleSocket);
                        maleSocket.setup();
                    } else {
                        log.error("cannot load digital action " + socketSystemName);
                    }
                }
            } else {
                _socket.getConnectedSocket().setup();
            }
        } catch (SocketAlreadyConnectedException ex) {
            // This shouldn't happen and is a runtime error if it does.
            throw new RuntimeException("socket is already connected");
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
    }
    
    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
    }
    
    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TableForEach.class);

}

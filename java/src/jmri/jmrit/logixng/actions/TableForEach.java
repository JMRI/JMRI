package jmri.jmrit.logixng.actions;

import java.beans.PropertyVetoException;

import jmri.jmrit.logixng.TableRowOrColumn;

import java.beans.VetoableChangeListener;
import java.util.*;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.ReferenceUtil;
import jmri.jmrit.logixng.util.parser.*;
import jmri.util.TypeConversionUtil;

/**
 * Executes an action when the expression is True.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public class TableForEach extends AbstractDigitalAction
        implements FemaleSocketListener, VetoableChangeListener {

    private NamedBeanAddressing _addressing = NamedBeanAddressing.Direct;
    private NamedBeanHandle<NamedTable> _tableHandle;
    private String _tableReference = "";
    private String _tableLocalVariable = "";
    private String _tableFormula = "";
    private ExpressionNode _tableExpressionNode;
    private NamedBeanAddressing _rowOrColumnAddressing = NamedBeanAddressing.Direct;
    private TableRowOrColumn _tableRowOrColumn = TableRowOrColumn.Row;
    private String _rowOrColumnName = "";
    private String _rowOrColumnReference = "";
    private String _rowOrColumnLocalVariable = "";
    private String _rowOrColumnFormula = "";
    private ExpressionNode _rowOrColumnExpressionNode;
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
        copy.setAddressing(_addressing);
        copy.setTable(_tableHandle);
        copy.setTableReference(_tableReference);
        copy.setTableLocalVariable(_tableLocalVariable);
        copy.setTableFormula(_tableFormula);
        copy.setRowOrColumnAddressing(_rowOrColumnAddressing);
        copy.setRowOrColumn(_tableRowOrColumn);
        copy.setRowOrColumnName(_rowOrColumnName);
        copy.setLocalVariableName(_variableName);
        return manager.registerAction(copy).deepCopyChildren(this, systemNames, userNames);
    }
    
    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.COMMON;
    }

    private String getNewRowOrColumnName() throws JmriException {

        switch (_rowOrColumnAddressing) {
            case Direct:
                return _rowOrColumnName;

            case Reference:
                return ReferenceUtil.getReference(
                        getConditionalNG().getSymbolTable(), _rowOrColumnReference);

            case LocalVariable:
                SymbolTable symbolTable = getConditionalNG().getSymbolTable();
                return TypeConversionUtil
                        .convertToString(symbolTable.getValue(_rowOrColumnLocalVariable), false);

            case Formula:
                return _rowOrColumnExpressionNode != null
                        ? TypeConversionUtil.convertToString(
                                _rowOrColumnExpressionNode.calculate(
                                        getConditionalNG().getSymbolTable()), false)
                        : null;

            default:
                throw new IllegalArgumentException("invalid _rowOrColumnAddressing state: " + _rowOrColumnAddressing.name());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {
        Table table;
        
//        System.out.format("TableForEach.execute: %s%n", getLongDescription());

        switch (_addressing) {
            case Direct:
                table = _tableHandle != null ? _tableHandle.getBean() : null;
                break;

            case Reference:
                String ref = ReferenceUtil.getReference(
                        getConditionalNG().getSymbolTable(), _tableReference);
                table = InstanceManager.getDefault(NamedTableManager.class)
                        .getNamedBean(ref);
                break;

            case LocalVariable:
                SymbolTable symbolTable = getConditionalNG().getSymbolTable();
                table = InstanceManager.getDefault(NamedTableManager.class)
                        .getNamedBean(TypeConversionUtil
                                .convertToString(symbolTable.getValue(_tableLocalVariable), false));
                break;

            case Formula:
                table = _tableExpressionNode != null ?
                        InstanceManager.getDefault(NamedTableManager.class)
                                .getNamedBean(TypeConversionUtil
                                        .convertToString(_tableExpressionNode.calculate(
                                                getConditionalNG().getSymbolTable()), false))
                        : null;
                break;

            default:
                throw new IllegalArgumentException("invalid _addressing state: " + _addressing.name());
        }

//        System.out.format("ActionTurnout.execute: turnout: %s%n", turnout);

        if (table == null) {
//            log.error("turnout is null");
            return;
        }
        
        String rowOrColumnName = getNewRowOrColumnName();

        if (rowOrColumnName == null) {
            log.error("rowOrColumnName is null");
            return;
        }
        if (rowOrColumnName.isEmpty()) {
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
        
        if (_tableRowOrColumn == TableRowOrColumn.Row) {
            int row = table.getRowNumber(rowOrColumnName);
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
            int column = table.getColumnNumber(rowOrColumnName);
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
    
    public void setAddressing(NamedBeanAddressing addressing) throws ParserException {
        _addressing = addressing;
        parseTableFormula();
    }

    public NamedBeanAddressing getAddressing() {
        return _addressing;
    }

    public void setTable(@Nonnull NamedBeanHandle<NamedTable> handle) {
        assertListenersAreNotRegistered(log, "setTable");
        _tableHandle = handle;
        InstanceManager.getDefault(NamedTableManager.class).addVetoableChangeListener(this);
    }
    
    public void setTable(@Nonnull NamedTable turnout) {
        assertListenersAreNotRegistered(log, "setTable");
        setTable(InstanceManager.getDefault(NamedBeanHandleManager.class)
                .getNamedBeanHandle(turnout.getDisplayName(), turnout));
    }
    
    public void removeTable() {
        assertListenersAreNotRegistered(log, "setTable");
        if (_tableHandle != null) {
            InstanceManager.getDefault(NamedTableManager.class).removeVetoableChangeListener(this);
            _tableHandle = null;
        }
    }
    
    public NamedBeanHandle<NamedTable> getTable() {
        return _tableHandle;
    }
    
    public void setTableReference(@Nonnull String reference) {
        if ((! reference.isEmpty()) && (! ReferenceUtil.isReference(reference))) {
            throw new IllegalArgumentException("The reference \"" + reference + "\" is not a valid reference");
        }
        _tableReference = reference;
    }

    public String getTableReference() {
        return _tableReference;
    }

    public void setTableLocalVariable(@Nonnull String localVariable) {
        _tableLocalVariable = localVariable;
    }

    public String getTableLocalVariable() {
        return _tableLocalVariable;
    }

    public void setTableFormula(@Nonnull String formula) throws ParserException {
        _tableFormula = formula;
        parseTableFormula();
    }

    public String getTableFormula() {
        return _tableFormula;
    }

    private void parseTableFormula() throws ParserException {
        if (_addressing == NamedBeanAddressing.Formula) {
            Map<String, Variable> variables = new HashMap<>();

            RecursiveDescentParser parser = new RecursiveDescentParser(variables);
            _tableExpressionNode = parser.parseExpression(_tableFormula);
        } else {
            _tableExpressionNode = null;
        }
    }

    /**
     * Get tableRowOrColumn.
     * @return tableRowOrColumn
     */
    public TableRowOrColumn getRowOrColumn() {
        return _tableRowOrColumn;
    }
    
    /**
     * Set tableRowOrColumn.
     * @param tableRowOrColumn tableRowOrColumn
     */
    public void setRowOrColumn(@Nonnull TableRowOrColumn tableRowOrColumn) {
        _tableRowOrColumn = tableRowOrColumn;
    }
    
    public void setRowOrColumnAddressing(NamedBeanAddressing addressing) throws ParserException {
        _rowOrColumnAddressing = addressing;
        parseRowOrColumnFormula();
    }

    public NamedBeanAddressing getRowOrColumnAddressing() {
        return _rowOrColumnAddressing;
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
    
    public void setRowOrColumnReference(@Nonnull String reference) {
        if ((! reference.isEmpty()) && (! ReferenceUtil.isReference(reference))) {
            throw new IllegalArgumentException("The reference \"" + reference + "\" is not a valid reference");
        }
        _rowOrColumnReference = reference;
    }

    public String getRowOrColumnReference() {
        return _rowOrColumnReference;
    }

    public void setRowOrColumnLocalVariable(@Nonnull String localVariable) {
        _rowOrColumnLocalVariable = localVariable;
    }

    public String getRowOrColumnLocalVariable() {
        return _rowOrColumnLocalVariable;
    }

    public void setRowOrColumnFormula(@Nonnull String formula) throws ParserException {
        _rowOrColumnFormula = formula;
        parseRowOrColumnFormula();
    }

    public String getRowOrColumnFormula() {
        return _rowOrColumnFormula;
    }

    private void parseRowOrColumnFormula() throws ParserException {
        if (_rowOrColumnAddressing == NamedBeanAddressing.Formula) {
            Map<String, Variable> variables = new HashMap<>();

            RecursiveDescentParser parser = new RecursiveDescentParser(variables);
            _rowOrColumnExpressionNode = parser.parseExpression(_rowOrColumnFormula);
        } else {
            _rowOrColumnExpressionNode = null;
        }
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
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        if ("CanDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof NamedTable) {
                if (evt.getOldValue().equals(getTable().getBean())) {
                    throw new PropertyVetoException(getDisplayName(), evt);
                }
            }
        } else if ("DoDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof NamedTable) {
                if (evt.getOldValue().equals(getTable().getBean())) {
                    removeTable();
                }
            }
        }
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
        String namedBean;
        String rowOrColumnName;

        switch (_addressing) {
            case Direct:
                String tableName;
                if (_tableHandle != null) {
                    tableName = _tableHandle.getBean().getDisplayName();
                } else {
                    tableName = Bundle.getMessage(locale, "BeanNotSelected");
                }
                namedBean = Bundle.getMessage(locale, "AddressByDirect", tableName);
                break;

            case Reference:
                namedBean = Bundle.getMessage(locale, "AddressByReference", _tableReference);
                break;

            case LocalVariable:
                namedBean = Bundle.getMessage(locale, "AddressByLocalVariable", _tableLocalVariable);
                break;

            case Formula:
                namedBean = Bundle.getMessage(locale, "AddressByFormula", _tableFormula);
                break;

            default:
                throw new IllegalArgumentException("invalid _addressing state: " + _addressing.name());
        }

        switch (_rowOrColumnAddressing) {
            case Direct:
                rowOrColumnName = Bundle.getMessage(locale, "AddressByDirect", _rowOrColumnName);
                break;

            case Reference:
                rowOrColumnName = Bundle.getMessage(locale, "AddressByReference", _rowOrColumnReference);
                break;

            case LocalVariable:
                rowOrColumnName = Bundle.getMessage(locale, "AddressByLocalVariable", _rowOrColumnLocalVariable);
                break;

            case Formula:
                rowOrColumnName = Bundle.getMessage(locale, "AddressByFormula", _rowOrColumnFormula);
                break;

            default:
                throw new IllegalArgumentException("invalid _rowOrColumnAddressing state: " + _rowOrColumnAddressing.name());
        }

        return Bundle.getMessage(locale, "TableForEach_Long",
                _tableRowOrColumn.getOpposite().toStringLowerCase(),
                _tableRowOrColumn.toStringLowerCase(),
                rowOrColumnName,
                namedBean,
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

package jmri.jmrit.logixng.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.LogixNG_SelectNamedBean;
import jmri.jmrit.logixng.util.ReferenceUtil;
import jmri.jmrit.logixng.util.parser.*;
import jmri.util.TypeConversionUtil;

/**
 * Executes an action when the expression is True.
 *
 * @author Daniel Bergqvist Copyright 2018
 */
public class TableForEach extends AbstractDigitalAction
        implements FemaleSocketListener, PropertyChangeListener {

    private final LogixNG_SelectNamedBean<NamedTable> _selectNamedBean =
            new LogixNG_SelectNamedBean<>(
                    this, NamedTable.class, InstanceManager.getDefault(NamedTableManager.class), this);
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
        _selectNamedBean.copy(copy._selectNamedBean);
        copy.setRowOrColumnAddressing(_rowOrColumnAddressing);
        copy.setRowOrColumn(_tableRowOrColumn);
        copy.setRowOrColumnName(_rowOrColumnName);
        copy.setLocalVariableName(_variableName);
        return manager.registerAction(copy).deepCopyChildren(this, systemNames, userNames);
    }

    public LogixNG_SelectNamedBean<NamedTable> getSelectNamedBean() {
        return _selectNamedBean;
    }

    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.FLOW_CONTROL;
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
        Table table = _selectNamedBean.evaluateNamedBean(getConditionalNG());

        if (table == null) {
            return;
        }

        String rowOrColumnName = getNewRowOrColumnName();

        if (rowOrColumnName == null) {
            log.error("rowOrColumnName is null");
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
            int row = 0;    // Empty row name is header row
            if (!rowOrColumnName.isEmpty()) {
                row = table.getRowNumber(rowOrColumnName);
            }
            for (int column=1; column <= table.numColumns(); column++) {
                // If the header is null or empty, treat the row as a comment
                Object header = table.getCell(0, column);
                if ((header != null) && (!header.toString().isEmpty())) {
                    symbolTable.setValue(_variableName, table.getCell(row, column));
                    try {
                        _socket.execute();
                    } catch (BreakException e) {
                        break;
                    } catch (ContinueException e) {
                        // Do nothing, just catch it.
                    }
                }
            }
        } else {
            int column = 0;    // Empty column name is header column
            if (!rowOrColumnName.isEmpty()) {
                column = table.getColumnNumber(rowOrColumnName);
            }
            for (int row=1; row <= table.numRows(); row++) {
                // If the header is null or empty, treat the row as a comment
                Object header = table.getCell(row, 0);
//                System.out.format("Column header: %s%n", header);
                if ((header != null) && (!header.toString().isEmpty())) {
                    symbolTable.setValue(_variableName, table.getCell(row, column));
//                    System.out.format("Variable: %s, value: %s%n", _variableName, table.getCell(row, column));
                    try {
                        _socket.execute();
                    } catch (BreakException e) {
                        break;
                    } catch (ContinueException e) {
                        // Do nothing, just catch it.
                    }
                }
            }
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
        String namedBean = _selectNamedBean.getDescription(locale);
        String rowOrColumnName;

        switch (_rowOrColumnAddressing) {
            case Direct:
                String name = _rowOrColumnName;
                if (name.isEmpty()) name = Bundle.getMessage("TableForEach_Header");
                rowOrColumnName = Bundle.getMessage(locale, "AddressByDirect", name);
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
                        log.error("cannot load digital action {}", socketSystemName);
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
        _selectNamedBean.registerListeners();
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        _selectNamedBean.unregisterListeners();
    }

    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }

    /** {@inheritDoc} */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        getConditionalNG().execute();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TableForEach.class);

}

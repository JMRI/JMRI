package jmri.jmrit.logixng.expressions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.Bundle;
import jmri.jmrit.logixng.util.ReferenceUtil;
import jmri.jmrit.logixng.util.parser.*;
import jmri.util.TypeConversionUtil;

/**
 * Evaluates the state of a local variable.
 *
 * @author Daniel Bergqvist Copyright 2020
 */
public class ExpressionLocalVariable extends AbstractDigitalExpression
        implements PropertyChangeListener, VetoableChangeListener {

    private String _localVariable;
    private VariableOperation _variableOperation = VariableOperation.Equal;
    private CompareTo _compareTo = CompareTo.Value;
    private boolean _caseInsensitive = false;
    private String _constantValue = "";
    private NamedBeanHandle<Memory> _memoryHandle;
//    private NamedBeanHandle<NamedTable> _tableHandle;
//    private TableRowOrColumn _tableRowOrColumn = TableRowOrColumn.Row;
//    private String _rowOrColumnName = "";
    private String _otherLocalVariable = "";
    private String _regEx = "";
    private boolean _listenToMemory = true;
//    private boolean _listenToMemory = false;

    private NamedBeanAddressing _tableNameAddressing = NamedBeanAddressing.Direct;
    private NamedBeanHandle<NamedTable> _tableHandle;
    private String _tableNameReference = "";
    private String _tableNameLocalVariable = "";
    private String _tableNameFormula = "";
    private ExpressionNode _tableNameExpressionNode;

    private NamedBeanAddressing _tableRowAddressing = NamedBeanAddressing.Direct;
    private String _tableRowName = "";
    private String _tableRowReference = "";
    private String _tableRowLocalVariable = "";
    private String _tableRowFormula = "";
    private ExpressionNode _tableRowExpressionNode;

    private NamedBeanAddressing _tableColumnAddressing = NamedBeanAddressing.Direct;
    private String _tableColumnName = "";
    private String _tableColumnReference = "";
    private String _tableColumnLocalVariable = "";
    private String _tableColumnFormula = "";
    private ExpressionNode _tableColumnExpressionNode;

    public ExpressionLocalVariable(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        DigitalExpressionManager manager = InstanceManager.getDefault(DigitalExpressionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ExpressionLocalVariable copy = new ExpressionLocalVariable(sysName, userName);
        copy.setComment(getComment());
        copy.setLocalVariable(_localVariable);
        copy.setVariableOperation(_variableOperation);
        copy.setCompareTo(_compareTo);
        copy.setCaseInsensitive(_caseInsensitive);
        copy.setConstantValue(_constantValue);
        if (_memoryHandle != null) copy.setMemory(_memoryHandle);
//        if (_tableHandle != null) copy.setTable(_tableHandle);
//        copy.setRowOrColumn(_tableRowOrColumn);
//        copy.setRowOrColumnName(_rowOrColumnName);
        copy.setOtherLocalVariable(_localVariable);
        copy.setTableNameAddressing(_tableNameAddressing);
        if (_tableHandle != null) copy.setTable(_tableHandle);
        copy.setTableNameLocalVariable(_tableNameLocalVariable);
        copy.setTableNameReference(_tableNameReference);
        copy.setTableNameFormula(_tableNameFormula);
        copy.setTableRowAddressing(_tableRowAddressing);
        copy.setTableRowName(_tableRowName);
        copy.setTableRowLocalVariable(_tableRowLocalVariable);
        copy.setTableRowReference(_tableRowReference);
        copy.setTableRowFormula(_tableRowFormula);
        copy.setTableColumnAddressing(_tableColumnAddressing);
        copy.setTableColumnName(_tableColumnName);
        copy.setTableColumnLocalVariable(_tableColumnLocalVariable);
        copy.setTableColumnReference(_tableColumnReference);
        copy.setTableColumnFormula(_tableColumnFormula);
        return manager.registerExpression(copy).deepCopyChildren(this, systemNames, userNames);
    }

    public void setLocalVariable(String variableName) {
        assertListenersAreNotRegistered(log, "setLocalVariable");
        _localVariable = variableName;
    }

    public String getLocalVariable() {
        return _localVariable;
    }

    public void setMemory(@Nonnull String memoryName) {
        assertListenersAreNotRegistered(log, "setMemory");
        MemoryManager memoryManager = InstanceManager.getDefault(MemoryManager.class);
        Memory memory = memoryManager.getMemory(memoryName);
        if (memory != null) {
            setMemory(memory);
        } else {
            removeMemory();
            log.error("memory \"{}\" is not found", memoryName);
        }
    }

    public void setMemory(@Nonnull NamedBeanHandle<Memory> handle) {
        assertListenersAreNotRegistered(log, "setMemory");
        _memoryHandle = handle;
        if (_memoryHandle != null) {
            InstanceManager.getDefault(MemoryManager.class).addVetoableChangeListener(this);
        } else {
            InstanceManager.getDefault(MemoryManager.class).removeVetoableChangeListener(this);
        }
    }

    public void setMemory(@CheckForNull Memory memory) {
        assertListenersAreNotRegistered(log, "setMemory");
        if (memory != null) {
            _memoryHandle = InstanceManager.getDefault(NamedBeanHandleManager.class)
                    .getNamedBeanHandle(memory.getDisplayName(), memory);
            InstanceManager.getDefault(MemoryManager.class).addVetoableChangeListener(this);
        } else {
            _memoryHandle = null;
            InstanceManager.getDefault(MemoryManager.class).removeVetoableChangeListener(this);
        }
    }

    public void removeMemory() {
        assertListenersAreNotRegistered(log, "removeMemory");
        if (_memoryHandle != null) {
            InstanceManager.memoryManagerInstance().removeVetoableChangeListener(this);
            _memoryHandle = null;
        }
    }

    public NamedBeanHandle<Memory> getMemory() {
        return _memoryHandle;
    }
/*
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

    /*.*
     * Get tableRowOrColumn.
     * @return tableRowOrColumn
     *./
    public TableRowOrColumn getRowOrColumn() {
        return _tableRowOrColumn;
    }

    /*.*
     * Set tableRowOrColumn.
     * @param tableRowOrColumn tableRowOrColumn
     *./
    public void setRowOrColumn(@Nonnull TableRowOrColumn tableRowOrColumn) {
        _tableRowOrColumn = tableRowOrColumn;
    }

    /*.*
     * Get name of row or column
     * @return name of row or column
     *./
    public String getRowOrColumnName() {
        return _rowOrColumnName;
    }

    /*.*
     * Set name of row or column
     * @param rowOrColumnName name of row or column
     *./
    public void setRowOrColumnName(@Nonnull String rowOrColumnName) {
        if (rowOrColumnName == null) throw new RuntimeException("Daniel");
        _rowOrColumnName = rowOrColumnName;
    }
*/
    public void setOtherLocalVariable(@Nonnull String localVariable) {
        assertListenersAreNotRegistered(log, "setOtherLocalVariable");
        _otherLocalVariable = localVariable;
    }

    public String getOtherLocalVariable() {
        return _otherLocalVariable;
    }

    public void setConstantValue(String constantValue) {
        _constantValue = constantValue;
    }

    public String getConstantValue() {
        return _constantValue;
    }

    public void setRegEx(String regEx) {
        _regEx = regEx;
    }

    public String getRegEx() {
        return _regEx;
    }

    public void setTableNameAddressing(@Nonnull NamedBeanAddressing addressing) {
        this._tableNameAddressing = addressing;
    }

    public NamedBeanAddressing getTableNameAddressing() {
        return _tableNameAddressing;
    }

    public void setTable(@Nonnull NamedBeanHandle<NamedTable> handle) {
        assertListenersAreNotRegistered(log, "setTable");
        _tableHandle = handle;
        InstanceManager.getDefault(NamedTableManager.class).addVetoableChangeListener(this);
    }

    public void setTable(@Nonnull NamedTable table) {
        assertListenersAreNotRegistered(log, "setTable");
        setTable(InstanceManager.getDefault(NamedBeanHandleManager.class)
                .getNamedBeanHandle(table.getDisplayName(), table));
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

    public void setTableNameReference(@Nonnull String reference) {
        if ((! reference.isEmpty()) && (! ReferenceUtil.isReference(reference))) {
            throw new IllegalArgumentException("The reference \"" + reference + "\" is not a valid reference");
        }
        _tableNameReference = reference;
    }

    public String getTableNameReference() {
        return _tableNameReference;
    }

    public void setTableNameLocalVariable(@Nonnull String localVariable) {
        _tableNameLocalVariable = localVariable;
    }

    public String getTableNameLocalVariable() {
        return _tableNameLocalVariable;
    }

    public void setTableNameFormula(@Nonnull String formula) throws ParserException {
        _tableNameFormula = formula;
        parseTableNameFormula();
    }

    public String getTableNameFormula() {
        return _tableNameFormula;
    }

    private void parseTableNameFormula() throws ParserException {
        if (_tableNameAddressing == NamedBeanAddressing.Formula) {
            Map<String, Variable> variables = new HashMap<>();

            RecursiveDescentParser parser = new RecursiveDescentParser(variables);
            _tableNameExpressionNode = parser.parseExpression(_tableNameFormula);
        } else {
            _tableNameExpressionNode = null;
        }
    }

    public void setTableRowAddressing(@Nonnull NamedBeanAddressing addressing) {
        this._tableRowAddressing = addressing;
    }

    public NamedBeanAddressing getTableRowAddressing() {
        return _tableRowAddressing;
    }

    /**
     * Get name of row
     * @return name
     */
    public String getTableRowName() {
        return _tableRowName;
    }

    /**
     * Set name of column
     * @param rowName name
     */
    public void setTableRowName(@Nonnull String rowName) {
        _tableRowName = rowName;
    }

    public void setTableRowReference(@Nonnull String reference) {
        if ((! reference.isEmpty()) && (! ReferenceUtil.isReference(reference))) {
            throw new IllegalArgumentException("The reference \"" + reference + "\" is not a valid reference");
        }
        _tableRowReference = reference;
    }

    public String getTableRowReference() {
        return _tableRowReference;
    }

    public void setTableRowLocalVariable(@Nonnull String localVariable) {
        _tableRowLocalVariable = localVariable;
    }

    public String getTableRowLocalVariable() {
        return _tableRowLocalVariable;
    }

    public void setTableRowFormula(@Nonnull String formula) throws ParserException {
        _tableRowFormula = formula;
        parseTableRowFormula();
    }

    public String getTableRowFormula() {
        return _tableRowFormula;
    }

    private void parseTableRowFormula() throws ParserException {
        if (_tableRowAddressing == NamedBeanAddressing.Formula) {
            Map<String, Variable> variables = new HashMap<>();

            RecursiveDescentParser parser = new RecursiveDescentParser(variables);
            _tableRowExpressionNode = parser.parseExpression(_tableRowFormula);
        } else {
            _tableRowExpressionNode = null;
        }
    }

    public void setTableColumnAddressing(@Nonnull NamedBeanAddressing addressing) {
        this._tableColumnAddressing = addressing;
    }

    public NamedBeanAddressing getTableColumnAddressing() {
        return _tableColumnAddressing;
    }

    public void setTableColumnReference(@Nonnull String reference) {
        if ((! reference.isEmpty()) && (! ReferenceUtil.isReference(reference))) {
            throw new IllegalArgumentException("The reference \"" + reference + "\" is not a valid reference");
        }
        _tableColumnReference = reference;
    }

    public String getTableColumnReference() {
        return _tableColumnReference;
    }

    public void setTableColumnLocalVariable(@Nonnull String localVariable) {
        _tableColumnLocalVariable = localVariable;
    }

    public String getTableColumnLocalVariable() {
        return _tableColumnLocalVariable;
    }

    public void setTableColumnFormula(@Nonnull String formula) throws ParserException {
        _tableColumnFormula = formula;
        parseTableColumnFormula();
    }

    public String getTableColumnFormula() {
        return _tableColumnFormula;
    }

    private void parseTableColumnFormula() throws ParserException {
        if (_tableColumnAddressing == NamedBeanAddressing.Formula) {
            Map<String, Variable> variables = new HashMap<>();

            RecursiveDescentParser parser = new RecursiveDescentParser(variables);
            _tableColumnExpressionNode = parser.parseExpression(_tableColumnFormula);
        } else {
            _tableColumnExpressionNode = null;
        }
    }

    /**
     * Get name of column
     * @return name
     */
    public String getTableColumnName() {
        return _tableColumnName;
    }

    /**
     * Set name of column
     * @param columnName name
     */
    public void setTableColumnName(@Nonnull String columnName) {
        _tableColumnName = columnName;
    }

    public void setListenToMemory(boolean listenToMemory) {
        this._listenToMemory = listenToMemory;
    }

    public boolean getListenToMemory() {
        return _listenToMemory;
    }

    public void setVariableOperation(VariableOperation variableOperation) {
        _variableOperation = variableOperation;
    }

    public VariableOperation getVariableOperation() {
        return _variableOperation;
    }

    public void setCompareTo(CompareTo compareTo) {
        _compareTo = compareTo;
    }

    public CompareTo getCompareTo() {
        return _compareTo;
    }

    public void setCaseInsensitive(boolean caseInsensitive) {
        _caseInsensitive = caseInsensitive;
    }

    public boolean getCaseInsensitive() {
        return _caseInsensitive;
    }

    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        if ("CanDelete".equals(evt.getPropertyName())) { // No I18N
/*
            if (evt.getOldValue() instanceof Memory) {
                if (evt.getOldValue().equals(getMemory().getBean())) {
                    PropertyChangeEvent e = new PropertyChangeEvent(this, "DoNotDelete", null, null);
                    throw new PropertyVetoException(Bundle.getMessage("Memory_MemoryInUseMemoryExpressionVeto", getDisplayName()), e); // NOI18N
                }
            }
*/
            if (evt.getOldValue() instanceof NamedTable) {
                if (evt.getOldValue().equals(getTable().getBean())) {
                    throw new PropertyVetoException(getDisplayName(), evt);
                }
            }
        } else if ("DoDelete".equals(evt.getPropertyName())) { // No I18N
/*
            if (evt.getOldValue() instanceof Memory) {
                if (evt.getOldValue().equals(getMemory().getBean())) {
                    removeMemory();
                }
            }
*/
            if (evt.getOldValue() instanceof NamedTable) {
                if (evt.getOldValue().equals(getTable().getBean())) {
                    removeTable();
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.ITEM;
    }

    private String getString(Object o) {
        if (o != null) {
            return o.toString();
        }
        return null;
    }

    /**
     * Compare two values using the comparator set using the comparison
     * instructions in {@link #_variableOperation}.
     *
     * <strong>Note:</strong> {@link #_variableOperation} must be one of
     * {@link #ExpressionLocalVariable.MemoryOperation.LESS_THAN},
     * {@link #ExpressionLocalVariable.MemoryOperation.LESS_THAN_OR_EQUAL},
     * {@link #ExpressionLocalVariable.MemoryOperation.EQUAL},
     * {@link #ExpressionLocalVariable.MemoryOperation.GREATER_THAN_OR_EQUAL},
     * or {@link #ExpressionLocalVariable.MemoryOperation.GREATER_THAN}.
     *
     * @param value1          left side of the comparison
     * @param value2          right side of the comparison
     * @param caseInsensitive true if comparison should be case insensitive;
     *                        false otherwise
     * @return true if values compare per _memoryOperation; false otherwise
     */
    private boolean compare(String value1, String value2, boolean caseInsensitive) {
        if (value1 == null) {
            return value2 == null;
        } else {
            if (value2 == null) {
                return false;
            }
            value1 = value1.trim();
            value2 = value2.trim();
        }
        try {
            int n1 = Integer.parseInt(value1);
            try {
                int n2 = Integer.parseInt(value2);
                log.debug("Compare numbers: n1= {} to n2= {}", n1, n2);
                switch (_variableOperation) // both are numbers
                {
                    case LessThan:
                        return (n1 < n2);
                    case LessThanOrEqual:
                        return (n1 <= n2);
                    case Equal:
                        return (n1 == n2);
                    case NotEqual:
                        return (n1 != n2);
                    case GreaterThanOrEqual:
                        return (n1 >= n2);
                    case GreaterThan:
                        return (n1 > n2);
                    default:
                        throw new IllegalArgumentException("_memoryOperation has unknown value: "+_variableOperation.name());
                }
            } catch (NumberFormatException nfe) {
                return _variableOperation == VariableOperation.NotEqual;   // n1 is a number, n2 is not
            }
        } catch (NumberFormatException nfe) {
            try {
                Integer.parseInt(value2);
                return _variableOperation == VariableOperation.NotEqual;     // n1 is not a number, n2 is
            } catch (NumberFormatException ex) { // OK neither a number
            }
        }
        log.debug("Compare Strings: value1= {} to value2= {}", value1, value2);
        int compare;
        if (caseInsensitive) {
            compare = value1.compareToIgnoreCase(value2);
        } else {
            compare = value1.compareTo(value2);
        }
        switch (_variableOperation) {
            case LessThan:
                if (compare < 0) {
                    return true;
                }
                break;
            case LessThanOrEqual:
                if (compare <= 0) {
                    return true;
                }
                break;
            case Equal:
                if (compare == 0) {
                    return true;
                }
                break;
            case NotEqual:
                if (compare != 0) {
                    return true;
                }
                break;
            case GreaterThanOrEqual:
                if (compare >= 0) {
                    return true;
                }
                break;
            case GreaterThan:
                if (compare > 0) {
                    return true;
                }
                break;
            default:
                throw new IllegalArgumentException("_memoryOperation has unknown value: "+_variableOperation.name());
        }
        return false;
    }

    private boolean matchRegex(String memoryValue, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher m = pattern.matcher(memoryValue);
        return m.matches();
    }

    private NamedTable getTableBean() throws JmriException {

        if (_tableNameAddressing == NamedBeanAddressing.Direct) {
            return _tableHandle != null ? _tableHandle.getBean() : null;
        } else {
            String name;

            switch (_tableNameAddressing) {
                case Reference:
                    name = ReferenceUtil.getReference(
                            getConditionalNG().getSymbolTable(), _tableColumnReference);
                    break;

                case LocalVariable:
                    SymbolTable symbolTable = getConditionalNG().getSymbolTable();
                    name = TypeConversionUtil
                            .convertToString(symbolTable.getValue(_tableColumnLocalVariable), false);
                    break;

                case Formula:
                    name = _tableNameExpressionNode  != null
                            ? TypeConversionUtil.convertToString(
                                    _tableNameExpressionNode .calculate(
                                            getConditionalNG().getSymbolTable()), false)
                            : null;
                    break;

                default:
                    throw new IllegalArgumentException("invalid _addressing state: " + _tableColumnAddressing.name());
            }

            NamedTable table = null;
            if (name != null) {
                table = InstanceManager.getDefault(NamedTableManager.class)
                        .getNamedBean(name);
            }
            return table;
        }
    }

    private String getTableRow() throws JmriException {

        switch (_tableRowAddressing) {
            case Direct:
                return _tableRowName;

            case Reference:
                return ReferenceUtil.getReference(
                        getConditionalNG().getSymbolTable(), _tableRowReference);

            case LocalVariable:
                SymbolTable symbolTable = getConditionalNG().getSymbolTable();
                return TypeConversionUtil
                        .convertToString(symbolTable.getValue(_tableRowLocalVariable), false);

            case Formula:
                return _tableRowExpressionNode != null
                        ? TypeConversionUtil.convertToString(
                                _tableRowExpressionNode.calculate(
                                        getConditionalNG().getSymbolTable()), false)
                        : null;

            default:
                throw new IllegalArgumentException("invalid _addressing state: " + _tableRowAddressing.name());
        }
    }

    private String getTableColumn() throws JmriException {

        switch (_tableColumnAddressing) {
            case Direct:
                return _tableColumnName;

            case Reference:
                return ReferenceUtil.getReference(
                        getConditionalNG().getSymbolTable(), _tableColumnReference);

            case LocalVariable:
                SymbolTable symbolTable = getConditionalNG().getSymbolTable();
                return TypeConversionUtil
                        .convertToString(symbolTable.getValue(_tableColumnLocalVariable), false);

            case Formula:
                return _tableColumnExpressionNode != null
                        ? TypeConversionUtil.convertToString(
                                _tableColumnExpressionNode.calculate(
                                        getConditionalNG().getSymbolTable()), false)
                        : null;

            default:
                throw new IllegalArgumentException("invalid _addressing state: " + _tableColumnAddressing.name());
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean evaluate() throws JmriException {
        if (_localVariable == null) return false;

        String variableValue = getString(getConditionalNG()
                        .getSymbolTable().getValue(_localVariable));
        String otherValue = null;
        boolean result;

        switch (_compareTo) {
            case Value:
                otherValue = _constantValue;
                break;
            case Memory:
                otherValue = getString(_memoryHandle.getBean().getValue());
                break;
            case Table:
                otherValue = getString(getTableBean().getCell(getTableRow(), getTableColumn()));
                break;
            case LocalVariable:
                otherValue = TypeConversionUtil.convertToString(getConditionalNG().getSymbolTable().getValue(_otherLocalVariable), false);
                break;
            case RegEx:
                // Do nothing
                break;
            default:
                throw new IllegalArgumentException("_compareTo has unknown value: "+_compareTo.name());
        }

        switch (_variableOperation) {
            case LessThan:
                // fall through
            case LessThanOrEqual:
                // fall through
            case Equal:
                // fall through
            case NotEqual:
                // fall through
            case GreaterThanOrEqual:
                // fall through
            case GreaterThan:
                result = compare(variableValue, otherValue, _caseInsensitive);
                break;

            case IsNull:
                result = variableValue == null;
                break;
            case IsNotNull:
                result = variableValue != null;
                break;

            case MatchRegex:
                result = matchRegex(variableValue, _regEx);
                break;

            case NotMatchRegex:
                result = !matchRegex(variableValue, _regEx);
                break;

            default:
                throw new IllegalArgumentException("_memoryOperation has unknown value: "+_variableOperation.name());
        }

        return result;
    }

    @Override
    public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public int getChildCount() {
        return 0;
    }

    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "LocalVariable_Short");
    }

    private String getTableNameDescription(Locale locale) {
        String namedBean;
        switch (_tableNameAddressing) {
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
                namedBean = Bundle.getMessage(locale, "AddressByReference", _tableNameReference);
                break;

            case LocalVariable:
                namedBean = Bundle.getMessage(locale, "AddressByLocalVariable", _tableNameLocalVariable);
                break;

            case Formula:
                namedBean = Bundle.getMessage(locale, "AddressByFormula", _tableNameFormula);
                break;

            default:
                throw new IllegalArgumentException("invalid _tableNameAddressing: " + _tableNameAddressing.name());
        }
        return namedBean;
    }

    private String getTableRowDescription(Locale locale) {
        String row;
        switch (_tableRowAddressing) {
            case Direct:
                row = Bundle.getMessage(locale, "AddressByDirect", _tableRowName);
                break;

            case Reference:
                row = Bundle.getMessage(locale, "AddressByReference", _tableRowReference);
                break;

            case LocalVariable:
                row = Bundle.getMessage(locale, "AddressByLocalVariable", _tableRowLocalVariable);
                break;

            case Formula:
                row = Bundle.getMessage(locale, "AddressByFormula", _tableRowFormula);
                break;

            default:
                throw new IllegalArgumentException("invalid _tableRowAddressing: " + _tableRowAddressing.name());
        }
        return row;
    }

    private String getTableColumnDescription(Locale locale) {
        String column;
        switch (_tableColumnAddressing) {
            case Direct:
                column = Bundle.getMessage(locale, "AddressByDirect", _tableColumnName);
                break;

            case Reference:
                column = Bundle.getMessage(locale, "AddressByReference", _tableColumnReference);
                break;

            case LocalVariable:
                column = Bundle.getMessage(locale, "AddressByLocalVariable", _tableColumnLocalVariable);
                break;

            case Formula:
                column = Bundle.getMessage(locale, "AddressByFormula", _tableColumnFormula);
                break;

            default:
                throw new IllegalArgumentException("invalid _tableRowAddressing: " + _tableColumnAddressing.name());
        }
        return column;
    }

    @Override
    public String getLongDescription(Locale locale) {
        String variableName;
        if ((_localVariable == null) || _localVariable.isEmpty()) {
            variableName = Bundle.getMessage(locale, "BeanNotSelected");
        } else {
            variableName = _localVariable;
        }

        String memoryName;
        if (_memoryHandle != null) {
            memoryName = _memoryHandle.getName();
        } else {
            memoryName = Bundle.getMessage(locale, "BeanNotSelected");
        }

        String message;
        String other1;
        String other2 = null;
        String other3 = null;

        switch (_compareTo) {
            case Value:
                message = "LocalVariable_Long_CompareConstant";
                other1 = _constantValue;
                break;

            case Memory:
                message = "LocalVariable_Long_CompareMemory";
                other1 = memoryName;
                break;

            case LocalVariable:
                message = "LocalVariable_Long_CompareLocalVariable";
                other1 = _otherLocalVariable;
                break;

            case Table:
                message = "Memory_Long_CompareTable";
                other1 = getTableNameDescription(locale);
                other2 = getTableRowDescription(locale);
                other3 = getTableColumnDescription(locale);
                break;

            case RegEx:
                message = "LocalVariable_Long_CompareRegEx";
                other1 = _regEx;
                break;

            default:
                throw new IllegalArgumentException("_compareTo has unknown value: "+_compareTo.name());
        }

        switch (_variableOperation) {
            case LessThan:
                // fall through
            case LessThanOrEqual:
                // fall through
            case Equal:
                // fall through
            case NotEqual:
                // fall through
            case GreaterThanOrEqual:
                // fall through
            case GreaterThan:
                return Bundle.getMessage(locale, message, variableName, _variableOperation._text, other1, other2, other3);

            case IsNull:
                // fall through
            case IsNotNull:
                return Bundle.getMessage(locale, "LocalVariable_Long_CompareNull", variableName, _variableOperation._text);

            case MatchRegex:
                // fall through
            case NotMatchRegex:
                return Bundle.getMessage(locale, "LocalVariable_Long_CompareRegEx", variableName, _variableOperation._text, other1);

            default:
                throw new IllegalArgumentException("_variableOperation has unknown value: "+_variableOperation.name());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
        if (!_listenersAreRegistered && (_memoryHandle != null)) {
            if (_listenToMemory) {
                _memoryHandle.getBean().addPropertyChangeListener("value", this);
            }
            _listenersAreRegistered = true;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        if (_listenersAreRegistered) {
            if (_listenToMemory && (_memoryHandle != null)) {
                _memoryHandle.getBean().removePropertyChangeListener("value", this);
            }
            _listenersAreRegistered = false;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        getConditionalNG().execute();
    }

    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }



    public enum VariableOperation {
        LessThan(Bundle.getMessage("LocalVariableOperation_LessThan"), true),
        LessThanOrEqual(Bundle.getMessage("LocalVariableOperation_LessThanOrEqual"), true),
        Equal(Bundle.getMessage("LocalVariableOperation_Equal"), true),
        GreaterThanOrEqual(Bundle.getMessage("LocalVariableOperation_GreaterThanOrEqual"), true),
        GreaterThan(Bundle.getMessage("LocalVariableOperation_GreaterThan"), true),
        NotEqual(Bundle.getMessage("LocalVariableOperation_NotEqual"), true),
        IsNull(Bundle.getMessage("LocalVariableOperation_IsNull"), false),
        IsNotNull(Bundle.getMessage("LocalVariableOperation_IsNotNull"), false),
        MatchRegex(Bundle.getMessage("LocalVariableOperation_MatchRegEx"), true),
        NotMatchRegex(Bundle.getMessage("LocalVariableOperation_NotMatchRegEx"), true);

        private final String _text;
        private final boolean _extraValue;

        private VariableOperation(String text, boolean extraValue) {
            this._text = text;
            this._extraValue = extraValue;
        }

        @Override
        public String toString() {
            return _text;
        }

        public boolean hasExtraValue() {
            return _extraValue;
        }

    }


    public enum CompareTo {
        Value(Bundle.getMessage("LocalVariable_CompareTo_Value")),
        Memory(Bundle.getMessage("LocalVariable_CompareTo_Memory")),
        LocalVariable(Bundle.getMessage("LocalVariable_CompareTo_LocalVariable")),
        Table(Bundle.getMessage("LocalVariable_CompareTo_Table")),
        RegEx(Bundle.getMessage("LocalVariable_CompareTo_RegularExpression"));

        private final String _text;

        private CompareTo(String text) {
            this._text = text;
        }

        @Override
        public String toString() {
            return _text;
        }

    }

    /** {@inheritDoc} */
    @Override
    public void getUsageDetail(int level, NamedBean bean, List<NamedBeanUsageReport> report, NamedBean cdl) {
        log.debug("getUsageReport :: ExpressionLocalVariable: bean = {}, report = {}", cdl, report);
        if (getMemory() != null && bean.equals(getMemory().getBean())) {
            report.add(new NamedBeanUsageReport("LogixNGExpression", cdl, getLongDescription()));
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionLocalVariable.class);

}

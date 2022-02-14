package jmri.jmrit.logixng.actions;

import java.beans.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.ReferenceUtil;
import jmri.jmrit.logixng.util.parser.*;
import jmri.jmrit.logixng.util.parser.ExpressionNode;
import jmri.util.ThreadingUtil;
import jmri.util.TypeConversionUtil;

/**
 * This action sets the value of a memory.
 *
 * @author Daniel Bergqvist Copyright 2018
 */
public class ActionMemory extends AbstractDigitalAction
        implements PropertyChangeListener, VetoableChangeListener {

    private NamedBeanAddressing _addressing = NamedBeanAddressing.Direct;
    private NamedBeanHandle<Memory> _memoryHandle;
    private String _reference = "";
    private String _localVariable = "";
    private String _formula = "";
    private ExpressionNode _expressionNode;
    private NamedBeanHandle<Memory> _otherMemoryHandle;
    private MemoryOperation _memoryOperation = MemoryOperation.SetToString;
    private String _otherConstantValue = "";
//    private String _otherTableCell = "";
    private String _otherLocalVariable = "";
    private String _otherFormula = "";
    private ExpressionNode _otherExpressionNode;
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

    public ActionMemory(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws ParserException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ActionMemory copy = new ActionMemory(sysName, userName);
        copy.setComment(getComment());
        if (_memoryHandle != null) copy.setMemory(_memoryHandle);
        copy.setAddressing(_addressing);
        copy.setFormula(_formula);
        copy.setLocalVariable(_localVariable);
        copy.setReference(_reference);
        if (_otherMemoryHandle != null) copy.setOtherMemory(_otherMemoryHandle);
        copy.setMemoryOperation(_memoryOperation);
        copy.setOtherConstantValue(_otherConstantValue);
//        copy.setOtherTableCell(_otherTableCell);
        copy.setOtherLocalVariable(_otherLocalVariable);
        copy.setOtherFormula(_otherFormula);
        copy.setListenToMemory(_listenToMemory);
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
        return manager.registerAction(copy);
    }

    public void setMemory(@Nonnull String memoryName) {
        assertListenersAreNotRegistered(log, "setMemory");
        Memory memory = InstanceManager.getDefault(MemoryManager.class).getMemory(memoryName);
        if (memory != null) {
            setMemory(memory);
        } else {
            removeMemory();
            log.warn("memory \"{}\" is not found", memoryName);
        }
    }

    public void setMemory(@Nonnull NamedBeanHandle<Memory> handle) {
        assertListenersAreNotRegistered(log, "setMemory");
        _memoryHandle = handle;
        addRemoveVetoListener();
    }

    public void setMemory(@Nonnull Memory memory) {
        assertListenersAreNotRegistered(log, "setMemory");
        setMemory(InstanceManager.getDefault(NamedBeanHandleManager.class)
                .getNamedBeanHandle(memory.getDisplayName(), memory));
    }

    public void removeMemory() {
        assertListenersAreNotRegistered(log, "removeMemory");
        if (_memoryHandle != null) {
            _memoryHandle = null;
            addRemoveVetoListener();
        }
    }

    public NamedBeanHandle<Memory> getMemory() {
        return _memoryHandle;
    }


    public void setAddressing(NamedBeanAddressing addressing) throws ParserException {
        _addressing = addressing;
        parseFormula();
    }

    public NamedBeanAddressing getAddressing() {
        return _addressing;
    }

    public void setReference(@Nonnull String reference) {
        if ((! reference.isEmpty()) && (! ReferenceUtil.isReference(reference))) {
            throw new IllegalArgumentException("The reference \"" + reference + "\" is not a valid reference");
        }
        _reference = reference;
    }

    public String getReference() {
        return _reference;
    }

    public void setLocalVariable(@Nonnull String localVariable) {
        _localVariable = localVariable;
    }

    public String getLocalVariable() {
        return _localVariable;
    }

    public void setFormula(@Nonnull String formula) throws ParserException {
        _formula = formula;
        parseFormula();
    }

    public String getFormula() {
        return _formula;
    }

    private void parseFormula() throws ParserException {
        if (_addressing == NamedBeanAddressing.Formula) {
            Map<String, Variable> variables = new HashMap<>();

            RecursiveDescentParser parser = new RecursiveDescentParser(variables);
            _expressionNode = parser.parseExpression(_formula);
        } else {
            _expressionNode = null;
        }
    }


    public void setMemoryOperation(MemoryOperation state) throws ParserException {
        _memoryOperation = state;
        parseOtherFormula();
    }

    public MemoryOperation getMemoryOperation() {
        return _memoryOperation;
    }

    // Constant tab
    public void setOtherConstantValue(String constantValue) {
        _otherConstantValue = constantValue;
    }

    public String getConstantValue() {
        return _otherConstantValue;
    }

    // Memory tab
    public void setOtherMemory(@Nonnull String memoryName) {
        assertListenersAreNotRegistered(log, "setOtherMemory");
        MemoryManager memoryManager = InstanceManager.getDefault(MemoryManager.class);
        Memory memory = memoryManager.getMemory(memoryName);
        if (memory != null) {
            setOtherMemory(memory);
        } else {
            removeOtherMemory();
            log.warn("memory \"{}\" is not found", memoryName);
        }
    }

    public void setOtherMemory(@Nonnull NamedBeanHandle<Memory> handle) {
        assertListenersAreNotRegistered(log, "setOtherMemory");
        _otherMemoryHandle = handle;
        addRemoveVetoListener();
    }

    public void setOtherMemory(@Nonnull Memory memory) {
        assertListenersAreNotRegistered(log, "setOtherMemory");
        setOtherMemory(InstanceManager.getDefault(NamedBeanHandleManager.class)
                .getNamedBeanHandle(memory.getDisplayName(), memory));
    }

    public void removeOtherMemory() {
        assertListenersAreNotRegistered(log, "removeOtherMemory");
        if (_otherMemoryHandle != null) {
            _otherMemoryHandle = null;
            addRemoveVetoListener();
        }
    }

    public NamedBeanHandle<Memory> getOtherMemory() {
        return _otherMemoryHandle;
    }

    public void setListenToMemory(boolean listenToMemory) {
        this._listenToMemory = listenToMemory;
    }

    public boolean getListenToMemory() {
        return _listenToMemory;
    }
/*
    // Table tab
    public void setOtherTableCell(@Nonnull String tableCell) {
        if ((! tableCell.isEmpty()) && (! ReferenceUtil.isReference(tableCell))) {
            throw new IllegalArgumentException("The table reference \"" + tableCell + "\" is not a valid reference");
        }
        _otherTableCell = tableCell;
    }

    public String getOtherTableCell() {
        return _otherTableCell;
    }

    /*.*
     * Convert a table reference between direct table mode "table[row, col]"" and reference
     * table mode "{table[row, col]}".
     * @param string The current value.
     * @param toReference If true, return reference table mode, false for direct table mode.
     * @return the desired mode format.
     *./
    public static String convertTableReference(String string, boolean toReference) {
        String tableString = string == null ? "" : string.trim();
        boolean referenceFormat = ReferenceUtil.isReference(tableString);

        if (toReference) {
            if (referenceFormat) return tableString;
            return "{" + tableString + "}";
        }

        if (! referenceFormat) return tableString;
        return tableString.isEmpty() ? "" : tableString.substring(1, tableString.length() - 1);
    }
*/
    // Variable tab
    public void setOtherLocalVariable(@Nonnull String localVariable) {
        assertListenersAreNotRegistered(log, "setOtherLocalVariable");
        _otherLocalVariable = localVariable;
    }

    public String getOtherLocalVariable() {
        return _otherLocalVariable;
    }

    // Formula tab
    public void setOtherFormula(String formula) throws ParserException {
        _otherFormula = formula;
        parseOtherFormula();
    }

    public String getOtherFormula() {
        return _otherFormula;
    }

    private void parseOtherFormula() throws ParserException {
        if (_memoryOperation == MemoryOperation.CalculateFormula) {
            Map<String, Variable> variables = new HashMap<>();
/*
            SymbolTable symbolTable =
                    InstanceManager.getDefault(LogixNG_Manager.class)
                            .getSymbolTable();

            if (symbolTable == null && 1==1) return;    // Why does this happens?
//            if (symbolTable == null && 1==1) return;    // Nothing we can do if we don't have a symbol table
            if (symbolTable == null) throw new RuntimeException("Daniel AA");
            if (symbolTable.getSymbols() == null) throw new RuntimeException("Daniel BB");
            if (symbolTable.getSymbols().values() == null) throw new RuntimeException("Daniel BB");

            for (SymbolTable.Symbol symbol : symbolTable.getSymbols().values()) {
                variables.put(symbol.getName(),
                        new LocalVariableExpressionVariable(symbol.getName()));
            }
*/
            RecursiveDescentParser parser = new RecursiveDescentParser(variables);
            _otherExpressionNode = parser.parseExpression(_otherFormula);
        } else {
            _otherExpressionNode = null;
        }
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

    private void addRemoveVetoListener() {
        if ((_memoryHandle != null) || (_otherMemoryHandle != null)) {
            InstanceManager.getDefault(MemoryManager.class).addVetoableChangeListener(this);
        } else {
            InstanceManager.getDefault(MemoryManager.class).removeVetoableChangeListener(this);
        }
    }

    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        if ("CanDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof Memory) {
                boolean doVeto = false;
                if ((_memoryHandle != null) && evt.getOldValue().equals(_memoryHandle.getBean())) doVeto = true;
                if ((_otherMemoryHandle != null) && evt.getOldValue().equals(_otherMemoryHandle.getBean())) doVeto = true;
                if (doVeto) {
                    PropertyChangeEvent e = new PropertyChangeEvent(this, "DoNotDelete", null, null);
                    throw new PropertyVetoException(Bundle.getMessage("ActionMemory_MemoryInUseMemoryActionVeto", getDisplayName()), e); // NOI18N
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.ITEM;
    }

    private NamedTable getTableBean(ConditionalNG conditionalNG) throws JmriException {

        if (_tableNameAddressing == NamedBeanAddressing.Direct) {
            return _tableHandle != null ? _tableHandle.getBean() : null;
        } else {
            String name;

            switch (_tableNameAddressing) {
                case Reference:
                    name = ReferenceUtil.getReference(
                            conditionalNG.getSymbolTable(), _tableColumnReference);
                    break;

                case LocalVariable:
                    SymbolTable symbolTable = conditionalNG.getSymbolTable();
                    name = TypeConversionUtil
                            .convertToString(symbolTable.getValue(_tableColumnLocalVariable), false);
                    break;

                case Formula:
                    name = _tableNameExpressionNode  != null
                            ? TypeConversionUtil.convertToString(
                                    _tableNameExpressionNode .calculate(
                                            conditionalNG.getSymbolTable()), false)
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

    private String getTableRow(ConditionalNG conditionalNG) throws JmriException {

        switch (_tableRowAddressing) {
            case Direct:
                return _tableRowName;

            case Reference:
                return ReferenceUtil.getReference(
                        conditionalNG.getSymbolTable(), _tableRowReference);

            case LocalVariable:
                SymbolTable symbolTable = conditionalNG.getSymbolTable();
                return TypeConversionUtil
                        .convertToString(symbolTable.getValue(_tableRowLocalVariable), false);

            case Formula:
                return _tableRowExpressionNode != null
                        ? TypeConversionUtil.convertToString(
                                _tableRowExpressionNode.calculate(
                                        conditionalNG.getSymbolTable()), false)
                        : null;

            default:
                throw new IllegalArgumentException("invalid _addressing state: " + _tableRowAddressing.name());
        }
    }

    private String getTableColumn(ConditionalNG conditionalNG) throws JmriException {

        switch (_tableColumnAddressing) {
            case Direct:
                return _tableColumnName;

            case Reference:
                return ReferenceUtil.getReference(
                        conditionalNG.getSymbolTable(), _tableColumnReference);

            case LocalVariable:
                SymbolTable symbolTable = conditionalNG.getSymbolTable();
                return TypeConversionUtil
                        .convertToString(symbolTable.getValue(_tableColumnLocalVariable), false);

            case Formula:
                return _tableColumnExpressionNode != null
                        ? TypeConversionUtil.convertToString(
                                _tableColumnExpressionNode.calculate(
                                        conditionalNG.getSymbolTable()), false)
                        : null;

            default:
                throw new IllegalArgumentException("invalid _addressing state: " + _tableColumnAddressing.name());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {

        Memory memory;

//        System.out.format("ActionLight.execute: %s%n", getLongDescription());

        switch (_addressing) {
            case Direct:
                memory = _memoryHandle != null ? _memoryHandle.getBean() : null;
                break;

            case Reference:
                String ref = ReferenceUtil.getReference(
                        getConditionalNG().getSymbolTable(), _reference);
                memory = InstanceManager.getDefault(MemoryManager.class)
                        .getNamedBean(ref);
                break;

            case LocalVariable:
                SymbolTable symbolTable = getConditionalNG().getSymbolTable();
                memory = InstanceManager.getDefault(MemoryManager.class)
                        .getNamedBean(TypeConversionUtil
                                .convertToString(symbolTable.getValue(_localVariable), false));
                break;

            case Formula:
                memory = _expressionNode != null ?
                        InstanceManager.getDefault(MemoryManager.class)
                                .getNamedBean(TypeConversionUtil
                                        .convertToString(_expressionNode.calculate(
                                                getConditionalNG().getSymbolTable()), false))
                        : null;
                break;

            default:
                throw new IllegalArgumentException("invalid _addressing state: " + _addressing.name());
        }

//        System.out.format("ActionMemory.execute: Memory: %s%n", memory);

        if (memory == null) {
//            log.warn("memory is null");
            return;
        }

        AtomicReference<JmriException> ref = new AtomicReference<>();

        final ConditionalNG conditionalNG = getConditionalNG();

        ThreadingUtil.runOnLayoutWithJmriException(() -> {

            switch (_memoryOperation) {
                case SetToNull:
                    memory.setValue(null);
                    break;

                case SetToString:
                    memory.setValue(_otherConstantValue);
                    break;

                case CopyTableCellToMemory:
                    Object value = getTableBean(conditionalNG).getCell(
                            getTableRow(conditionalNG), getTableColumn(conditionalNG));
                    memory.setValue(value);
                    break;

                case CopyVariableToMemory:
                    Object variableValue = conditionalNG
                                    .getSymbolTable().getValue(_otherLocalVariable);
                    memory.setValue(variableValue);
                    break;

                case CopyMemoryToMemory:
                    if (_otherMemoryHandle != null) {
                        memory.setValue(_otherMemoryHandle.getBean().getValue());
                    } else {
                        log.warn("setMemory should copy memory to memory but other memory is null");
                    }
                    break;

                case CalculateFormula:
                    if (_otherFormula.isEmpty()) {
                        memory.setValue(null);
                    } else {
                        try {
                            if (_otherExpressionNode == null) {
                                return;
                            }
                            memory.setValue(_otherExpressionNode.calculate(
                                    conditionalNG.getSymbolTable()));
                        } catch (JmriException e) {
                            ref.set(e);
                        }
                    }
                    break;

                default:
                    throw new IllegalArgumentException("_memoryOperation has invalid value: {}" + _memoryOperation.name());
            }
        });

        if (ref.get() != null) throw ref.get();
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
        return Bundle.getMessage(locale, "ActionMemory_Short");
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
                throw new IllegalArgumentException("invalid _tableColumnAddressing: " + _tableColumnAddressing.name());
        }
        return column;
    }

    @Override
    public String getLongDescription(Locale locale) {
        String namedBean;

        switch (_addressing) {
            case Direct:
                String memoryName;
                if (_memoryHandle != null) {
                    memoryName = _memoryHandle.getBean().getDisplayName();
                } else {
                    memoryName = Bundle.getMessage(locale, "BeanNotSelected");
                }
                namedBean = Bundle.getMessage(locale, "AddressByDirect", memoryName);
                break;

            case Reference:
                namedBean = Bundle.getMessage(locale, "AddressByReference", _reference);
                break;

            case LocalVariable:
                namedBean = Bundle.getMessage(locale, "AddressByLocalVariable", _localVariable);
                break;

            case Formula:
                namedBean = Bundle.getMessage(locale, "AddressByFormula", _formula);
                break;

            default:
                throw new IllegalArgumentException("invalid _addressing state: " + _addressing.name());
        }

        String copyToMemoryName;
        if (_otherMemoryHandle != null) {
            copyToMemoryName = _otherMemoryHandle.getBean().getDisplayName();
        } else {
            copyToMemoryName = Bundle.getMessage(locale, "BeanNotSelected");
        }

        switch (_memoryOperation) {
            case SetToNull:
                return Bundle.getMessage(locale, "ActionMemory_Long_Null", namedBean);
            case SetToString:
                return Bundle.getMessage(locale, "ActionMemory_Long_Value", namedBean, _otherConstantValue);
            case CopyVariableToMemory:
                return Bundle.getMessage(locale, "ActionMemory_Long_CopyVariableToMemory", namedBean, _otherLocalVariable);
            case CopyMemoryToMemory:
                return Bundle.getMessage(locale, "ActionMemory_Long_CopyMemoryToMemory", namedBean, copyToMemoryName);
            case CopyTableCellToMemory:
                String tableName = getTableNameDescription(locale);
                String rowName = getTableRowDescription(locale);
                String columnName = getTableColumnDescription(locale);
                return Bundle.getMessage(locale, "ActionMemory_Long_CopyTableCellToMemory", namedBean, tableName, rowName, columnName);
            case CalculateFormula:
                return Bundle.getMessage(locale, "ActionMemory_Long_Formula", namedBean, _otherFormula);
            default:
                throw new IllegalArgumentException("_memoryOperation has invalid value: " + _memoryOperation.name());
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
        if (!_listenersAreRegistered && (_otherMemoryHandle != null)) {
            if (_listenToMemory) {
                _otherMemoryHandle.getBean().addPropertyChangeListener("value", this);
            }
            _listenersAreRegistered = true;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        if (_listenersAreRegistered) {
            if (_listenToMemory && (_otherMemoryHandle != null)) {
                _otherMemoryHandle.getBean().removePropertyChangeListener("value", this);
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


    public enum MemoryOperation {
        SetToNull(Bundle.getMessage("ActionMemory_MemoryOperation_SetToNull")),
        SetToString(Bundle.getMessage("ActionMemory_MemoryOperation_SetToString")),
        CopyVariableToMemory(Bundle.getMessage("ActionMemory_MemoryOperation_CopyVariableToMemory")),
        CopyMemoryToMemory(Bundle.getMessage("ActionMemory_MemoryOperation_CopyMemoryToMemory")),
        CopyTableCellToMemory(Bundle.getMessage("ActionMemory_MemoryOperation_CopyTableCellToMemory")),
        CalculateFormula(Bundle.getMessage("ActionMemory_MemoryOperation_CalculateFormula"));

        private final String _text;

        private MemoryOperation(String text) {
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
        log.debug("getUsageReport :: ActionMemory: bean = {}, report = {}", cdl, report);
        if (getMemory() != null && bean.equals(getMemory().getBean())) {
            report.add(new NamedBeanUsageReport("LogixNGAction", cdl, getLongDescription()));
        }
        if (getOtherMemory() != null && bean.equals(getOtherMemory().getBean())) {
            report.add(new NamedBeanUsageReport("LogixNGAction", cdl, getLongDescription()));
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionMemory.class);

}

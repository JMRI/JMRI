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
 * This action sets the value of a local variable.
 *
 * @author Daniel Bergqvist Copyright 2020
 */
public class ActionLocalVariable extends AbstractDigitalAction
        implements PropertyChangeListener, VetoableChangeListener {

    private String _localVariable;
    private NamedBeanHandle<Memory> _memoryHandle;
    private NamedBeanHandle<Block> _blockHandle;
    private NamedBeanHandle<Reporter> _reporterHandle;
    private VariableOperation _variableOperation = VariableOperation.SetToString;
    private String _constantValue = "";
//    private String _otherTableCell = "";
    private String _otherLocalVariable = "";
    private String _formula = "";
    private ExpressionNode _expressionNode;
    private boolean _listenToMemory = true;
    private boolean _listenToBlock = true;
    private boolean _listenToReporter = true;

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

    public ActionLocalVariable(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws ParserException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = systemNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ActionLocalVariable copy = new ActionLocalVariable(sysName, userName);
        copy.setComment(getComment());
        copy.setLocalVariable(_localVariable);
        copy.setVariableOperation(_variableOperation);
        copy.setConstantValue(_constantValue);
        if (_memoryHandle != null) copy.setMemory(_memoryHandle);
//        copy.setOtherTableCell(_otherTableCell);
        copy.setOtherLocalVariable(_otherLocalVariable);
        copy.setFormula(_formula);
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
        copy.setListenToMemory(_listenToMemory);
        copy.setListenToBlock(_listenToBlock);
        copy.setListenToReporter(_listenToReporter);
        return manager.registerAction(copy);
    }

    public void setLocalVariable(String variableName) {
        assertListenersAreNotRegistered(log, "setLocalVariable");   // No I18N
        _localVariable = variableName;
    }

    public String getLocalVariable() {
        return _localVariable;
    }

    public void setMemory(@Nonnull String memoryName) {
        assertListenersAreNotRegistered(log, "setMemory");  // No I18N
        MemoryManager memoryManager = InstanceManager.getDefault(MemoryManager.class);
        Memory memory = memoryManager.getMemory(memoryName);
        if (memory != null) {
            setMemory(memory);
        } else {
            removeMemory();
            log.warn("memory \"{}\" is not found", memoryName);
        }
    }

    public void setMemory(@Nonnull NamedBeanHandle<Memory> handle) {
        assertListenersAreNotRegistered(log, "setMemory");  // No I18N
        _memoryHandle = handle;
        if (_memoryHandle != null) {
            InstanceManager.getDefault(MemoryManager.class).addVetoableChangeListener(this);
        } else {
            InstanceManager.getDefault(MemoryManager.class).removeVetoableChangeListener(this);
        }
    }

    public void setMemory(@CheckForNull Memory memory) {
        assertListenersAreNotRegistered(log, "setMemory");  // No I18N
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
        assertListenersAreNotRegistered(log, "removeMemory");   // No I18N
        if (_memoryHandle != null) {
            InstanceManager.memoryManagerInstance().removeVetoableChangeListener(this);
            _memoryHandle = null;
        }
    }

    public NamedBeanHandle<Memory> getMemory() {
        return _memoryHandle;
    }

    public void setBlock(@Nonnull String blockName) {
        assertListenersAreNotRegistered(log, "setBlock");  // No I18N
        BlockManager blockManager = InstanceManager.getDefault(BlockManager.class);
        Block block = blockManager.getBlock(blockName);
        if (block != null) {
            setBlock(block);
        } else {
            removeBlock();
            log.warn("block \"{}\" is not found", blockName);
        }
    }

    public void setBlock(@Nonnull NamedBeanHandle<Block> handle) {
        assertListenersAreNotRegistered(log, "setBlock");  // No I18N
        _blockHandle = handle;
        if (_blockHandle != null) {
            InstanceManager.getDefault(BlockManager.class).addVetoableChangeListener(this);
        } else {
            InstanceManager.getDefault(BlockManager.class).removeVetoableChangeListener(this);
        }
    }

    public void setBlock(@CheckForNull Block block) {
        assertListenersAreNotRegistered(log, "setBlock");  // No I18N
        if (block != null) {
            _blockHandle = InstanceManager.getDefault(NamedBeanHandleManager.class)
                    .getNamedBeanHandle(block.getDisplayName(), block);
            InstanceManager.getDefault(BlockManager.class).addVetoableChangeListener(this);
        } else {
            _blockHandle = null;
            InstanceManager.getDefault(BlockManager.class).removeVetoableChangeListener(this);
        }
    }

    public void removeBlock() {
        assertListenersAreNotRegistered(log, "removeBlock");   // No I18N
        if (_blockHandle != null) {
            InstanceManager.getDefault(BlockManager.class).removeVetoableChangeListener(this);
            _blockHandle = null;
        }
    }

    public NamedBeanHandle<Block> getBlock() {
        return _blockHandle;
    }

    public void setReporter(@Nonnull String reporterName) {
        assertListenersAreNotRegistered(log, "setReporter");  // No I18N
        ReporterManager reporterManager = InstanceManager.getDefault(ReporterManager.class);
        Reporter reporter = reporterManager.getReporter(reporterName);
        if (reporter != null) {
            setReporter(reporter);
        } else {
            removeReporter();
            log.warn("reporter \"{}\" is not found", reporterName);
        }
    }

    public void setReporter(@Nonnull NamedBeanHandle<Reporter> handle) {
        assertListenersAreNotRegistered(log, "setReporter");  // No I18N
        _reporterHandle = handle;
        if (_reporterHandle != null) {
            InstanceManager.getDefault(ReporterManager.class).addVetoableChangeListener(this);
        } else {
            InstanceManager.getDefault(ReporterManager.class).removeVetoableChangeListener(this);
        }
    }

    public void setReporter(@CheckForNull Reporter reporter) {
        assertListenersAreNotRegistered(log, "setReporter");  // No I18N
        if (reporter != null) {
            _reporterHandle = InstanceManager.getDefault(NamedBeanHandleManager.class)
                    .getNamedBeanHandle(reporter.getDisplayName(), reporter);
            InstanceManager.getDefault(ReporterManager.class).addVetoableChangeListener(this);
        } else {
            _reporterHandle = null;
            InstanceManager.getDefault(ReporterManager.class).removeVetoableChangeListener(this);
        }
    }

    public void removeReporter() {
        assertListenersAreNotRegistered(log, "removeReporter");   // No I18N
        if (_reporterHandle != null) {
            InstanceManager.getDefault(ReporterManager.class).removeVetoableChangeListener(this);
            _reporterHandle = null;
        }
    }

    public NamedBeanHandle<Reporter> getReporter() {
        return _reporterHandle;
    }

    public void setVariableOperation(VariableOperation variableOperation) throws ParserException {
        _variableOperation = variableOperation;
        parseFormula();
    }

    public VariableOperation getVariableOperation() {
        return _variableOperation;
    }
/*
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
     * Convert a table reference between direct table mode "table[row, col]" and reference
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

    public void setFormula(String formula) throws ParserException {
        _formula = formula;
        parseFormula();
    }

    public String getFormula() {
        return _formula;
    }

    public void setListenToMemory(boolean listenToMemory) {
        this._listenToMemory = listenToMemory;
    }

    public boolean getListenToMemory() {
        return _listenToMemory;
    }

    public void setListenToBlock(boolean listenToBlock) {
        this._listenToBlock = listenToBlock;
    }

    public boolean getListenToBlock() {
        return _listenToBlock;
    }

    public void setListenToReporter(boolean listenToReporter) {
        this._listenToReporter = listenToReporter;
    }

    public boolean getListenToReporter() {
        return _listenToReporter;
    }

    private void parseFormula() throws ParserException {
        if (_variableOperation == VariableOperation.CalculateFormula) {
            Map<String, Variable> variables = new HashMap<>();

            RecursiveDescentParser parser = new RecursiveDescentParser(variables);
            _expressionNode = parser.parseExpression(_formula);
        } else {
            _expressionNode = null;
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

    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        if ("CanDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof Memory) {
                if (evt.getOldValue().equals(_memoryHandle.getBean())) {
                    PropertyChangeEvent e = new PropertyChangeEvent(this, "DoNotDelete", null, null);   // No I18N
                    throw new PropertyVetoException(Bundle.getMessage("ActionLocalVariable_MemoryInUseLocalVariableActionVeto", getDisplayName()), e); // NOI18N
                }
            }
            if (evt.getOldValue() instanceof Block) {
                if (evt.getOldValue().equals(_blockHandle.getBean())) {
                    PropertyChangeEvent e = new PropertyChangeEvent(this, "DoNotDelete", null, null);   // No I18N
                    throw new PropertyVetoException(Bundle.getMessage("ActionLocalVariable_BlockInUseLocalVariableActionVeto", getDisplayName()), e); // NOI18N
                }
            }
            if (evt.getOldValue() instanceof Memory) {
                if (evt.getOldValue().equals(_reporterHandle.getBean())) {
                    PropertyChangeEvent e = new PropertyChangeEvent(this, "DoNotDelete", null, null);   // No I18N
                    throw new PropertyVetoException(Bundle.getMessage("ActionLocalVariable_ReporterInUseLocalVariableActionVeto", getDisplayName()), e); // NOI18N
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.ITEM;
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
    public void execute() throws JmriException {
        if (_localVariable == null) return;

        SymbolTable symbolTable = getConditionalNG().getSymbolTable();

        AtomicReference<JmriException> ref = new AtomicReference<>();

        final ConditionalNG conditionalNG = getConditionalNG();

        ThreadingUtil.runOnLayoutWithJmriException(() -> {

            switch (_variableOperation) {
                case SetToNull:
                    symbolTable.setValue(_localVariable, null);
                    break;

                case SetToString:
                    symbolTable.setValue(_localVariable, _constantValue);
                    break;

                case CopyVariableToVariable:
                    Object variableValue = conditionalNG
                                    .getSymbolTable().getValue(_otherLocalVariable);

                    symbolTable.setValue(_localVariable, variableValue);
                    break;

                case CopyMemoryToVariable:
                    if (_memoryHandle != null) {
                        symbolTable.setValue(_localVariable, _memoryHandle.getBean().getValue());
                    } else {
                        log.warn("ActionLocalVariable should copy memory to variable but memory is null");
                    }
                    break;

                case CopyTableCellToVariable:
                    Object value = getTableBean().getCell(getTableRow(), getTableColumn());
                    symbolTable.setValue(_localVariable, value);
                    break;

                case CopyBlockToVariable:
                    if (_blockHandle != null) {
                        symbolTable.setValue(_localVariable, _blockHandle.getBean().getValue());
                    } else {
                        log.warn("ActionLocalVariable should copy block value to variable but block is null");
                    }
                    break;

                case CopyReporterToVariable:
                    if (_reporterHandle != null) {
                        symbolTable.setValue(_localVariable, _reporterHandle.getBean().getCurrentReport());
                    } else {
                        log.warn("ActionLocalVariable should copy current report to variable but reporter is null");
                    }
                    break;

                case CalculateFormula:
                    if (_formula.isEmpty()) {
                        symbolTable.setValue(_localVariable, null);
                    } else {
                        if (_expressionNode == null) return;

                        symbolTable.setValue(_localVariable,
                                _expressionNode.calculate(
                                        conditionalNG.getSymbolTable()));
                    }
                    break;

                default:
                    // Throw exception
                    throw new IllegalArgumentException("_memoryOperation has invalid value: {}" + _variableOperation.name());
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
        return Bundle.getMessage(locale, "ActionLocalVariable_Short");
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
        switch (_tableRowAddressing) {
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
                throw new IllegalArgumentException("invalid _tableRowAddressing: " + _tableRowAddressing.name());
        }
        return column;
    }

    @Override
    public String getLongDescription(Locale locale) {
        String copyToMemoryName;
        if (_memoryHandle != null) {
            copyToMemoryName = _memoryHandle.getBean().getDisplayName();
        } else {
            copyToMemoryName = Bundle.getMessage(locale, "BeanNotSelected");
        }

        String copyToBlockName;
        if (_blockHandle != null) {
            copyToBlockName = _blockHandle.getBean().getDisplayName();
        } else {
            copyToBlockName = Bundle.getMessage(locale, "BeanNotSelected");
        }

        String copyToReporterName;
        if (_reporterHandle != null) {
            copyToReporterName = _reporterHandle.getBean().getDisplayName();
        } else {
            copyToReporterName = Bundle.getMessage(locale, "BeanNotSelected");
        }

        switch (_variableOperation) {
            case SetToNull:
                return Bundle.getMessage(locale, "ActionLocalVariable_Long_Null", _localVariable);

            case SetToString:
                return Bundle.getMessage(locale, "ActionLocalVariable_Long_Value", _localVariable, _constantValue);

            case CopyVariableToVariable:
                return Bundle.getMessage(locale, "ActionLocalVariable_Long_CopyVariableToVariable",
                        _localVariable, _otherLocalVariable);

            case CopyMemoryToVariable:
                return Bundle.getMessage(locale, "ActionLocalVariable_Long_CopyMemoryToVariable",
                        _localVariable, copyToMemoryName);

            case CopyBlockToVariable:
                return Bundle.getMessage(locale, "ActionLocalVariable_Long_CopyBlockToVariable",
                        _localVariable, copyToBlockName);

            case CopyTableCellToVariable:
                String tableName = getTableNameDescription(locale);
                String rowName = getTableRowDescription(locale);
                String columnName = getTableColumnDescription(locale);
                return Bundle.getMessage(locale, "ActionMemory_Long_CopyTableCellToMemory", _localVariable, tableName, rowName, columnName);

            case CopyReporterToVariable:
                return Bundle.getMessage(locale, "ActionLocalVariable_Long_CopyReporterToVariable",
                        _localVariable, copyToReporterName);

            case CalculateFormula:
                return Bundle.getMessage(locale, "ActionLocalVariable_Long_Formula", _localVariable, _formula);

            default:
                throw new IllegalArgumentException("_memoryOperation has invalid value: " + _variableOperation.name());
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
        if (!_listenersAreRegistered) {
            if (_listenToMemory
                    && (_variableOperation == VariableOperation.CopyMemoryToVariable)
                    && (_memoryHandle != null)) {
                _memoryHandle.getBean().addPropertyChangeListener("value", this);
            }
            if (_listenToBlock
                    && (_variableOperation == VariableOperation.CopyBlockToVariable)
                    && (_blockHandle != null)) {
                _blockHandle.getBean().addPropertyChangeListener("value", this);
            }
            if (_listenToReporter
                    && (_variableOperation == VariableOperation.CopyReporterToVariable)
                    && (_reporterHandle != null)) {
                _reporterHandle.getBean().addPropertyChangeListener("currentReport", this);
            }
            _listenersAreRegistered = true;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        if (_listenersAreRegistered) {
            if (_listenToMemory
                    && (_variableOperation == VariableOperation.CopyMemoryToVariable)
                    && (_memoryHandle != null)) {
                _memoryHandle.getBean().removePropertyChangeListener("value", this);
            }
            if (_listenToBlock
                    && (_variableOperation == VariableOperation.CopyBlockToVariable)
                    && (_blockHandle != null)) {
                _blockHandle.getBean().removePropertyChangeListener("value", this);
            }
            if (_listenToReporter
                    && (_variableOperation == VariableOperation.CopyReporterToVariable)
                    && (_reporterHandle != null)) {
                _reporterHandle.getBean().removePropertyChangeListener("currentReport", this);
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
        SetToNull(Bundle.getMessage("ActionLocalVariable_VariableOperation_SetToNull")),
        SetToString(Bundle.getMessage("ActionLocalVariable_VariableOperation_SetToString")),
        CopyVariableToVariable(Bundle.getMessage("ActionLocalVariable_VariableOperation_CopyVariableToVariable")),
        CopyMemoryToVariable(Bundle.getMessage("ActionLocalVariable_VariableOperation_CopyMemoryToVariable")),
        CopyTableCellToVariable(Bundle.getMessage("ActionLocalVariable_VariableOperation_CopyTableCellToVariable")),
        CopyBlockToVariable(Bundle.getMessage("ActionLocalVariable_VariableOperation_CopyBlockToVariable")),
        CopyReporterToVariable(Bundle.getMessage("ActionLocalVariable_VariableOperation_CopyReporterToVariable")),
        CalculateFormula(Bundle.getMessage("ActionLocalVariable_VariableOperation_CalculateFormula"));

        private final String _text;

        private VariableOperation(String text) {
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
        log.debug("getUsageReport :: ActionLocalVariable: bean = {}, report = {}", cdl, report);
        if (getMemory() != null && bean.equals(getMemory().getBean())) {
            report.add(new NamedBeanUsageReport("LogixNGAction", cdl, getLongDescription()));
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionLocalVariable.class);

}

package jmri.jmrit.logixng.actions;

import java.beans.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.parser.*;
import jmri.jmrit.logixng.util.parser.ExpressionNode;
import jmri.jmrit.logixng.util.LogixNG_SelectTable;
import jmri.util.ThreadingUtil;

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
    private String _otherLocalVariable = "";
    private String _formula = "";
    private ExpressionNode _expressionNode;
    private boolean _listenToMemory = false;
    private boolean _listenToBlock = false;
    private boolean _listenToReporter = false;

    private final LogixNG_SelectTable _selectTable =
            new LogixNG_SelectTable(this, () -> {return _variableOperation == VariableOperation.CopyTableCellToVariable;});


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
        copy.setOtherLocalVariable(_otherLocalVariable);
        copy.setFormula(_formula);
        _selectTable.copy(copy._selectTable);
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

    public LogixNG_SelectTable getSelectTable() {
        return _selectTable;
    }

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
                    Object value = _selectTable.evaluateTableData(conditionalNG);
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
                String tableName = _selectTable.getTableNameDescription(locale);
                String rowName = _selectTable.getTableRowDescription(locale);
                String columnName = _selectTable.getTableColumnDescription(locale);
                return Bundle.getMessage(locale, "ActionLocalVariable_Long_CopyTableCellToVariable", _localVariable, tableName, rowName, columnName);

            case CopyReporterToVariable:
                return Bundle.getMessage(locale, "ActionLocalVariable_Long_CopyReporterToVariable",
                        _localVariable, copyToReporterName);

            case CalculateFormula:
                return Bundle.getMessage(locale, "ActionLocalVariable_Long_Formula", _localVariable, _formula);

            default:
                throw new IllegalArgumentException("_variableOperation has invalid value: " + _variableOperation.name());
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

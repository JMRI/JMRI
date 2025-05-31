package jmri.jmrit.logixng.actions;

import java.beans.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.parser.*;
import jmri.jmrit.logixng.util.parser.ExpressionNode;
import jmri.jmrit.logixng.util.LogixNG_SelectNamedBean;
import jmri.jmrit.logixng.util.LogixNG_SelectTable;
import jmri.jmrit.logixng.util.ReferenceUtil;
import jmri.util.ThreadingUtil;
import jmri.util.TypeConversionUtil;

/**
 * This action sets the value of a local variable.
 *
 * @author Daniel Bergqvist Copyright 2020
 */
public class ActionLocalVariable extends AbstractDigitalAction
        implements PropertyChangeListener {

    private String _localVariable;

    private final LogixNG_SelectNamedBean<Memory> _selectMemoryNamedBean =
            new LogixNG_SelectNamedBean<>(
                    this, Memory.class, InstanceManager.getDefault(MemoryManager.class), this);

    private final LogixNG_SelectNamedBean<Block> _selectBlockNamedBean =
            new LogixNG_SelectNamedBean<>(
                    this, Block.class, InstanceManager.getDefault(BlockManager.class), this);

    private final LogixNG_SelectNamedBean<Reporter> _selectReporterNamedBean =
            new LogixNG_SelectNamedBean<>(
                    this, Reporter.class, InstanceManager.getDefault(ReporterManager.class), this);

    private VariableOperation _variableOperation = VariableOperation.SetToString;
    private ConstantType _constantType = ConstantType.String;
    private String _constantValue = "";
    private String _otherLocalVariable = "";
    private String _reference = "";
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

        _selectMemoryNamedBean.setOnlyDirectAddressingAllowed();
        _selectBlockNamedBean.setOnlyDirectAddressingAllowed();
        _selectReporterNamedBean.setOnlyDirectAddressingAllowed();
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws ParserException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ActionLocalVariable copy = new ActionLocalVariable(sysName, userName);
        copy.setComment(getComment());
        copy.setLocalVariable(_localVariable);
        copy.setVariableOperation(_variableOperation);
        copy.setConstantType(_constantType);
        copy.setConstantValue(_constantValue);
        _selectMemoryNamedBean.copy(copy._selectMemoryNamedBean);
        _selectBlockNamedBean.copy(copy._selectBlockNamedBean);
        _selectReporterNamedBean.copy(copy._selectReporterNamedBean);
        copy.setOtherLocalVariable(_otherLocalVariable);
        copy.setReference(_reference);
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

    public LogixNG_SelectNamedBean<Memory> getSelectMemoryNamedBean() {
        return _selectMemoryNamedBean;
    }

    public LogixNG_SelectNamedBean<Block> getSelectBlockNamedBean() {
        return _selectBlockNamedBean;
    }

    public LogixNG_SelectNamedBean<Reporter> getSelectReporterNamedBean() {
        return _selectReporterNamedBean;
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

    public void setReference(@Nonnull String reference) {
        assertListenersAreNotRegistered(log, "setReference");
        _reference = reference;
    }

    public String getReference() {
        return _reference;
    }

    public void setConstantType(ConstantType constantType) {
        _constantType = constantType;
    }

    public ConstantType getConstantType() {
        return _constantType;
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

    /** {@inheritDoc} */
    @Override
    public LogixNG_Category getCategory() {
        return LogixNG_Category.ITEM;
    }

    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {
        if (_localVariable == null) return;

        final ConditionalNG conditionalNG = getConditionalNG();

        SymbolTable symbolTable = conditionalNG.getSymbolTable();

        AtomicReference<JmriException> ref = new AtomicReference<>();

        ThreadingUtil.runOnLayoutWithJmriException(() -> {

            switch (_variableOperation) {
                case SetToNull:
                    symbolTable.setValue(_localVariable, null);
                    break;

                case SetToString: {
                    Object value;
                    switch (_constantType) {
                        case String:
                            value = _constantValue;
                            break;
                        case Integer:
                            value = TypeConversionUtil.convertToLong(_constantValue);
                            break;
                        case FloatingNumber:
                            value = TypeConversionUtil.convertToDouble(_constantValue, true, true, true);
                            break;
                        case Boolean:
                            value = TypeConversionUtil.convertToBoolean(_constantValue, true);
                            break;
                        default:
                            // Throw exception
                            throw new IllegalArgumentException("_constantType has invalid value: {}" + _constantType.name());
                    }
                    symbolTable.setValue(_localVariable, value);
                    break;
                }

                case CopyVariableToVariable:
                    Object variableValue = conditionalNG
                                    .getSymbolTable().getValue(_otherLocalVariable);

                    symbolTable.setValue(_localVariable, variableValue);
                    break;

                case CopyMemoryToVariable:
                    Memory memory = _selectMemoryNamedBean.evaluateNamedBean(conditionalNG);
                    if (memory != null) {
                        symbolTable.setValue(_localVariable, memory.getValue());
                    } else {
                        log.warn("ActionLocalVariable should copy memory to variable but memory is null");
                    }
                    break;

                case CopyReferenceToVariable:
                    symbolTable.setValue(_localVariable, ReferenceUtil.getReference(
                            conditionalNG.getSymbolTable(), _reference));
                    break;

                case CopyTableCellToVariable:
                    Object value = _selectTable.evaluateTableData(conditionalNG);
                    symbolTable.setValue(_localVariable, value);
                    break;

                case CopyBlockToVariable:
                    Block block = _selectBlockNamedBean.evaluateNamedBean(conditionalNG);
                    if (block != null) {
                        symbolTable.setValue(_localVariable, block.getValue());
                    } else {
                        log.warn("ActionLocalVariable should copy block value to variable but block is null");
                    }
                    break;

                case CopyReporterToVariable:
                    Reporter reporter = _selectReporterNamedBean.evaluateNamedBean(conditionalNG);
                    if (reporter != null) {
                        symbolTable.setValue(_localVariable, reporter.getCurrentReport());
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
                    throw new IllegalArgumentException("_variableOperation has invalid value: {}" + _variableOperation.name());
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
        String copyToMemoryName = _selectMemoryNamedBean.getDescription(locale);
        String copyToBlockName = _selectBlockNamedBean.getDescription(locale);
        String copyToReporterName = _selectReporterNamedBean.getDescription(locale);

        switch (_variableOperation) {
            case SetToNull:
                return Bundle.getMessage(locale, "ActionLocalVariable_Long_Null", _localVariable);

            case SetToString:
                return Bundle.getMessage(locale, "ActionLocalVariable_Long_Value",
                        _localVariable, _constantType._text, _constantValue);

            case CopyVariableToVariable:
                return Bundle.getMessage(locale, "ActionLocalVariable_Long_CopyVariableToVariable",
                        _localVariable, _otherLocalVariable);

            case CopyMemoryToVariable:
                return Bundle.getMessage(locale, "ActionLocalVariable_Long_CopyMemoryToVariable",
                        _localVariable, copyToMemoryName, Base.getListenString(_listenToMemory));

            case CopyReferenceToVariable:
                return Bundle.getMessage(locale, "ActionLocalVariable_Long_CopyReferenceToVariable",
                        _localVariable, _reference);

            case CopyBlockToVariable:
                return Bundle.getMessage(locale, "ActionLocalVariable_Long_CopyBlockToVariable",
                        _localVariable, copyToBlockName, Base.getListenString(_listenToBlock));

            case CopyTableCellToVariable:
                String tableName = _selectTable.getTableNameDescription(locale);
                String rowName = _selectTable.getTableRowDescription(locale);
                String columnName = _selectTable.getTableColumnDescription(locale);
                return Bundle.getMessage(locale, "ActionLocalVariable_Long_CopyTableCellToVariable", _localVariable, tableName, rowName, columnName);

            case CopyReporterToVariable:
                return Bundle.getMessage(locale, "ActionLocalVariable_Long_CopyReporterToVariable",
                        _localVariable, copyToReporterName, Base.getListenString(_listenToReporter));

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
                    && (_variableOperation == VariableOperation.CopyMemoryToVariable)) {
                _selectMemoryNamedBean.addPropertyChangeListener("value", this);
            }
            if (_listenToBlock
                    && (_variableOperation == VariableOperation.CopyBlockToVariable)) {
                _selectBlockNamedBean.addPropertyChangeListener("value", this);
            }
            if (_listenToReporter
                    && (_variableOperation == VariableOperation.CopyReporterToVariable)) {
                _selectReporterNamedBean.addPropertyChangeListener("currentReport", this);
            }
            _selectMemoryNamedBean.registerListeners();
            _selectBlockNamedBean.registerListeners();
            _selectReporterNamedBean.registerListeners();
            _listenersAreRegistered = true;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        if (_listenersAreRegistered) {
            if (_listenToMemory
                    && (_variableOperation == VariableOperation.CopyMemoryToVariable)) {
                _selectMemoryNamedBean.removePropertyChangeListener("value", this);
            }
            if (_listenToBlock
                    && (_variableOperation == VariableOperation.CopyBlockToVariable)) {
                _selectBlockNamedBean.removePropertyChangeListener("value", this);
            }
            if (_listenToReporter
                    && (_variableOperation == VariableOperation.CopyReporterToVariable)) {
                _selectReporterNamedBean.removePropertyChangeListener("currentReport", this);
            }
            _selectMemoryNamedBean.unregisterListeners();
            _selectBlockNamedBean.unregisterListeners();
            _selectReporterNamedBean.unregisterListeners();
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
        CopyReferenceToVariable(Bundle.getMessage("ActionLocalVariable_VariableOperation_CopyReferenceToVariable")),
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

    public enum ConstantType {
        String(Bundle.getMessage("ActionLocalVariable_ConstantType_String")),
        Integer(Bundle.getMessage("ActionLocalVariable_ConstantType_Integer")),
        FloatingNumber(Bundle.getMessage("ActionLocalVariable_ConstantType_FloatingNumber")),
        Boolean(Bundle.getMessage("ActionLocalVariable_ConstantType_Boolean"));

        private final String _text;

        private ConstantType(String text) {
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
        _selectMemoryNamedBean.getUsageDetail(level, bean, report, cdl, this, LogixNG_SelectNamedBean.Type.Action);
        _selectBlockNamedBean.getUsageDetail(level, bean, report, cdl, this, LogixNG_SelectNamedBean.Type.Action);
        _selectReporterNamedBean.getUsageDetail(level, bean, report, cdl, this, LogixNG_SelectNamedBean.Type.Action);
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionLocalVariable.class);

}

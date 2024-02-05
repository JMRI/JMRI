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
 * This action sets a cell value of a LogixNG table.
 *
 * @author Daniel Bergqvist Copyright 2024
 */
public class ActionTable extends AbstractDigitalAction
        implements PropertyChangeListener {

    private final LogixNG_SelectTable _selectTableToSet =
            new LogixNG_SelectTable(this, () -> {return true; });

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


    public ActionTable(String sys, String user)
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
        ActionTable copy = new ActionTable(sysName, userName);
        copy.setComment(getComment());
        _selectTableToSet.copy(copy._selectTableToSet);
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

    public LogixNG_SelectTable getSelectTableToSet() {
        return _selectTableToSet;
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
    public Category getCategory() {
        return Category.ITEM;
    }

    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {

        final ConditionalNG conditionalNG = getConditionalNG();

        AtomicReference<JmriException> ref = new AtomicReference<>();

        ThreadingUtil.runOnLayoutWithJmriException(() -> {

            Object value;

            switch (_variableOperation) {
                case SetToNull:
                    value = null;
                    break;

                case SetToString: {
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
                    break;
                }

                case CopyVariableToVariable:
                    value = conditionalNG.getSymbolTable().getValue(_otherLocalVariable);
                    break;

                case CopyMemoryToVariable:
                    Memory memory = _selectMemoryNamedBean.evaluateNamedBean(conditionalNG);
                    if (memory != null) {
                        value = memory.getValue();
                    } else {
                        log.warn("ActionTable should copy memory to variable but memory is null");
                        return;
                    }
                    break;

                case CopyReferenceToVariable:
                    value = ReferenceUtil.getReference(conditionalNG.getSymbolTable(),
                            _reference);
                    break;

                case CopyTableCellToVariable:
                    value = _selectTable.evaluateTableData(conditionalNG);
                    break;

                case CopyBlockToVariable:
                    Block block = _selectBlockNamedBean.evaluateNamedBean(conditionalNG);
                    if (block != null) {
                        value = block.getValue();
                    } else {
                        log.warn("ActionTable should copy block value to variable but block is null");
                        return;
                    }
                    break;

                case CopyReporterToVariable:
                    Reporter reporter = _selectReporterNamedBean.evaluateNamedBean(conditionalNG);
                    if (reporter != null) {
                        value = reporter.getCurrentReport();
                    } else {
                        log.warn("ActionTable should copy current report to variable but reporter is null");
                        return;
                    }
                    break;

                case CalculateFormula:
                    if (_formula.isEmpty()) {
                        value = null;
                    } else {
                        if (_expressionNode == null) return;

                        value = _expressionNode.calculate(conditionalNG.getSymbolTable());
                    }
                    break;

                default:
                    // Throw exception
                    throw new IllegalArgumentException("_variableOperation has invalid value: {}" + _variableOperation.name());
            }

            _selectTableToSet.evaluateAndSetTableData(conditionalNG, value);
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
        return Bundle.getMessage(locale, "ActionTable_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {

        String setTableName = _selectTableToSet.getTableNameDescription(locale);
        String setRowName = _selectTableToSet.getTableRowDescription(locale);
        String setColumnName = _selectTableToSet.getTableColumnDescription(locale);
        String setTable = Bundle.getMessage(locale, "ActionTable_Table", setTableName, setRowName, setColumnName);

        String copyToMemoryName = _selectMemoryNamedBean.getDescription(locale);
        String copyToBlockName = _selectBlockNamedBean.getDescription(locale);
        String copyToReporterName = _selectReporterNamedBean.getDescription(locale);

        switch (_variableOperation) {
            case SetToNull:
                return Bundle.getMessage(locale, "ActionTable_Long_Null", setTable);

            case SetToString:
                return Bundle.getMessage(locale, "ActionTable_Long_Value",
                        setTable, _constantType._text, _constantValue);

            case CopyVariableToVariable:
                return Bundle.getMessage(locale, "ActionTable_Long_CopyVariableToVariable",
                        setTable, _otherLocalVariable);

            case CopyMemoryToVariable:
                return Bundle.getMessage(locale, "ActionTable_Long_CopyMemoryToVariable",
                        setTable, copyToMemoryName, Base.getListenString(_listenToMemory));

            case CopyReferenceToVariable:
                return Bundle.getMessage(locale, "ActionTable_Long_CopyReferenceToVariable",
                        setTable, _reference);

            case CopyBlockToVariable:
                return Bundle.getMessage(locale, "ActionTable_Long_CopyBlockToVariable",
                        setTable, copyToBlockName, Base.getListenString(_listenToBlock));

            case CopyTableCellToVariable:
                String tableName = _selectTable.getTableNameDescription(locale);
                String rowName = _selectTable.getTableRowDescription(locale);
                String columnName = _selectTable.getTableColumnDescription(locale);
                return Bundle.getMessage(locale, "ActionTable_Long_CopyTableCellToVariable",
                        setTable, tableName, rowName, columnName);

            case CopyReporterToVariable:
                return Bundle.getMessage(locale, "ActionTable_Long_CopyReporterToVariable",
                        setTable, copyToReporterName, Base.getListenString(_listenToReporter));

            case CalculateFormula:
                return Bundle.getMessage(locale, "ActionTable_Long_Formula", setTable, _formula);

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
        SetToNull(Bundle.getMessage("ActionTable_VariableOperation_SetToNull")),
        SetToString(Bundle.getMessage("ActionTable_VariableOperation_SetToString")),
        CopyVariableToVariable(Bundle.getMessage("ActionTable_VariableOperation_CopyVariableToVariable")),
        CopyMemoryToVariable(Bundle.getMessage("ActionTable_VariableOperation_CopyMemoryToVariable")),
        CopyReferenceToVariable(Bundle.getMessage("ActionTable_VariableOperation_CopyReferenceToVariable")),
        CopyTableCellToVariable(Bundle.getMessage("ActionTable_VariableOperation_CopyTableCellToVariable")),
        CopyBlockToVariable(Bundle.getMessage("ActionTable_VariableOperation_CopyBlockToVariable")),
        CopyReporterToVariable(Bundle.getMessage("ActionTable_VariableOperation_CopyReporterToVariable")),
        CalculateFormula(Bundle.getMessage("ActionTable_VariableOperation_CalculateFormula"));

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
        String(Bundle.getMessage("ActionTable_ConstantType_String")),
        Integer(Bundle.getMessage("ActionTable_ConstantType_Integer")),
        FloatingNumber(Bundle.getMessage("ActionTable_ConstantType_FloatingNumber")),
        Boolean(Bundle.getMessage("ActionTable_ConstantType_Boolean"));

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
        log.debug("getUsageReport :: ActionTable: bean = {}, report = {}", cdl, report);
        _selectMemoryNamedBean.getUsageDetail(level, bean, report, cdl, this, LogixNG_SelectNamedBean.Type.Action);
        _selectBlockNamedBean.getUsageDetail(level, bean, report, cdl, this, LogixNG_SelectNamedBean.Type.Action);
        _selectReporterNamedBean.getUsageDetail(level, bean, report, cdl, this, LogixNG_SelectNamedBean.Type.Action);
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionTable.class);

}

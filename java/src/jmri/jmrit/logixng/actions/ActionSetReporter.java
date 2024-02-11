package jmri.jmrit.logixng.actions;

import java.beans.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.LogixNG_SelectNamedBean;
import jmri.jmrit.logixng.util.parser.*;
import jmri.jmrit.logixng.util.parser.ExpressionNode;
import jmri.jmrit.logixng.util.LogixNG_SelectTable;
import jmri.util.ThreadingUtil;

/**
 * This action sets the current report of a Reporter.
 *
 * @author Daniel Bergqvist Copyright 2024
 */
public class ActionSetReporter extends AbstractDigitalAction
        implements PropertyChangeListener {

    private final LogixNG_SelectNamedBean<Reporter> _selectNamedBean =
            new LogixNG_SelectNamedBean<>(
                    this, Reporter.class, InstanceManager.getDefault(ReporterManager.class), this);
    private final LogixNG_SelectNamedBean<Memory> _selectOtherMemoryNamedBean =
            new LogixNG_SelectNamedBean<>(
                    this, Memory.class, InstanceManager.getDefault(MemoryManager.class), this);
//    private NamedBeanHandle<Memory> _otherMemoryHandle;
    private ReporterOperation _reporterOperation = ReporterOperation.SetToString;
    private String _otherConstantValue = "";
    private String _otherLocalVariable = "";
    private String _otherFormula = "";
    private ExpressionNode _otherExpressionNode;
    private boolean _listenToMemory = true;

    private final LogixNG_SelectTable _selectTable =
            new LogixNG_SelectTable(this, () -> {return _reporterOperation == ReporterOperation.CopyTableCellToReporter;});


    public ActionSetReporter(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws ParserException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ActionSetReporter copy = new ActionSetReporter(sysName, userName);
        copy.setComment(getComment());
        _selectNamedBean.copy(copy._selectNamedBean);
        _selectOtherMemoryNamedBean.copy(copy._selectOtherMemoryNamedBean);
        copy.setMemoryOperation(_reporterOperation);
        copy.setOtherConstantValue(_otherConstantValue);
//        copy.setOtherTableCell(_otherTableCell);
        copy.setOtherLocalVariable(_otherLocalVariable);
        copy.setOtherFormula(_otherFormula);
        copy.setListenToMemory(_listenToMemory);
        _selectTable.copy(copy._selectTable);
        return manager.registerAction(copy);
    }

    public LogixNG_SelectNamedBean<Reporter> getSelectNamedBean() {
        return _selectNamedBean;
    }

    public LogixNG_SelectNamedBean<Memory> getSelectOtherMemoryNamedBean() {
        return _selectOtherMemoryNamedBean;
    }

    public void setMemoryOperation(ReporterOperation state) throws ParserException {
        _reporterOperation = state;
        parseOtherFormula();
    }

    public ReporterOperation getReporterOperation() {
        return _reporterOperation;
    }

    // Constant tab
    public void setOtherConstantValue(String constantValue) {
        _otherConstantValue = constantValue;
    }

    public String getConstantValue() {
        return _otherConstantValue;
    }

    public LogixNG_SelectTable getSelectTable() {
        return _selectTable;
    }

    public void setListenToMemory(boolean listenToMemory) {
        this._listenToMemory = listenToMemory;
    }

    public boolean getListenToMemory() {
        return _listenToMemory;
    }

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
        if (_reporterOperation == ReporterOperation.CalculateFormula) {
            Map<String, Variable> variables = new HashMap<>();
            RecursiveDescentParser parser = new RecursiveDescentParser(variables);
            _otherExpressionNode = parser.parseExpression(_otherFormula);
        } else {
            _otherExpressionNode = null;
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

        Reporter reporter = _selectNamedBean.evaluateNamedBean(conditionalNG);

        if (reporter == null) {
//            log.warn("memory is null");
            return;
        }

        AtomicReference<JmriException> ref = new AtomicReference<>();

        ThreadingUtil.runOnLayoutWithJmriException(() -> {

            switch (_reporterOperation) {
                case SetToNull:
                    reporter.setReport(null);
                    break;

                case SetToString:
                    reporter.setReport(_otherConstantValue);
                    break;

                case CopyTableCellToReporter:
                    Object value = _selectTable.evaluateTableData(conditionalNG);
                    reporter.setReport(value);
                    break;

                case CopyVariableToReporter:
                    Object variableValue = conditionalNG
                                    .getSymbolTable().getValue(_otherLocalVariable);
                    reporter.setReport(variableValue);
                    break;

                case CopyMemoryToReporter:
                    Memory otherMemory = _selectOtherMemoryNamedBean.evaluateNamedBean(conditionalNG);
                    if (otherMemory != null) {
                        reporter.setReport(otherMemory.getValue());
                    } else {
                        log.warn("setReporter should copy memory to reporter but memory is null");
                    }
                    break;

                case CalculateFormula:
                    if (_otherFormula.isEmpty()) {
                        reporter.setReport(null);
                    } else {
                        try {
                            if (_otherExpressionNode == null) {
                                return;
                            }
                            reporter.setReport(_otherExpressionNode.calculate(
                                    conditionalNG.getSymbolTable()));
                        } catch (JmriException e) {
                            ref.set(e);
                        }
                    }
                    break;

                default:
                    throw new IllegalArgumentException("_reporterOperation has invalid value: {}" + _reporterOperation.name());
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
        return Bundle.getMessage(locale, "ActionSetReporter_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String namedBean = _selectNamedBean.getDescription(locale);

        String copyToMemoryName = _selectOtherMemoryNamedBean.getDescription(locale);

        switch (_reporterOperation) {
            case SetToNull:
                return Bundle.getMessage(locale, "ActionSetReporter_Long_Null", namedBean);
            case SetToString:
                return Bundle.getMessage(locale, "ActionSetReporter_Long_Value", namedBean, _otherConstantValue);
            case CopyVariableToReporter:
                return Bundle.getMessage(locale, "ActionSetReporter_Long_CopyVariableToReporter", namedBean, _otherLocalVariable);
            case CopyMemoryToReporter:
                return Bundle.getMessage(locale, "ActionSetReporter_Long_CopyMemoryToReporter", namedBean, copyToMemoryName);
            case CopyTableCellToReporter:
                String tableName = _selectTable.getTableNameDescription(locale);
                String rowName = _selectTable.getTableRowDescription(locale);
                String columnName = _selectTable.getTableColumnDescription(locale);
                return Bundle.getMessage(locale, "ActionSetReporter_Long_CopyTableCellToReporter", namedBean, tableName, rowName, columnName);
            case CalculateFormula:
                return Bundle.getMessage(locale, "ActionSetReporter_Long_Formula", namedBean, _otherFormula);
            default:
                throw new IllegalArgumentException("_memoryOperation has invalid value: " + _reporterOperation.name());
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
            if (_listenToMemory) {
                _selectOtherMemoryNamedBean.addPropertyChangeListener("value", this);
            }
            _selectNamedBean.registerListeners();
            _listenersAreRegistered = true;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        if (_listenersAreRegistered && _listenToMemory) {
            _selectOtherMemoryNamedBean.removePropertyChangeListener("value", this);
        }
        _selectNamedBean.unregisterListeners();
        _listenersAreRegistered = false;
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


    public enum ReporterOperation {
        SetToNull(Bundle.getMessage("ActionSetReporter_ReporterOperation_SetToNull")),
        SetToString(Bundle.getMessage("ActionSetReporter_ReporterOperation_SetToString")),
        CopyVariableToReporter(Bundle.getMessage("ActionSetReporter_ReporterOperation_CopyVariableToReporter")),
        CopyMemoryToReporter(Bundle.getMessage("ActionSetReporter_ReporterOperation_CopyMemoryToReporter")),
        CopyTableCellToReporter(Bundle.getMessage("ActionSetReporter_ReporterOperation_CopyTableCellToReporter")),
        CalculateFormula(Bundle.getMessage("ActionSetReporter_ReporterOperation_CalculateFormula"));

        private final String _text;

        private ReporterOperation(String text) {
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
        _selectNamedBean.getUsageDetail(level, bean, report, cdl, this, LogixNG_SelectNamedBean.Type.Action);
        _selectOtherMemoryNamedBean.getUsageDetail(level, bean, report, cdl, this, LogixNG_SelectNamedBean.Type.Action);
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionSetReporter.class);

}

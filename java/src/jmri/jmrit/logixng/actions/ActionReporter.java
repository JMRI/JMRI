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
import jmri.jmrit.logixng.util.parser.ExpressionNode;
import jmri.jmrit.logixng.util.parser.RecursiveDescentParser;
import jmri.util.TypeConversionUtil;

/**
 * This action triggers a reporter.
 *
 * @author Daniel Bergqvist Copyright 2021
 * @author Dave Sand Copyright 2021
 */
public class ActionReporter extends AbstractDigitalAction
        implements PropertyChangeListener {

    private final LogixNG_SelectNamedBean<Reporter> _selectNamedBean =
            new LogixNG_SelectNamedBean<>(
                    this, Reporter.class, InstanceManager.getDefault(ReporterManager.class), this);

    private final LogixNG_SelectNamedBean<Memory> _selectMemoryNamedBean =
            new LogixNG_SelectNamedBean<>(
                    this, Memory.class, InstanceManager.getDefault(MemoryManager.class), this);

    private ReporterValue _reporterValue = ReporterValue.CopyCurrentReport;

    private NamedBeanAddressing _dataAddressing = NamedBeanAddressing.Direct;
    private String _dataReference = "";
    private String _dataLocalVariable = "";
    private String _dataFormula = "";
    private ExpressionNode _dataExpressionNode;

//    private NamedBeanHandle<Memory> _memoryHandle;

    public ActionReporter(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws ParserException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ActionReporter copy = new ActionReporter(sysName, userName);
        copy.setComment(getComment());
        _selectNamedBean.copy(copy._selectNamedBean);
        _selectMemoryNamedBean.copy(copy._selectMemoryNamedBean);

        copy.setReporterValue(_reporterValue);

        copy.setDataAddressing(_dataAddressing);
        copy.setDataReference(_dataReference);
        copy.setDataLocalVariable(_dataLocalVariable);
        copy.setDataFormula(_dataFormula);
//        if (_memoryHandle != null) copy.setMemory(_memoryHandle);

        return manager.registerAction(copy);
    }

    public LogixNG_SelectNamedBean<Reporter> getSelectNamedBean() {
        return _selectNamedBean;
    }

    public LogixNG_SelectNamedBean<Memory> getSelectMemoryNamedBean() {
        return _selectMemoryNamedBean;
    }

    public void setReporterValue(ReporterValue value) {
        _reporterValue = value;
    }

    public ReporterValue getReporterValue() {
        return _reporterValue;
    }

    public void setDataAddressing(NamedBeanAddressing addressing) throws ParserException {
        _dataAddressing = addressing;
        parseDataFormula();
    }

    public NamedBeanAddressing getDataAddressing() {
        return _dataAddressing;
    }

    public void setDataReference(@Nonnull String reference) {
        if ((! reference.isEmpty()) && (! ReferenceUtil.isReference(reference))) {
            throw new IllegalArgumentException("The reference \"" + reference + "\" is not a valid reference");
        }
        _dataReference = reference;
    }

    public String getDataReference() {
        return _dataReference;
    }

    public void setDataLocalVariable(@Nonnull String localVariable) {
        _dataLocalVariable = localVariable;
    }

    public String getDataLocalVariable() {
        return _dataLocalVariable;
    }

    public void setDataFormula(@Nonnull String formula) throws ParserException {
        _dataFormula = formula;
        parseDataFormula();
    }

    public String getDataFormula() {
        return _dataFormula;
    }

    private void parseDataFormula() throws ParserException {
        if (_dataAddressing == NamedBeanAddressing.Formula) {
            Map<String, Variable> variables = new HashMap<>();

            RecursiveDescentParser parser = new RecursiveDescentParser(variables);
            _dataExpressionNode = parser.parseExpression(_dataFormula);
        } else {
            _dataExpressionNode = null;
        }
    }


    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.ITEM;
    }

    Object getReporterData(Reporter reporter) throws JmriException {
        Object obj;
        ReporterValue value = _reporterValue;

        switch (value) {
            case CopyCurrentReport:
                obj = reporter.getCurrentReport();
                break;
            case CopyLastReport:
                obj = reporter.getLastReport();
                break;
            case CopyState:
                obj = reporter.getState();
                break;
            default:
                throw new IllegalArgumentException("invalid value name: " + value.name());
        }

        return obj;
    }

    void updateDestination(Object data) throws JmriException {
        switch (_dataAddressing) {
            case Direct:
                Memory memory = _selectMemoryNamedBean.evaluateNamedBean(getConditionalNG());
                if (memory != null) {
                    memory.setValue(data);
                }
                break;

            case Reference:
                String refName = ReferenceUtil.getReference(
                        getConditionalNG().getSymbolTable(), _dataReference);
                log.debug("ref ref = {}, name = {}", _dataReference, refName);
                Memory refMem = InstanceManager.getDefault(MemoryManager.class).getMemory(refName);
                if (refMem == null) {
                    throw new IllegalArgumentException("invalid memory reference: " + refName);
                }
                refMem.setValue(data);
                break;

            case LocalVariable:
                log.debug("LocalVariable: lv = {}", _dataLocalVariable);
                getConditionalNG().getSymbolTable().setValue(_dataLocalVariable, data);
                break;

            case Formula:
                String formulaName = _dataExpressionNode != null
                        ? TypeConversionUtil.convertToString(
                                _dataExpressionNode.calculate(
                                        getConditionalNG().getSymbolTable()), false)
                        : null;
                if (formulaName == null) {
                    throw new IllegalArgumentException("invalid memory formula, name is null");
                }

                Memory formulaMem = InstanceManager.getDefault(MemoryManager.class).getMemory(formulaName);
                if (formulaMem == null) {
                    throw new IllegalArgumentException("invalid memory formula: " + formulaName);
                }
                formulaMem.setValue(data);
                break;

            default:
                throw new IllegalArgumentException("invalid _dataAddressing state: " + _dataAddressing.name());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {
        // Get the reporter bean
        Reporter reporter = _selectNamedBean.evaluateNamedBean(getConditionalNG());
        if (reporter == null) return;
        log.debug("reporter = {}", reporter.getDisplayName());

        // Get the reporter data
        Object data = getReporterData(reporter);
        log.debug("data = {}", data);

        // Update the destination
        updateDestination(data);
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
        return Bundle.getMessage(locale, "ActionReporter_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String bean = _selectNamedBean.getDescription(locale);
        String dest = "";

        switch (_dataAddressing) {
            case Direct:
                String memoryName = _selectMemoryNamedBean.getDescription(locale);
                dest = Bundle.getMessage(locale, "AddressByDirect", memoryName);
                break;

            case Reference:
                dest = Bundle.getMessage(locale, "AddressByReference", _dataReference);
                break;

            case LocalVariable:
                dest = Bundle.getMessage(locale, "AddressByLocalVariable", _dataLocalVariable);
                break;

            case Formula:
                dest = Bundle.getMessage(locale, "AddressByFormula", _dataFormula);
                break;

            default:
                throw new IllegalArgumentException("invalid _dataAddressing state: " + _dataAddressing.name());
        }

        String item = getReporterValue().toString();

        return Bundle.getMessage(locale, "ActionReporter_Long", item, bean, dest);
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
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

    public enum ReporterValue {
        CopyCurrentReport(Bundle.getMessage("ActionReporter_CopyCurrentReport")),
        CopyLastReport(Bundle.getMessage("ActionReporter_CopyLastReport")),
        CopyState(Bundle.getMessage("ActionReporter_CopyState"));

        private final String _text;

        private ReporterValue(String text) {
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
        _selectMemoryNamedBean.getUsageDetail(level, bean, report, cdl, this, LogixNG_SelectNamedBean.Type.Action);
    }

    /** {@inheritDoc} */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        getConditionalNG().execute();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionReporter.class);

}

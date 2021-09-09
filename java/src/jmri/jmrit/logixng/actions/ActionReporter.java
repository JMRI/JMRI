package jmri.jmrit.logixng.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.*;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.ReferenceUtil;
import jmri.jmrit.logixng.util.parser.*;
import jmri.jmrit.logixng.util.parser.ExpressionNode;
import jmri.jmrit.logixng.util.parser.RecursiveDescentParser;
import jmri.util.ThreadingUtil;
import jmri.util.TypeConversionUtil;

/**
 * This action triggers a reporter.
 *
 * @author Daniel Bergqvist Copyright 2021
 * @author Dave Sand Copyright 2021
 */
public class ActionReporter extends AbstractDigitalAction implements VetoableChangeListener {

    private NamedBeanAddressing _addressing = NamedBeanAddressing.Direct;
    private NamedBeanHandle<Reporter> _reporterHandle;
    private String _reference = "";
    private String _localVariable = "";
    private String _formula = "";
    private ExpressionNode _expressionNode;

    private NamedBeanAddressing _operationAddressing = NamedBeanAddressing.Direct;
    private DirectOperation _operationDirect = DirectOperation.CopyCurrentReport;
    private String _operationReference = "";
    private String _operationLocalVariable = "";
    private String _operationFormula = "";
    private ExpressionNode _operationExpressionNode;

    private NamedBeanAddressing _dataAddressing = NamedBeanAddressing.Direct;
    private String _dataReference = "";
    private String _dataLocalVariable = "";
    private String _dataFormula = "";
    private ExpressionNode _dataExpressionNode;

    private NamedBeanHandle<Memory> _memoryHandle;

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
        copy.setAddressing(_addressing);
        if (_reporterHandle != null) copy.setReporter(_reporterHandle);
        copy.setReference(_reference);
        copy.setLocalVariable(_localVariable);
        copy.setFormula(_formula);

        copy.setOperationAddressing(_operationAddressing);
        copy.setOperationDirect(_operationDirect);
        copy.setOperationReference(_operationReference);
        copy.setOperationLocalVariable(_operationLocalVariable);
        copy.setOperationFormula(_operationFormula);

        copy.setDataAddressing(_dataAddressing);
        copy.setDataReference(_dataReference);
        copy.setDataLocalVariable(_dataLocalVariable);
        copy.setDataFormula(_dataFormula);
        if (_memoryHandle != null) copy.setMemory(_memoryHandle);

        return manager.registerAction(copy);
    }

    public void setReporter(@Nonnull String reporterName) {
        assertListenersAreNotRegistered(log, "setReporter");
        Reporter reporter = InstanceManager.getDefault(jmri.ReporterManager.class).getNamedBean(reporterName);
        if (reporter != null) {
            ActionReporter.this.setReporter(reporter);
        } else {
            removeReporter();
            log.error("Reporter \"{}\" is not found", reporterName);
        }
    }

    public void setReporter(@Nonnull Reporter reporter) {
        assertListenersAreNotRegistered(log, "setReporter");
        ActionReporter.this.setReporter(InstanceManager.getDefault(NamedBeanHandleManager.class)
                .getNamedBeanHandle(reporter.getDisplayName(), reporter));
    }

    public void setReporter(@Nonnull NamedBeanHandle<Reporter> handle) {
        assertListenersAreNotRegistered(log, "setReporter");
        _reporterHandle = handle;
        InstanceManager.getDefault(ReporterManager.class).addVetoableChangeListener(this);
    }

    public void removeReporter() {
        assertListenersAreNotRegistered(log, "removeReporter");
        if (_reporterHandle != null) {
            InstanceManager.getDefault(ReporterManager.class).removeVetoableChangeListener(this);
            _reporterHandle = null;
        }
    }

    public NamedBeanHandle<Reporter> getReporter() {
        return _reporterHandle;
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


    public void setOperationAddressing(NamedBeanAddressing addressing) throws ParserException {
        _operationAddressing = addressing;
        parseOperationFormula();
    }

    public NamedBeanAddressing getOperationAddressing() {
        return _operationAddressing;
    }

    public void setOperationDirect(DirectOperation state) {
        _operationDirect = state;
    }

    public DirectOperation getOperationDirect() {
        return _operationDirect;
    }

    public void setOperationReference(@Nonnull String reference) {
        if ((! reference.isEmpty()) && (! ReferenceUtil.isReference(reference))) {
            throw new IllegalArgumentException("The reference \"" + reference + "\" is not a valid reference");
        }
        _operationReference = reference;
    }

    public String getOperationReference() {
        return _operationReference;
    }

    public void setOperationLocalVariable(@Nonnull String localVariable) {
        _operationLocalVariable = localVariable;
    }

    public String getOperationLocalVariable() {
        return _operationLocalVariable;
    }

    public void setOperationFormula(@Nonnull String formula) throws ParserException {
        _operationFormula = formula;
        parseOperationFormula();
    }

     public String getOperationFormula() {
        return _operationFormula;
    }

    private void parseOperationFormula() throws ParserException {
        if (_operationAddressing == NamedBeanAddressing.Formula) {
            Map<String, Variable> variables = new HashMap<>();

            RecursiveDescentParser parser = new RecursiveDescentParser(variables);
            _operationExpressionNode = parser.parseExpression(_operationFormula);
        } else {
            _operationExpressionNode = null;
        }
    }


    public void setMemory(@Nonnull String memoryName) {
        assertListenersAreNotRegistered(log, "setMemory");
        Memory memory = InstanceManager.getDefault(jmri.MemoryManager.class).getNamedBean(memoryName);
        if (memory != null) {
            ActionReporter.this.setMemory(memory);
        } else {
            removeMemory();
            log.error("Memory \"{}\" is not found", memoryName);
        }
    }

    public void setMemory(@Nonnull Memory memory) {
        assertListenersAreNotRegistered(log, "setMemory");
        ActionReporter.this.setMemory(InstanceManager.getDefault(NamedBeanHandleManager.class)
                .getNamedBeanHandle(memory.getDisplayName(), memory));
    }

    public void setMemory(@Nonnull NamedBeanHandle<Memory> handle) {
        assertListenersAreNotRegistered(log, "setMemory");
        _memoryHandle = handle;
        InstanceManager.getDefault(MemoryManager.class).addVetoableChangeListener(this);
    }

    public void removeMemory() {
        assertListenersAreNotRegistered(log, "removeMemory");
        if (_memoryHandle != null) {
            InstanceManager.getDefault(MemoryManager.class).removeVetoableChangeListener(this);
            _memoryHandle = null;
        }
    }

    public NamedBeanHandle<Memory> getMemory() {
        return _memoryHandle;
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


    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        if ("CanDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof Reporter) {
                if (evt.getOldValue().equals(getReporter().getBean())) {
                    PropertyChangeEvent e = new PropertyChangeEvent(this, "DoNotDelete", null, null);
                    throw new PropertyVetoException(Bundle.getMessage("ActionReporter_ReporterInUseVeto", getDisplayName()), e); // NOI18N
                }
            }
            if (evt.getOldValue() instanceof Memory) {
                if (evt.getOldValue().equals(getMemory().getBean())) {
                    PropertyChangeEvent e = new PropertyChangeEvent(this, "DoNotDelete", null, null);
                    throw new PropertyVetoException(Bundle.getMessage("ActionReporter_MemoryInUseVeto", getDisplayName()), e); // NOI18N
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
    public boolean isExternal() {
        return true;
    }

    private String getNewOperation() throws JmriException {

        switch (_operationAddressing) {
            case Reference:
                return ReferenceUtil.getReference(
                        getConditionalNG().getSymbolTable(), _operationReference);

            case LocalVariable:
                SymbolTable symbolTable = getConditionalNG().getSymbolTable();
                return TypeConversionUtil
                        .convertToString(symbolTable.getValue(_operationLocalVariable), false);

            case Formula:
                return _operationExpressionNode != null
                        ? TypeConversionUtil.convertToString(
                                _operationExpressionNode.calculate(
                                        getConditionalNG().getSymbolTable()), false)
                        : null;

            default:
                throw new IllegalArgumentException("invalid _addressing state: " + _operationAddressing.name());
        }
    }

    private String getNewData() throws JmriException {

        switch (_dataAddressing) {
            case Direct:
                if (_memoryHandle != null) {
                    return _memoryHandle.getBean().getDisplayName();
                }
                return "";

            case Reference:
                return ReferenceUtil.getReference(
                        getConditionalNG().getSymbolTable(), _dataReference);

            case LocalVariable:
                log.info("getNewData: lv = {}", _dataLocalVariable);
                SymbolTable symbolTable = getConditionalNG().getSymbolTable();
                return TypeConversionUtil
                        .convertToString(symbolTable.getValue(_dataLocalVariable), false);

            case Formula:
                return _dataExpressionNode != null
                        ? TypeConversionUtil.convertToString(
                                _dataExpressionNode.calculate(
                                        getConditionalNG().getSymbolTable()), false)
                        : null;

            default:
                throw new IllegalArgumentException("invalid _dataAddressing state: " + _dataAddressing.name());
        }
    }

    Reporter getSourceReporter() throws JmriException {
        Reporter reporter = null;
        switch (_addressing) {
            case Direct:
                reporter = _reporterHandle != null ? _reporterHandle.getBean() : null;
                break;

            case Reference:
                String ref = ReferenceUtil.getReference(
                        getConditionalNG().getSymbolTable(), _reference);
                reporter = InstanceManager.getDefault(jmri.ReporterManager.class)
                        .getNamedBean(ref);
                break;

            case LocalVariable:
                SymbolTable symbolTable = getConditionalNG().getSymbolTable();
                reporter = InstanceManager.getDefault(ReporterManager.class)
                        .getNamedBean(TypeConversionUtil
                                .convertToString(symbolTable.getValue(_localVariable), false));
                break;

            case Formula:
                reporter = _expressionNode != null ?
                        InstanceManager.getDefault(ReporterManager.class)
                                .getNamedBean(TypeConversionUtil
                                        .convertToString(_expressionNode.calculate(
                                                getConditionalNG().getSymbolTable()), false))
                        : null;
                break;

            default:
                throw new IllegalArgumentException("invalid _addressing state: " + _addressing.name());
        }
        return reporter;
    }

    Object getReporterData(Reporter reporter) throws JmriException {
        DirectOperation oper;

        String name = (_operationAddressing != NamedBeanAddressing.Direct)
                ? getNewOperation() : null;

        if ((_operationAddressing == NamedBeanAddressing.Direct)) {
            oper = _operationDirect;    // combo selection
        } else {
            oper = DirectOperation.valueOf(name);   // convert from string name
        }

        Object obj;
        switch (oper) {
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
                throw new IllegalArgumentException("invalid oper state: " + oper.name());
        }
        return obj;
    }

    void updateDestination(Object data) throws JmriException {
        switch (_dataAddressing) {
            case Direct:
                if (_memoryHandle != null) {
                    _memoryHandle.getBean().setValue(data);
                }
                break;

            case Reference:
                String refName = ReferenceUtil.getReference(
                        getConditionalNG().getSymbolTable(), _dataReference);
                log.info("ref ref = {}, name = {}", _dataReference, refName);
                Memory refMem = InstanceManager.getDefault(MemoryManager.class).getMemory(refName);
                if (refMem == null) {
                    throw new IllegalArgumentException("invalid memory reference: " + refName);
                }
                refMem.setValue(data);
                break;

            case LocalVariable:
                log.info("LocalVariable: lv = {}", _dataLocalVariable);
                getConditionalNG().getSymbolTable().setValue(_dataLocalVariable, data);
                break;

            case Formula:
//                 return _dataExpressionNode != null
//                         ? TypeConversionUtil.convertToString(
//                                 _dataExpressionNode.calculate(
//                                         getConditionalNG().getSymbolTable()), false)
//                         : null;
                break;

            default:
                throw new IllegalArgumentException("invalid _dataAddressing state: " + _dataAddressing.name());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {
        // Get the reporter bean
        Reporter reporter = getSourceReporter();
        if (reporter == null) return;
        log.info("reporter = {}", reporter.getDisplayName());

        // Get the reporter data
        Object data = getReporterData(reporter);
        log.info("data = {}", data);

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
        String bean = "";
        String item = "";
        String dest = "";

        switch (_addressing) {
            case Direct:
                String reporterName;
                if (_reporterHandle != null) {
                    reporterName = _reporterHandle.getBean().getDisplayName();
                } else {
                    reporterName = Bundle.getMessage(locale, "BeanNotSelected");
                }
                bean = Bundle.getMessage(locale, "AddressByDirect", reporterName);
                break;

            case Reference:
                bean = Bundle.getMessage(locale, "AddressByReference", _reference);
                break;

            case LocalVariable:
                bean = Bundle.getMessage(locale, "AddressByLocalVariable", _localVariable);
                break;

            case Formula:
                bean = Bundle.getMessage(locale, "AddressByFormula", _formula);
                break;

            default:
                throw new IllegalArgumentException("invalid _addressing state: " + _addressing.name());
        }

        switch (_operationAddressing) {
            case Direct:
                item = getOperationDirect().toString();
                break;

            case Reference:
                item = Bundle.getMessage(locale, "AddressByReference", _operationReference);
                break;

            case LocalVariable:
                item = Bundle.getMessage(locale, "AddressByLocalVariable", _operationLocalVariable);
                break;

            case Formula:
                item = Bundle.getMessage(locale, "AddressByFormula", _operationFormula);
                break;

            default:
                throw new IllegalArgumentException("invalid _stateAddressing state: " + _operationAddressing.name());
        }

        switch (_dataAddressing) {
            case Direct:
                String memoryName;
                if (_memoryHandle != null) {
                    memoryName = _memoryHandle.getBean().getDisplayName();
                } else {
                    memoryName = Bundle.getMessage(locale, "BeanNotSelected");
                }
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
// Copy reporter "{0}" {1} to {2}
        return Bundle.getMessage(locale, "ActionReporter_Long", bean, item, dest);
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
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

    public enum DirectOperation {
        CopyCurrentReport(Bundle.getMessage("ActionReporter_CopyCurrentReport")),
        CopyLastReport(Bundle.getMessage("ActionReporter_CopyLastReport")),
        CopyState(Bundle.getMessage("ActionReporter_CopyState"));

        private final String _text;

        private DirectOperation(String text) {
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
        log.debug("getUsageReport :: ActionReporter: bean = {}, report = {}", cdl, report);
        if (getReporter() != null && bean.equals(getReporter().getBean())) {
            report.add(new NamedBeanUsageReport("LogixNGAction", cdl, getLongDescription()));
        }
        if (getMemory() != null && bean.equals(getMemory().getBean())) {
            report.add(new NamedBeanUsageReport("LogixNGAction", cdl, getLongDescription()));
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionReporter.class);

}

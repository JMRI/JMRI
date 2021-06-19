package jmri.jmrit.logixng.actions;

import java.beans.*;
import java.util.*;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.parser.*;
import jmri.jmrit.logixng.util.parser.ExpressionNode;

/**
 * This action sets the value of a local variable.
 *
 * @author Daniel Bergqvist Copyright 2020
 */
public class ActionLocalVariable extends AbstractDigitalAction
        implements PropertyChangeListener, VetoableChangeListener {

    private String _localVariable;
    private NamedBeanHandle<Memory> _memoryHandle;
    private VariableOperation _variableOperation = VariableOperation.SetToString;
    private String _constantValue = "";
    private String _otherLocalVariable = "";
    private String _formula = "";
    private ExpressionNode _expressionNode;
    private boolean _listenToMemory = true;
//    private boolean _listenToMemory = false;

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
        copy.setOtherLocalVariable(_localVariable);
        copy.setFormula(_formula);
        copy.setListenToMemory(_listenToMemory);
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

    public void setVariableOperation(VariableOperation variableOperation) throws ParserException {
        _variableOperation = variableOperation;
        parseFormula();
    }

    public VariableOperation getVariableOperation() {
        return _variableOperation;
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
        } else if ("DoDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof Memory) {
                if (evt.getOldValue().equals(_memoryHandle.getBean())) {
                    removeMemory();
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

    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {
        if (_localVariable == null) return;

        SymbolTable symbolTable = getConditionalNG().getSymbolTable();

        switch (_variableOperation) {
            case SetToNull:
                symbolTable.setValue(_localVariable, null);
                break;

            case SetToString:
                symbolTable.setValue(_localVariable, _constantValue);
                break;

            case CopyVariableToVariable:
                Object variableValue = getConditionalNG()
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

            case CalculateFormula:
                if (_formula.isEmpty()) {
                    symbolTable.setValue(_localVariable, null);
                } else {
                    if (_expressionNode == null) return;

                    symbolTable.setValue(_localVariable,
                            _expressionNode.calculate(
                                    getConditionalNG().getSymbolTable()));
                }
                break;

            default:
                // Throw exception
                throw new IllegalArgumentException("_memoryOperation has invalid value: {}" + _variableOperation.name());
        }
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

        switch (_variableOperation) {
            case SetToNull:
                return Bundle.getMessage(locale, "ActionLocalVariable_Long_Null", _localVariable);
            case SetToString:
                return Bundle.getMessage(locale, "ActionLocalVariable_Long_Value", _localVariable, _constantValue);
            case CopyVariableToVariable:
                return Bundle.getMessage(locale, "ActionLocalVariable_Long_CopyVariableToVariable", _localVariable, _otherLocalVariable);
            case CopyMemoryToVariable:
                return Bundle.getMessage(locale, "ActionLocalVariable_Long_CopyMemoryToVariable", _localVariable, copyToMemoryName);
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
                _memoryHandle.getBean().addPropertyChangeListener("value", this);
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

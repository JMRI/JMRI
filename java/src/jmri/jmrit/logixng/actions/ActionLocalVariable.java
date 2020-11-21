package jmri.jmrit.logixng.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.parser.*;
import jmri.jmrit.logixng.util.parser.ExpressionNode;
import jmri.util.ThreadingUtil;

/**
 * This action sets the value of a local variable.
 * 
 * @author Daniel Bergqvist Copyright 2020
 */
public class ActionLocalVariable extends AbstractDigitalAction implements VetoableChangeListener {

    private String _variableName;
    private NamedBeanHandle<Memory> _memoryHandle;
    private VariableOperation _variableOperation = VariableOperation.SetToString;
    private String _data = "";
    private ExpressionNode _expressionNode;
    
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
        copy.setVariable(_variableName);
        copy.setMemory(_memoryHandle);
        copy.setVariableOperation(_variableOperation);
        copy.setData(_data);
        return manager.registerAction(copy);
    }
    
    public void setVariable(String variableName) {
        assertListenersAreNotRegistered(log, "setMemory");
        _variableName = variableName;
    }
    
    public String getVariableName() {
        return _variableName;
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
    
    public void setVariableOperation(VariableOperation variableOperation) throws ParserException {
        _variableOperation = variableOperation;
        parseFormula();
    }
    
    public VariableOperation getVariableOperation() {
        return _variableOperation;
    }

    public void setData(String newValue) throws ParserException {
        _data = newValue;
        parseFormula();
    }
    
    public String getData() {
        return _data;
    }
    
    private void parseFormula() throws ParserException {
        if (_variableOperation == VariableOperation.CalculateFormula) {
            Map<String, Variable> variables = new HashMap<>();
            
            RecursiveDescentParser parser = new RecursiveDescentParser(variables);
            _expressionNode = parser.parseExpression(_data);
        } else {
            _expressionNode = null;
        }
    }
    
    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        if ("CanDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof Memory) {
                if (evt.getOldValue().equals(_memoryHandle.getBean())) {
                    PropertyChangeEvent e = new PropertyChangeEvent(this, "DoNotDelete", null, null);
                    throw new PropertyVetoException(Bundle.getMessage("Memory_MemoryInUseMemoryExpressionVeto", getDisplayName()), e); // NOI18N
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
        if (_variableName == null) return;
        
        SymbolTable symbolTable =
                InstanceManager.getDefault(LogixNG_Manager.class).getSymbolTable();
        
        AtomicReference<JmriException> ref = new AtomicReference<>();
        
        ThreadingUtil.runOnLayout(() -> {
            
            switch (_variableOperation) {
                case SetToNull:
                    symbolTable.setValue(_variableName, null);
                    break;
                    
                case SetToString:
                    symbolTable.setValue(_variableName, _data);
                    break;
                    
                case CopyVariableToVariable:
                    Object variableValue =
                            InstanceManager.getDefault(LogixNG_Manager.class)
                                    .getSymbolTable().getValue(_data);
                    
                    symbolTable.setValue(_variableName, variableValue);
                    break;
                    
                case CopyMemoryToVariable:
                    if (_memoryHandle != null) {
                        symbolTable.setValue(_variableName, _memoryHandle.getBean().getValue());
                    } else {
                        log.error("setLocalVariable should copy memory to variable but memory is null");
                    }
                    break;
                    
                case CalculateFormula:
                    if (_data.isEmpty()) {
                        symbolTable.setValue(_variableName, null);
                    } else {
                        try {
                            if (_expressionNode == null) {
                                return;
                            }
                            symbolTable.setValue(_variableName, _expressionNode.calculate());
                        } catch (JmriException e) {
                            ref.set(e);
                        }
                    }
                    break;
                    
                default:
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
        return Bundle.getMessage(locale, "LocalVariable_Short");
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
                return Bundle.getMessage(locale, "LocalVariable_Long_Null", _variableName);
            case SetToString:
                return Bundle.getMessage(locale, "LocalVariable_Long_Value", _variableName, _data);
            case CopyVariableToVariable:
                return Bundle.getMessage(locale, "LocalVariable_Long_CopyVariableToVariable", _data, _variableName);
            case CopyMemoryToVariable:
                return Bundle.getMessage(locale, "LocalVariable_Long_CopyMemoryToVariable", copyToMemoryName, _variableName);
            case CalculateFormula:
                return Bundle.getMessage(locale, "LocalVariable_Long_Formula", _variableName, _data);
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
    }
    
    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
    }
    
    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }
    
    
    public enum VariableOperation {
        SetToNull,
        SetToString,
        CopyVariableToVariable,
        CopyMemoryToVariable,
        CalculateFormula;
    }
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionLocalVariable.class);
    
}

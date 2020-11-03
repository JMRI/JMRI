package jmri.jmrit.logixng.digital.actions;

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
import jmri.jmrit.logixng.util.parser.expressionnode.ExpressionNode;
import jmri.jmrit.logixng.util.parser.variables.LocalVariableExpressionVariable;
import jmri.util.ThreadingUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This action sets the value of a memory.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public class ActionLocalVariable extends AbstractDigitalAction implements VetoableChangeListener {

    private String _variableName;
    private NamedBeanHandle<Memory> _otherMemoryHandle;
    private VariableOperation _variableOperation = VariableOperation.SET_TO_STRING;
    private String _data = "";
    private ExpressionNode _expressionNode;
    
    public ActionLocalVariable(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }
    
    public void setVariable(String variableName) {
        assertListenersAreNotRegistered(log, "setMemory");
        _variableName = variableName;
    }
    
    public String getVariableName() {
        return _variableName;
    }
    
    public void setOtherMemory(String memoryName) {
        MemoryManager memoryManager = InstanceManager.getDefault(MemoryManager.class);
        Memory memory = memoryManager.getMemory(memoryName);
        if (memory != null) {
            _otherMemoryHandle = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(memoryName, memory);
            memoryManager.addVetoableChangeListener(this);
        } else {
            _otherMemoryHandle = null;
            memoryManager.removeVetoableChangeListener(this);
        }
    }
    
    public void setOtherMemory(NamedBeanHandle<Memory> handle) {
        _otherMemoryHandle = handle;
        if (_otherMemoryHandle != null) {
            InstanceManager.getDefault(MemoryManager.class).addVetoableChangeListener(this);
        } else {
            InstanceManager.getDefault(MemoryManager.class).removeVetoableChangeListener(this);
        }
    }
    
    public void setOtherMemory(@CheckForNull Memory memory) {
        if (memory != null) {
            _otherMemoryHandle = InstanceManager.getDefault(NamedBeanHandleManager.class)
                    .getNamedBeanHandle(memory.getDisplayName(), memory);
            InstanceManager.getDefault(MemoryManager.class).addVetoableChangeListener(this);
        } else {
            _otherMemoryHandle = null;
            InstanceManager.getDefault(MemoryManager.class).removeVetoableChangeListener(this);
        }
    }
    
    public NamedBeanHandle<Memory> getOtherMemory() {
        return _otherMemoryHandle;
    }
    
    public void setVariableOperation(VariableOperation memoryOperation) throws ParserException {
        _variableOperation = memoryOperation;
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
        if (_variableOperation == VariableOperation.FORMULA) {
            Map<String, Variable> variables = new HashMap<>();
            
            RecursiveDescentParser parser = new RecursiveDescentParser(variables);
            _expressionNode = parser.parseExpression(_data);
        } else {
            _expressionNode = null;
        }
    }
    
    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
/*        
        if ("CanDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof Memory) {
                if (evt.getOldValue().equals(getMemory().getBean())) {
                    PropertyChangeEvent e = new PropertyChangeEvent(this, "DoNotDelete", null, null);
                    throw new PropertyVetoException(Bundle.getMessage("Memory_MemoryInUseMemoryExpressionVeto", getDisplayName()), e); // NOI18N
                }
            }
        } else if ("DoDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof Memory) {
                if (evt.getOldValue().equals(getMemory().getBean())) {
                    removeMemory();
                }
            }
        }
*/        
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
                case SET_TO_NULL:
                    symbolTable.setValue(_variableName, null);
                    break;
                    
                case SET_TO_STRING:
                    symbolTable.setValue(_variableName, _data);
                    break;
                    
                case COPY_VARIABLE_TO_VARIABLE:
                    Object variableValue =
                            InstanceManager.getDefault(LogixNG_Manager.class)
                                    .getSymbolTable().getValue(_data);
                    
                    symbolTable.setValue(_variableName, variableValue);
                    break;
                    
                case COPY_MEMORY_TO_VARIABLE:
                    if (_otherMemoryHandle != null) {
                        symbolTable.setValue(_variableName, _otherMemoryHandle.getBean().getValue());
                    } else {
                        log.error("setLocalVariable should copy memory to variable but memory is null");
                    }
                    break;
                    
                case FORMULA:
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
        if (_otherMemoryHandle != null) {
            copyToMemoryName = _otherMemoryHandle.getBean().getDisplayName();
        } else {
            copyToMemoryName = Bundle.getMessage(locale, "BeanNotSelected");
        }
        
        switch (_variableOperation) {
            case SET_TO_NULL:
                return Bundle.getMessage(locale, "LocalVariable_Long_Null", _variableName);
            case SET_TO_STRING:
                return Bundle.getMessage(locale, "LocalVariable_Long_Value", _variableName, _data);
            case COPY_VARIABLE_TO_VARIABLE:
                return Bundle.getMessage(locale, "LocalVariable_Long_CopyVariableToVariable", _data, _variableName);
            case COPY_MEMORY_TO_VARIABLE:
                return Bundle.getMessage(locale, "LocalVariable_Long_CopyMemoryToVariable", copyToMemoryName, _variableName);
            case FORMULA:
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
        SET_TO_NULL,
        SET_TO_STRING,
        COPY_VARIABLE_TO_VARIABLE,
        COPY_MEMORY_TO_VARIABLE,
        FORMULA;
    }
    
    private final static Logger log = LoggerFactory.getLogger(ActionLocalVariable.class);
    
}

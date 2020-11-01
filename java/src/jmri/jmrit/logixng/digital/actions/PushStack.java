package jmri.jmrit.logixng.digital.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.parser.*;
import jmri.jmrit.logixng.util.parser.expressionnode.ExpressionNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This action reads the value of a memory and pushes it on a stack.
 * 
 * @author Daniel Bergqvist Copyright 2020
 */
public class PushStack extends AbstractDigitalAction implements VetoableChangeListener {

    private NamedBeanHandle<Stack> _stackHandle;
    private NamedBeanHandle<Memory> _memoryHandle;
    private ExpressionNode _expressionNode;
    private String _data = "";
    private Operation _operation = Operation.MEMORY;
    
    public PushStack(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }
    
    public void setMemory(@Nonnull String memoryName) {
        assertListenersAreNotRegistered(log, "setMemory");
        Memory memory = InstanceManager.getDefault(MemoryManager.class).getMemory(memoryName);
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
        InstanceManager.memoryManagerInstance().addVetoableChangeListener(this);
    }
    
    public void setMemory(@Nonnull Memory memory) {
        assertListenersAreNotRegistered(log, "setMemory");
        setMemory(InstanceManager.getDefault(NamedBeanHandleManager.class)
                .getNamedBeanHandle(memory.getDisplayName(), memory));
    }
    
    public void removeMemory() {
        assertListenersAreNotRegistered(log, "setMemory");
        if (_memoryHandle != null) {
            InstanceManager.memoryManagerInstance().removeVetoableChangeListener(this);
            _memoryHandle = null;
        }
    }
    
    public NamedBeanHandle<Memory> getMemory() {
        return _memoryHandle;
    }
    
    public void setStack(@Nonnull String stackName) {
        assertListenersAreNotRegistered(log, "setStack");
        NamedTable stack = InstanceManager.getDefault(NamedTableManager.class).getBySystemName(stackName);
        if (stack == null) {
            removeStack();
            log.error("stack \"{}\" is not found", stackName);
        } else if (! (stack instanceof Stack)) {
            removeStack();
            log.error("stack \"{}\" is not a Stack", stackName);
        } else {
            setStack((Stack)stack);
        }
    }
    
    public void setStack(@Nonnull NamedBeanHandle<Stack> handle) {
        assertListenersAreNotRegistered(log, "setStack");
        _stackHandle = handle;
        InstanceManager.memoryManagerInstance().addVetoableChangeListener(this);
    }
    
    public void setStack(@Nonnull Stack stack) {
        assertListenersAreNotRegistered(log, "setMemory");
        setStack(InstanceManager.getDefault(NamedBeanHandleManager.class)
                .getNamedBeanHandle(stack.getDisplayName(), stack));
    }
    
    public void removeStack() {
        assertListenersAreNotRegistered(log, "removeStack");
        if (_stackHandle != null) {
            InstanceManager.memoryManagerInstance().removeVetoableChangeListener(this);
            _stackHandle = null;
        }
    }
    
    public NamedBeanHandle<Stack> getStack() {
        return _stackHandle;
    }
    
    public void setOperation(Operation operation) throws ParserException {
        _operation = operation;
        parseFormula();
    }
    
    public Operation getOperation() {
        return _operation;
    }
    
    public void setData(String data) throws ParserException {
        _data = data;
        parseFormula();
    }
    
    public String getData() {
        return _data;
    }
    
    private void parseFormula() throws ParserException {
        if (_operation == Operation.FORMULA) {
            Map<String, Variable> variables = new HashMap<>();
            RecursiveDescentParser parser = new RecursiveDescentParser(variables);
            try {
            _expressionNode = parser.parseExpression(_data);
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        } else {
            _expressionNode = null;
        }
    }
    
    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        if ("CanDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof Memory) {
                if (evt.getOldValue().equals(getMemory().getBean())) {
                    PropertyChangeEvent e = new PropertyChangeEvent(this, "DoNotDelete", null, null);
                    throw new PropertyVetoException(Bundle.getMessage("PushStack_MemoryInUseMemoryExpressionVeto", getDisplayName()), e); // NOI18N
                }
            }
            if (evt.getOldValue() instanceof Stack) {
                if (evt.getOldValue().equals(getStack().getBean())) {
                    PropertyChangeEvent e = new PropertyChangeEvent(this, "DoNotDelete", null, null);
                    throw new PropertyVetoException(Bundle.getMessage("PushStack_StackInUseMemoryExpressionVeto", getDisplayName()), e); // NOI18N
                }
            }
        } else if ("DoDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof Memory) {
                if (evt.getOldValue().equals(getMemory().getBean())) {
                    removeMemory();
                }
            }
            if (evt.getOldValue() instanceof Stack) {
                if (evt.getOldValue().equals(getStack().getBean())) {
                    removeStack();
                }
            }
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.OTHER;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isExternal() {
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {
        
        final Stack stack = _stackHandle.getBean();
        
        System.out.format("%s: Push on stack %s. Operation: %s%n", getSystemName(), stack.getSystemName(), _operation.name());
        
        Object value;
        
        switch (_operation) {
            case STRING:
                value = _data;
                break;
                
            case MEMORY:
                if (_memoryHandle == null) return;
                value = _memoryHandle.getBean().getValue();
                break;
                
            case FORMULA:
                System.out.format("aaa%n");
                if (_data.isEmpty()) {
                    System.out.format("bbb%n");
                    value = null;
                } else {
                    value = _expressionNode.calculate();
                }
                System.out.format("eee%n");
                break;
                
            default:
                System.out.format("%s: Push on stack %s. _operation has invalid value: %s%n", getSystemName(), stack.getSystemName(), _operation.name());
                throw new IllegalArgumentException("_operation has invalid value: " + _operation.name());
        }
        
        System.out.format("%s: Push %s on stack %s. Operation: %s%n", getSystemName(), value, stack.getSystemName(), _operation.name());
        
        stack.push(value);
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
        return Bundle.getMessage(locale, "PushStack_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String memoryName;
        if (_memoryHandle != null) {
            memoryName = _memoryHandle.getBean().getDisplayName();
        } else {
            memoryName = Bundle.getMessage(locale, "BeanNotSelected");
        }
        
        String stackName;
        if (_stackHandle != null) {
            stackName = _stackHandle.getBean().getDisplayName();
        } else {
            stackName = Bundle.getMessage(locale, "BeanNotSelected");
        }
        
        switch (_operation) {
            case STRING:
                return Bundle.getMessage(locale, "PushStack_Long_String", _data, stackName);
            case MEMORY:
                return Bundle.getMessage(locale, "PushStack_Long_Memory", memoryName, stackName);
            case FORMULA:
                return Bundle.getMessage(locale, "PushStack_Long_Formula", _data, stackName);
            default:
                throw new IllegalArgumentException("_memoryOperation has invalid value: {}" + _operation.name());
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
        removeMemory();
        removeStack();
    }
    
    
    public enum Operation {
        STRING,
        MEMORY,
        FORMULA;
    }
    
    private final static Logger log = LoggerFactory.getLogger(PushStack.class);
    
}

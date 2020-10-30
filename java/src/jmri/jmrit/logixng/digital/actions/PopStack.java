package jmri.jmrit.logixng.digital.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.Locale;

import javax.annotation.Nonnull;

import jmri.InstanceManager;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.jmrit.logixng.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This action pops a value from a stack and writes the value to a memory.
 * 
 * @author Daniel Bergqvist Copyright 2020
 */
public class PopStack extends AbstractDigitalAction implements VetoableChangeListener {

    private NamedBeanHandle<Memory> _memoryHandle;
    private NamedBeanHandle<Stack> _stackHandle;
    
    public PopStack(String sys, String user)
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
    public void execute() {
        if (_memoryHandle == null) return;
        
        final Memory memory = _memoryHandle.getBean();
        final Stack stack = _stackHandle.getBean();
        
        memory.setValue(stack.pop());
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
        
        return Bundle.getMessage(locale, "PushStack_Long", memoryName, stackName);
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
    
    
    public enum MemoryOperation {
        SET_TO_NULL,
        SET_TO_STRING,
        COPY_MEMORY;
    }
    
    private final static Logger log = LoggerFactory.getLogger(PopStack.class);
    
}

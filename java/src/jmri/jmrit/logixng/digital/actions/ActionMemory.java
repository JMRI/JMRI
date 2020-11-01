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
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.FemaleSocket;
import jmri.jmrit.logixng.util.parser.*;
import jmri.jmrit.logixng.util.parser.expressionnode.ExpressionNode;
import jmri.util.ThreadingUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This action sets the value of a memory.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public class ActionMemory extends AbstractDigitalAction implements VetoableChangeListener {

    private NamedBeanHandle<Memory> _memoryHandle;
    private NamedBeanHandle<Memory> _copyToMemoryHandle;
    private MemoryOperation _memoryOperation = MemoryOperation.SET_TO_STRING;
    private String _data = "";
    private ExpressionNode _expressionNode;
    
    public ActionMemory(String sys, String user)
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
    
    public void setCopyToMemoryName(String memoryName) {
        MemoryManager memoryManager = InstanceManager.getDefault(MemoryManager.class);
        Memory memory = memoryManager.getMemory(memoryName);
        if (memory != null) {
            _copyToMemoryHandle = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(memoryName, memory);
            memoryManager.addVetoableChangeListener(this);
        } else {
            _copyToMemoryHandle = null;
            memoryManager.removeVetoableChangeListener(this);
        }
    }
    
    public void setCopyToMemory(NamedBeanHandle<Memory> handle) {
        _copyToMemoryHandle = handle;
        if (_copyToMemoryHandle != null) {
            InstanceManager.getDefault(MemoryManager.class).addVetoableChangeListener(this);
        } else {
            InstanceManager.getDefault(MemoryManager.class).removeVetoableChangeListener(this);
        }
    }
    
    public void setCopyToMemory(@CheckForNull Memory memory) {
        if (memory != null) {
            _copyToMemoryHandle = InstanceManager.getDefault(NamedBeanHandleManager.class)
                    .getNamedBeanHandle(memory.getDisplayName(), memory);
            InstanceManager.getDefault(MemoryManager.class).addVetoableChangeListener(this);
        } else {
            _copyToMemoryHandle = null;
            InstanceManager.getDefault(MemoryManager.class).removeVetoableChangeListener(this);
        }
    }
    
    public NamedBeanHandle<Memory> getCopyToMemory() {
        return _copyToMemoryHandle;
    }
    
    public void setMemoryOperation(MemoryOperation state) throws ParserException {
        _memoryOperation = state;
        parseFormula();
    }
    
    public MemoryOperation getMemoryOperation() {
        return _memoryOperation;
    }
    
    public void setData(String newValue) throws ParserException {
        _data = newValue;
        parseFormula();
    }
    
    public String getData() {
        return _data;
    }
    
    private void parseFormula() throws ParserException {
        if (_memoryOperation == MemoryOperation.FORMULA) {
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
        if (_memoryHandle == null) return;
        
        final Memory memory = _memoryHandle.getBean();
        
        System.out.format("ActionMemory %s: %s, %s, %s%n", getSystemName(), memory.getSystemName(), _data, _memoryOperation.name());
        
        AtomicReference<JmriException> ref = new AtomicReference<>();
        
        ThreadingUtil.runOnLayout(() -> {
            switch (_memoryOperation) {
                case SET_TO_NULL:
                    memory.setValue(null);
                    break;
                    
                case SET_TO_STRING:
                    memory.setValue(_data);
                    break;
                    
                case COPY_MEMORY:
                    if (_copyToMemoryHandle != null) {
                        _copyToMemoryHandle.getBean().setValue(memory.getValue());
                    } else {
                        log.error("setMemory should copy to memory but destination memory is null");
                    }
                    break;
                    
                case FORMULA:
                    if (_data.isEmpty()) {
                        memory.setValue(null);
                    } else {
                        try {
                            memory.setValue(_expressionNode.calculate());
                        } catch (JmriException e) {
                            ref.set(e);
                        }
                    }
                    break;
                    
                default:
                    throw new IllegalArgumentException("_memoryOperation has invalid value: {}" + _memoryOperation.name());
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
        return Bundle.getMessage(locale, "Memory_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String memoryName;
        if (_memoryHandle != null) {
            memoryName = _memoryHandle.getBean().getDisplayName();
        } else {
            memoryName = Bundle.getMessage(locale, "BeanNotSelected");
        }
        
        String copyToMemoryName;
        if (_copyToMemoryHandle != null) {
            copyToMemoryName = _copyToMemoryHandle.getBean().getDisplayName();
        } else {
            copyToMemoryName = Bundle.getMessage(locale, "BeanNotSelected");
        }
        
        switch (_memoryOperation) {
            case SET_TO_NULL:
                return Bundle.getMessage(locale, "Memory_Long_Null", memoryName);
            case SET_TO_STRING:
                return Bundle.getMessage(locale, "Memory_Long_Value", memoryName, _data);
            case COPY_MEMORY:
                return Bundle.getMessage(locale, "Memory_Long_CopyMemory", memoryName, copyToMemoryName);
            case FORMULA:
                return Bundle.getMessage(locale, "Memory_Long_Formula", memoryName, _data);
            default:
                throw new IllegalArgumentException("_memoryOperation has invalid value: " + _memoryOperation.name());
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
    
    
    public enum MemoryOperation {
        SET_TO_NULL,
        SET_TO_STRING,
        COPY_MEMORY,
        FORMULA;
    }
    
    private final static Logger log = LoggerFactory.getLogger(ActionMemory.class);
    
}

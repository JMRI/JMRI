package jmri.jmrit.logixng.digital.actions;

import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.Locale;
import javax.annotation.CheckForNull;
import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.FemaleSocket;
import jmri.util.ThreadingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This action sets the value of a memory.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public class ActionMemory extends AbstractDigitalAction implements VetoableChangeListener {

//    private ActionTurnout _template;
    private NamedBeanHandle<Memory> _memoryHandle;
    private NamedBeanHandle<Memory> _copyFromMemoryHandle;
    private MemoryOperation _memoryOperation = MemoryOperation.SET_TO_NULL;
    private String _newStringValue = "";
    
    public ActionMemory(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }
    
    private ActionMemory(ActionMemory template) {
        super(InstanceManager.getDefault(DigitalActionManager.class).getAutoSystemName(), null);
//        _template = template;
    }
    
    /** {@inheritDoc} */
    @Override
    public Base getNewObjectBasedOnTemplate() {
        return new ActionMemory(this);
    }
    
    public void setMemoryName(String memoryName) {
        MemoryManager memoryManager = InstanceManager.getDefault(MemoryManager.class);
        Memory memory = memoryManager.getMemory(memoryName);
        if (memory != null) {
            _memoryHandle = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(memoryName, memory);
            memoryManager.addVetoableChangeListener(this);
        } else {
            _memoryHandle = null;
            memoryManager.removeVetoableChangeListener(this);
        }
    }
    
    public void setMemory(NamedBeanHandle<Memory> handle) {
        _memoryHandle = handle;
        if (_memoryHandle != null) {
            InstanceManager.getDefault(MemoryManager.class).addVetoableChangeListener(this);
        } else {
            InstanceManager.getDefault(MemoryManager.class).removeVetoableChangeListener(this);
        }
    }
    
    public void setMemory(@CheckForNull Memory memory) {
        if (memory != null) {
            _memoryHandle = InstanceManager.getDefault(NamedBeanHandleManager.class)
                    .getNamedBeanHandle(memory.getDisplayName(), memory);
            // I have a bug here for every action/expression that does this. If 'this' is already registred,
            // I should not register it again.
            InstanceManager.getDefault(MemoryManager.class).addVetoableChangeListener(this);
        } else {
            _memoryHandle = null;
            InstanceManager.getDefault(MemoryManager.class).removeVetoableChangeListener(this);
        }
    }
    
    public NamedBeanHandle<Memory> getMemory() {
        return _memoryHandle;
    }
    
    public void setCopyFromMemoryName(String memoryName) {
        MemoryManager memoryManager = InstanceManager.getDefault(MemoryManager.class);
        Memory memory = memoryManager.getMemory(memoryName);
        if (memory != null) {
            _copyFromMemoryHandle = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(memoryName, memory);
            memoryManager.addVetoableChangeListener(this);
        } else {
            _copyFromMemoryHandle = null;
            memoryManager.removeVetoableChangeListener(this);
        }
    }
    
    public void setCopyFromMemory(NamedBeanHandle<Memory> handle) {
        _copyFromMemoryHandle = handle;
        if (_copyFromMemoryHandle != null) {
            InstanceManager.getDefault(MemoryManager.class).addVetoableChangeListener(this);
        } else {
            InstanceManager.getDefault(MemoryManager.class).removeVetoableChangeListener(this);
        }
    }
    
    public void setCopyFromMemory(@CheckForNull Memory memory) {
        if (memory != null) {
            _copyFromMemoryHandle = InstanceManager.getDefault(NamedBeanHandleManager.class)
                    .getNamedBeanHandle(memory.getDisplayName(), memory);
            InstanceManager.getDefault(MemoryManager.class).addVetoableChangeListener(this);
        } else {
            _copyFromMemoryHandle = null;
            InstanceManager.getDefault(MemoryManager.class).removeVetoableChangeListener(this);
        }
    }
    
    public NamedBeanHandle<Memory> getCopyFromMemory() {
        return _copyFromMemoryHandle;
    }
    
    public void setTurnoutState(MemoryOperation state) {
        _memoryOperation = state;
    }
    
    public MemoryOperation getTurnoutState() {
        return _memoryOperation;
    }
    
    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        if ("CanDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof Memory) {
                if (evt.getOldValue().equals(getMemory().getBean())) {
                    throw new PropertyVetoException(getDisplayName(), evt);
                }
            }
        } else if ("DoDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof Memory) {
                if (evt.getOldValue().equals(getMemory().getBean())) {
                    setMemory((Memory)null);
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
    public void execute() {
        final Memory memory = _memoryHandle.getBean();
        
        ThreadingUtil.runOnLayout(() -> {
            switch (_memoryOperation) {
                case SET_TO_STRING:
                    memory.setValue(_newStringValue);
                    break;
                    
                case SET_TO_NULL:
                    memory.setValue(null);
                    break;
                    
                case COPY_MEMORY:
                    memory.setValue(_copyFromMemoryHandle.getBean().getValue());
                    break;
                    
                default:
                    log.error("_memoryOperation has invalid value: {}", _memoryOperation.name());
            }
        });
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
        return Bundle.getMessage(locale, "Memory_Long", memoryName, _memoryOperation._text);
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
        SET_TO_STRING(Bundle.getMessage("MemoryOperation_SetToString")),
        SET_TO_NULL(Bundle.getMessage("MemoryOperation_SetToNull")),
        COPY_MEMORY(Bundle.getMessage("MemoryOperation_CopyMemory"));
        
        private final String _text;
        
        private MemoryOperation(String text) {
            this._text = text;
        }
        
        @Override
        public String toString() {
            return _text;
        }
        
    }
    
    private final static Logger log = LoggerFactory.getLogger(ActionMemory.class);
    
}

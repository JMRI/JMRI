package jmri.jmrit.logixng.digital.actions;

import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.annotation.CheckForNull;
import jmri.InstanceManager;
import jmri.Light;
import jmri.LightManager;
import jmri.Manager;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.NamedBean;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.FemaleSocket;
import jmri.jmrit.logixng.util.DuplicateKeyMap;
import jmri.util.ThreadingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This action listens on some beans and runs the ConditionalNG on property change.
 * 
 * @author Daniel Bergqvist Copyright 2019
 */
public class ActionListenOnBeans extends AbstractDigitalAction implements VetoableChangeListener {

    private final Map<String, NamedBeanReference> namedBeanReferences = new DuplicateKeyMap<>();
    private boolean _listenersAreRegistered = false;
    
    public ActionListenOnBeans(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
//        jmri.NamedBeanMap a;
    }
    
    public void addReference(NamedBeanReference reference) {
        int a = 0;
    }
/*    
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
    
    public void setMemoryOperation(MemoryOperation state) {
        _memoryOperation = state;
    }
    
    public MemoryOperation getMemoryOperation() {
        return _memoryOperation;
    }
    
    public void setNewValue(String newValue) {
        _newValue = newValue;
    }
    
    public String getNewValue() {
        return _newValue;
    }
*/    
    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
/*        
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
    public void execute() {
        // Do nothing.
        // The purpose of this action is only to listen on property changes
        // of the registered beans and execute the ConditionalNG when it
        // happens.
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
        return Bundle.getMessage(locale, "ListenOnBeans_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        return Bundle.getMessage(locale, "ListenOnBeans_Long");
    }
    
    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
    }
    
    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
//        if (!_listenersAreRegistered && (_lightHandle != null)) {
//            _lightHandle.getBean().addPropertyChangeListener("KnownState", this);
//            _listenersAreRegistered = true;
//        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        if (_listenersAreRegistered) {
//            _lightHandle.getBean().removePropertyChangeListener("KnownState", this);
            _listenersAreRegistered = false;
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }
    
    
    public enum NamedBeanType {
//        CONSTANT(Bundle.getMessage("BeanNameConstant"), InstanceManager.getDefault(ConstantManager.class)),
        LIGHT(Bundle.getMessage("BeanNameLight"), Light.class, InstanceManager.getDefault(LightManager.class)),
        MEMORY(Bundle.getMessage("BeanNameMemory"), Memory.class, InstanceManager.getDefault(MemoryManager.class)),
        SENSOR(Bundle.getMessage("BeanNameSensor"), Sensor.class, InstanceManager.getDefault(SensorManager.class)),
        TURNOUT(Bundle.getMessage("BeanNameTurnout"), Turnout.class, InstanceManager.getDefault(TurnoutManager.class));
        
        private final String _name;
        private final Class<? extends NamedBean> _clazz;
        private final Manager<? extends NamedBean> _manager;
        
        NamedBeanType(String name, Class<? extends NamedBean> clazz, Manager<? extends NamedBean> manager) {
            _name = name;
            _clazz = clazz;
            _manager = manager;
        }
    }
    
    
    public class NamedBeanReference {
        
        private String _name;
        private NamedBeanType _type;
        private NamedBeanHandle<? extends NamedBean> _handle;
        
        public NamedBeanReference(String name, NamedBeanType type) {
            _name = name;
            _type = type;
            
            NamedBean bean = _type._manager.getNamedBean(name);
            if (bean != null) {
                _handle = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(_name, bean);
            }
        }
        
        public String getName() {
            return _name;
        }
        
        public NamedBeanType getType() {
            return _type;
        }
        
        public NamedBeanHandle<? extends NamedBean> getHandle() {
            return _handle;
        }
    }
    
    
    private final static Logger log = LoggerFactory.getLogger(ActionListenOnBeans.class);
    
}

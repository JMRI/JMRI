package jmri.jmrit.logixng.digital.actions;

import java.beans.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

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
public class ActionListenOnBeans extends AbstractDigitalAction
        implements PropertyChangeListener, VetoableChangeListener {

    private final Map<String, NamedBeanReference> _namedBeanReferences = new DuplicateKeyMap<>();
//    private boolean _listenersAreRegistered = false;
    
    public ActionListenOnBeans(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
//        jmri.NamedBeanMap a;
    }
    
    /**
     * Register a bean
     * The bean must be on the form "beantype:name" where beantype is for
     * example turnout, sensor or memory, and name is the name of the bean.
     * The type can be upper case or lower case, it doesn't matter.
     * @param beanAndType the bean and type
     */
    public void addReference(String beanAndType) {
        assertListenersAreNotRegistered(log, "addReference");
        String[] parts = beanAndType.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException(
                    "Parameter 'beanAndType' must be on the format type:name"
                    + " where type is turnout, sensor, memory, ...");
        }
        
        try {
            NamedBeanType type = NamedBeanType.valueOf(parts[0].toUpperCase());
            NamedBeanReference reference = new NamedBeanReference(parts[1], type);
            _namedBeanReferences.put(reference._name, reference);
        } catch (IllegalArgumentException e) {
            String types = Arrays.asList(NamedBeanType.values())
                    .stream()
                    .map(Enum::toString)
                    .collect(Collectors.joining(", "));
            throw new IllegalArgumentException(
                    "Parameter 'beanAndType' has wrong type. Valid types are: " + types);
        }
    }
    
    public void addReference(NamedBeanReference reference) {
        assertListenersAreNotRegistered(log, "addReference");
        _namedBeanReferences.put(reference._name, reference);
    }
    
    public void removeReference(NamedBeanReference reference) {
        assertListenersAreNotRegistered(log, "removeReference");
        _namedBeanReferences.remove(reference._name);
    }
    
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
        return Bundle.getMessage(locale, "ActionListenOnBeans_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        return Bundle.getMessage(locale, "ActionListenOnBeans_Long");
    }
    
    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
    }
    
    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
        if (_listenersAreRegistered) return;
        
        for (NamedBeanReference namedBeanReference : _namedBeanReferences.values()) {
            if (namedBeanReference._handle != null) {
                namedBeanReference._handle.getBean()
                        .addPropertyChangeListener(namedBeanReference._type._propertyName, this);
            }
        }
        _listenersAreRegistered = true;
    }
    
    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        if (!_listenersAreRegistered) return;
        
        for (NamedBeanReference namedBeanReference : _namedBeanReferences.values()) {
            if (namedBeanReference._handle != null) {
                namedBeanReference._handle.getBean()
                        .removePropertyChangeListener(namedBeanReference._type._propertyName, this);
            }
        }
        _listenersAreRegistered = false;
    }
    
    /** {@inheritDoc} */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        System.out.format("propertyChange%n");
        getConditionalNG().execute();
    }
    
    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }
    
    
    public enum NamedBeanType {
        LIGHT(Bundle.getMessage("BeanNameLight"), Light.class, "KnownState", InstanceManager.getDefault(LightManager.class)),
        MEMORY(Bundle.getMessage("BeanNameMemory"), Memory.class, "value", InstanceManager.getDefault(MemoryManager.class)),
        SENSOR(Bundle.getMessage("BeanNameSensor"), Sensor.class, "KnownState", InstanceManager.getDefault(SensorManager.class)),
        TURNOUT(Bundle.getMessage("BeanNameTurnout"), Turnout.class, "KnownState", InstanceManager.getDefault(TurnoutManager.class));
        
        private final String _name;
        private final Class<? extends NamedBean> _clazz;
        private final String _propertyName;
        private final Manager<? extends NamedBean> _manager;
        
        NamedBeanType(String name, Class<? extends NamedBean> clazz, String propertyName, Manager<? extends NamedBean> manager) {
            _name = name;
            _clazz = clazz;
            _propertyName = propertyName;
            _manager = manager;
        }
        
        public String getName() { return _name; }
        
        public Class<? extends NamedBean> getClazz() { return _clazz; }
        
        public Manager<? extends NamedBean> getManager() { return _manager; }
    }
    
    
    public static class NamedBeanReference {
        
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

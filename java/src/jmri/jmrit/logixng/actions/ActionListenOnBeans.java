package jmri.jmrit.logixng.actions;

import java.beans.*;
import java.util.*;
import java.util.stream.Collectors;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.DuplicateKeyMap;

/**
 * This action listens on some beans and runs the ConditionalNG on property change.
 *
 * @author Daniel Bergqvist Copyright 2019
 */
public class ActionListenOnBeans extends AbstractDigitalAction
        implements PropertyChangeListener, VetoableChangeListener {

    private final Map<String, NamedBeanReference> _namedBeanReferences = new DuplicateKeyMap<>();

    public ActionListenOnBeans(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ActionListenOnBeans copy = new ActionListenOnBeans(sysName, userName);
        copy.setComment(getComment());
        for (NamedBeanReference reference : _namedBeanReferences.values()) {
            copy.addReference(reference);
        }
        return manager.registerAction(copy);
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
            NamedBeanType type = NamedBeanType.valueOf(parts[0]);
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

    public Collection<NamedBeanReference> getReferences() {
        return _namedBeanReferences.values();
    }

    public void clearReferences() {
        _namedBeanReferences.clear();
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
        getConditionalNG().execute();
    }

    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }


    public enum NamedBeanType {
        Light(Bundle.getMessage("BeanNameLight"), Light.class, "KnownState", () -> { return InstanceManager.getDefault(LightManager.class); }),
        Memory(Bundle.getMessage("BeanNameMemory"), Memory.class, "value", () -> { return InstanceManager.getDefault(MemoryManager.class); }),
        Sensor(Bundle.getMessage("BeanNameSensor"), Sensor.class, "KnownState", () -> { return InstanceManager.getDefault(SensorManager.class); }),
        Turnout(Bundle.getMessage("BeanNameTurnout"), Turnout.class, "KnownState", () -> { return InstanceManager.getDefault(TurnoutManager.class); });

        private final String _name;
        private final Class<? extends NamedBean> _clazz;
        private final String _propertyName;
        private final GetManager _getManager;
        private Manager<? extends NamedBean> _manager;

        NamedBeanType(String name, Class<? extends NamedBean> clazz, String propertyName, GetManager getManager) {
            _name = name;
            _clazz = clazz;
            _propertyName = propertyName;
            _getManager = getManager;
            _manager = _getManager.getManager();
        }

        @Override
        public String toString() { return _name; }

        public Class<? extends NamedBean> getClazz() { return _clazz; }

        public Manager<? extends NamedBean> getManager() { return _manager; }

        // This method is used by test classes to reset this enum.
        // Each test resets the InstanceManager so we need to reset the
        // managers in this enum.
        public static void reset() {
            for (NamedBeanType type : NamedBeanType.values()) {
                type._manager = type._getManager.getManager();
            }
        }

        private interface GetManager {
            Manager<? extends NamedBean> getManager();
        }
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

        public void setName(String name) {
            _name = name;
        }

        public NamedBeanType getType() {
            return _type;
        }

        public void setType(NamedBeanType type) {
            if (type == null) throw new NullPointerException("type is null");
            _type = type;
        }

        public NamedBeanHandle<? extends NamedBean> getHandle() {
            return _handle;
        }

        public void updateHandle() {
            if (!_name.isEmpty()) {
                NamedBean bean = _type._manager.getNamedBean(_name);
                if (bean != null) {
                    _handle = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(_name, bean);
                } else {
                    log.warn("Cannot find named bean "+_name+" in manager for "+_type._manager.getBeanTypeHandled());
                    _handle = null;
                }
            } else {
                _handle = null;
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void getUsageDetail(int level, NamedBean bean, List<NamedBeanUsageReport> report, NamedBean cdl) {
        log.debug("getUsageReport :: ActionListenOnBeans: bean = {}, report = {}", cdl, report);
        for (NamedBeanReference namedBeanReference : _namedBeanReferences.values()) {
            if (namedBeanReference._handle != null) {
                if (bean.equals(namedBeanReference._handle.getBean())) {
                    report.add(new NamedBeanUsageReport("LogixNGAction", cdl, getLongDescription()));
                }
            }
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionListenOnBeans.class);

}

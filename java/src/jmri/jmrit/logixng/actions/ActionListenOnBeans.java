package jmri.jmrit.logixng.actions;

import java.beans.*;
import java.util.*;
import java.util.stream.Collectors;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.DuplicateKeyMap;

import net.jcip.annotations.GuardedBy;

/**
 * This action listens on some beans and runs the ConditionalNG on property change.
 *
 * @author Daniel Bergqvist Copyright 2019
 */
public class ActionListenOnBeans extends AbstractDigitalAction
        implements PropertyChangeListener, VetoableChangeListener {

    private final Map<String, NamedBeanReference> _namedBeanReferences = new DuplicateKeyMap<>();
    private String _localVariableNamedBean;
    private String _localVariableEvent;
    private String _localVariableNewValue;

    @GuardedBy("this")
    private final Deque<PropertyChangeEvent> _eventQueue = new ArrayDeque<>();


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
        copy.setLocalVariableNamedBean(_localVariableNamedBean);
        copy.setLocalVariableEvent(_localVariableEvent);
        copy.setLocalVariableNewValue(_localVariableNewValue);
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
        if ((parts.length < 2) || (parts.length > 3)) {
            throw new IllegalArgumentException(
                    "Parameter 'beanAndType' must be on the format type:name"
                    + " where type is turnout, sensor, memory, ..., or on the"
                    + " format type:name:all where all is yes or no");
        }

        boolean listenToAll = false;
        if (parts.length == 3) listenToAll = "yes".equals(parts[2]); // NOI18N

        try {
            NamedBeanType type = NamedBeanType.valueOf(parts[0]);
            NamedBeanReference reference = new NamedBeanReference(parts[1], type, listenToAll);
            addReference(reference);
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
        reference._type.getManager().addVetoableChangeListener(this);
    }

    public void removeReference(NamedBeanReference reference) {
        assertListenersAreNotRegistered(log, "removeReference");
        _namedBeanReferences.remove(reference._name, reference);
        reference._type.getManager().removeVetoableChangeListener(this);
    }

    public Collection<NamedBeanReference> getReferences() {
        return _namedBeanReferences.values();
    }

    public void clearReferences() {
        _namedBeanReferences.clear();
    }

    public void setLocalVariableNamedBean(String localVariableNamedBean) {
        if ((localVariableNamedBean != null) && (!localVariableNamedBean.isEmpty())) {
            this._localVariableNamedBean = localVariableNamedBean;
        } else {
            this._localVariableNamedBean = null;
        }
    }

    public String getLocalVariableNamedBean() {
        return _localVariableNamedBean;
    }

    public void setLocalVariableEvent(String localVariableEvent) {
        if ((localVariableEvent != null) && (!localVariableEvent.isEmpty())) {
            this._localVariableEvent = localVariableEvent;
        } else {
            this._localVariableEvent = null;
        }
    }

    public String getLocalVariableEvent() {
        return _localVariableEvent;
    }

    public void setLocalVariableNewValue(String localVariableNewValue) {
        if ((localVariableNewValue != null) && (!localVariableNewValue.isEmpty())) {
            this._localVariableNewValue = localVariableNewValue;
        } else {
            this._localVariableNewValue = null;
        }
    }

    public String getLocalVariableNewValue() {
        return _localVariableNewValue;
    }

    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        var tempNamedBeanReferences = new ArrayList<NamedBeanReference>(_namedBeanReferences.values());
        for (NamedBeanReference reference : tempNamedBeanReferences) {
            if (reference._type.getClazz().isAssignableFrom(evt.getOldValue().getClass())) {
                if ((reference._handle != null) && evt.getOldValue().equals(reference._handle.getBean())) {
                    if ("CanDelete".equals(evt.getPropertyName())) { // No I18N
                        PropertyChangeEvent e = new PropertyChangeEvent(this, "DoNotDelete", null, null);
                        throw new PropertyVetoException(getDisplayName(), e);
                    } else if ("DoDelete".equals(evt.getPropertyName())) { // No I18N
                        _namedBeanReferences.remove(reference._name, reference);
                    }
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
    public void execute() {
        // The main purpose of this action is only to listen on property
        // changes of the registered beans and execute the ConditionalNG
        // when it happens.

        synchronized(this) {
            String namedBean;
            String event;
            String newValue;

            PropertyChangeEvent evt = _eventQueue.poll();
            if (evt != null) {
                namedBean = ((NamedBean)evt.getSource()).getDisplayName();
                event = evt.getPropertyName();
                newValue = evt.getNewValue() != null ? evt.getNewValue().toString() : null;
            } else {
                namedBean = null;
                event = null;
                newValue = null;
            }

            SymbolTable symbolTable = getConditionalNG().getSymbolTable();

            if (_localVariableNamedBean != null) {
                symbolTable.setValue(_localVariableNamedBean, namedBean);
            }
            if (_localVariableEvent != null) {
                symbolTable.setValue(_localVariableEvent, event);
            }
            if (_localVariableNewValue != null) {
                symbolTable.setValue(_localVariableNewValue, newValue);
            }

            if (!_eventQueue.isEmpty()) {
                getConditionalNG().execute();
            }
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
                if (!namedBeanReference._listenOnAllProperties
                        && (namedBeanReference._type.getPropertyName() != null)) {
                    namedBeanReference._handle.getBean()
                            .addPropertyChangeListener(namedBeanReference._type.getPropertyName(), this);
                } else {
                    namedBeanReference._handle.getBean()
                            .addPropertyChangeListener(this);
                }
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
                if (!namedBeanReference._listenOnAllProperties
                        && (namedBeanReference._type.getPropertyName() != null)) {
                    namedBeanReference._handle.getBean()
                            .removePropertyChangeListener(namedBeanReference._type.getPropertyName(), this);
                } else {
                    namedBeanReference._handle.getBean()
                            .removePropertyChangeListener(this);
                }
            }
        }
        _listenersAreRegistered = false;
    }

    /** {@inheritDoc} */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        boolean isQueueEmpty;
        synchronized(this) {
            isQueueEmpty = _eventQueue.isEmpty();
            _eventQueue.add(evt);
        }
        if (isQueueEmpty) {
            getConditionalNG().execute();
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

    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }


    public static class NamedBeanReference {

        private String _name;
        private NamedBeanType _type;
        private NamedBeanHandle<? extends NamedBean> _handle;
        private boolean _listenOnAllProperties = false;

        public NamedBeanReference(NamedBeanReference ref) {
            this(ref._handle, ref._type, ref._listenOnAllProperties);
        }

        public NamedBeanReference(String name, NamedBeanType type, boolean all) {
            _name = name;
            _type = type;
            _listenOnAllProperties = all;

            if (_type != null) {
                NamedBean bean = _type.getManager().getNamedBean(name);
                if (bean != null) {
                    _handle = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(_name, bean);
                }
            }
        }

        public NamedBeanReference(NamedBeanHandle<? extends NamedBean> handle, NamedBeanType type, boolean all) {
            _name = handle != null ? handle.getName() : null;
            _type = type;
            _listenOnAllProperties = all;
            _handle = handle;
        }

        public String getName() {
            return _name;
        }

        public void setName(String name) {
            _name = name;
            updateHandle();
        }

        public void setName(NamedBean bean) {
            if (bean != null) {
                _handle = InstanceManager.getDefault(NamedBeanHandleManager.class)
                        .getNamedBeanHandle(bean.getDisplayName(), bean);
                _name = _handle.getName();
            } else {
                _name = null;
                _handle = null;
            }
        }

        public void setName(NamedBeanHandle<? extends NamedBean> handle) {
            if (handle != null) {
                _handle = handle;
                _name = _handle.getName();
            } else {
                _name = null;
                _handle = null;
            }
        }

        public NamedBeanType getType() {
            return _type;
        }

        public void setType(NamedBeanType type) {
            if (type == null) {
                log.warn("type is null");
                type = NamedBeanType.Turnout;
            }
            _type = type;
            _handle = null;
        }

        public NamedBeanHandle<? extends NamedBean> getHandle() {
            return _handle;
        }

        private void updateHandle() {
            if (_type != null && _name != null && !_name.isEmpty()) {
                NamedBean bean = _type.getManager().getNamedBean(_name);
                if (bean != null) {
                    _handle = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(_name, bean);
                } else {
                    log.warn("Cannot find named bean {} in manager for {}", _name, _type.getManager().getBeanTypeHandled());
                    _handle = null;
                }
            } else {
                _handle = null;
            }
        }

        public boolean getListenOnAllProperties() {
            return _listenOnAllProperties;
        }

        public void setListenOnAllProperties(boolean listenOnAllProperties) {
            _listenOnAllProperties = listenOnAllProperties;
        }

        // This method is used by ListenOnBeansTableModel
        @Override
        public String toString() {
            if (_handle != null) return _handle.getName();
            else return "";
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionListenOnBeans.class);

}

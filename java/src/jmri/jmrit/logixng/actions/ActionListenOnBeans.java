package jmri.jmrit.logixng.actions;

import jmri.jmrit.logixng.NamedBeanType;
import jmri.jmrit.logixng.NamedBeanReference;

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
        implements ReplaceNamedBean, PropertyChangeListener, VetoableChangeListener {

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
        _namedBeanReferences.put(reference.getName(), reference);
        reference.getType().getManager().addVetoableChangeListener(this);
    }

    public void removeReference(NamedBeanReference reference) {
        assertListenersAreNotRegistered(log, "removeReference");
        _namedBeanReferences.remove(reference.getName(), reference);
        reference.getType().getManager().removeVetoableChangeListener(this);
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
            if (reference.getType().getClazz().isAssignableFrom(evt.getOldValue().getClass())) {
                if ((reference.getHandle() != null) && evt.getOldValue().equals(reference.getHandle().getBean())) {
                    if ("CanDelete".equals(evt.getPropertyName())) { // No I18N
                        PropertyChangeEvent e = new PropertyChangeEvent(this, "DoNotDelete", null, null);
                        throw new PropertyVetoException(getDisplayName(), e);
                    } else if ("DoDelete".equals(evt.getPropertyName())) { // No I18N
                        _namedBeanReferences.remove(reference.getName(), reference);
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
            if (namedBeanReference.getHandle() != null) {
                if (!namedBeanReference.getListenOnAllProperties()
                        && (namedBeanReference.getType().getPropertyName() != null)) {
                    namedBeanReference.getHandle().getBean()
                            .addPropertyChangeListener(namedBeanReference.getType().getPropertyName(), this);
                } else {
                    namedBeanReference.getHandle().getBean()
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
            if (namedBeanReference.getHandle() != null) {
                if (!namedBeanReference.getListenOnAllProperties()
                        && (namedBeanReference.getType().getPropertyName() != null)) {
                    namedBeanReference.getHandle().getBean()
                            .removePropertyChangeListener(namedBeanReference.getType().getPropertyName(), this);
                } else {
                    namedBeanReference.getHandle().getBean()
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
            if (namedBeanReference.getHandle() != null) {
                if (bean.equals(namedBeanReference.getHandle().getBean())) {
                    report.add(new NamedBeanUsageReport("LogixNGAction", cdl, getLongDescription()));
                }
            }
        }
    }

    @Override
    public void getNamedBeans(List<NamedBeanReference> list) {
        for (NamedBeanReference ref : _namedBeanReferences.values()) {
            list.add(new NamedBeanReference(ref));
        }
    }

    @Override
    public void replaceNamedBean(NamedBeanReference oldRef, NamedBeanReference newRef) {
        boolean found = false;
        for (NamedBeanReference ref : _namedBeanReferences.values()) {
            if (ref.equals(oldRef)) {
                ref.setName(newRef.getHandle());
                found = true;
            }
        }
        if (!found) throw new IllegalArgumentException("Can't find old reference");     // Probably a bug in JMRI if this happens.
    }

    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionListenOnBeans.class);

}

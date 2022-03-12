package jmri.managers;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.*;

import javax.annotation.CheckReturnValue;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;

import jmri.*;
import jmri.beans.VetoableChangeSupport;
import jmri.SystemConnectionMemo;
import jmri.jmrix.ConnectionConfig;
import jmri.jmrix.ConnectionConfigManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.NamedBeanComparator;

/**
 * Implementation of a Manager that can serves as a proxy for multiple
 * system-specific implementations.
 * <p>
 * Automatically includes an Internal system, which need not be separately added
 * any more.
 * <p>
 * Encapsulates access to the "Primary" manager, used by default, which is the
 * first one provided.
 * <p>
 * Internally, this is done by using an ordered list of all non-Internal
 * managers, plus a separate reference to the internal manager and default
 * manager.
 *
 * @param <E> the supported type of NamedBean
 * @author Bob Jacobsen Copyright (C) 2003, 2010, 2018
 */
abstract public class AbstractProxyManager<E extends NamedBean> extends VetoableChangeSupport implements ProxyManager<E>, PropertyChangeListener, Manager.ManagerDataListener<E> {

    /**
     * List of names of bound properties requested to be listened to by
     * PropertyChangeListeners.
     */
    private final List<String> boundPropertyNames = new ArrayList<>();
    /**
     * List of names of bound properties requested to be listened to by
     * VetoableChangeListeners.
     */
    private final List<String> vetoablePropertyNames = new ArrayList<>();
    protected final Map<String, Boolean> silencedProperties = new HashMap<>();
    protected final Set<String> silenceableProperties = new HashSet<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Manager<E>> getManagerList() {
        // make sure internal present
        initInternal();
        return new ArrayList<>(mgrs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Manager<E>> getDisplayOrderManagerList() {
        // make sure internal present
        initInternal();

        ArrayList<Manager<E>> retval = new ArrayList<>();
        if (defaultManager != null) {
            retval.add(defaultManager);
        }
        mgrs.stream()
                .filter(manager -> manager != defaultManager && manager != internalManager)
                .forEachOrdered(retval::add);
        if (internalManager != null && internalManager != defaultManager) {
            retval.add(internalManager);
        }
        return retval;
    }

    public Manager<E> getInternalManager() {
        initInternal();
        return internalManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public Manager<E> getDefaultManager() {
        return defaultManager != null ? defaultManager : getInternalManager();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addManager(@Nonnull Manager<E> m) {
        Objects.requireNonNull(m, "Can only add non-null manager");
        // check for already present
        for (Manager<E> check : mgrs) {
            if (m == check) { // can't use contains(..) because of Comparator.equals is on the prefix
                // already present, complain and skip
                log.warn("Manager already present: {}", m); // NOI18N
                return;
            }
        }
        mgrs.add(m);

        if (defaultManager == null) defaultManager = m;  // 1st one is default

        Arrays.stream(getPropertyChangeListeners()).forEach(l -> m.addPropertyChangeListener(l));
        Arrays.stream(getVetoableChangeListeners()).forEach(l -> m.addVetoableChangeListener(l));
        boundPropertyNames
                .forEach(n -> Arrays.stream(getPropertyChangeListeners(n))
                .forEach(l -> m.addPropertyChangeListener(n, l)));
        vetoablePropertyNames
                .forEach(n -> Arrays.stream(getVetoableChangeListeners(n))
                .forEach(l -> m.addVetoableChangeListener(n, l)));
        m.addPropertyChangeListener("beans", this);
        m.addDataListener(this);
        recomputeNamedBeanSet();
        log.debug("added manager {}", m.getClass());
    }

    protected Manager<E> initInternal() {
        if (internalManager == null) {
            log.debug("create internal manager when first requested"); // NOI18N
            internalManager = makeInternalManager();
        }
        return internalManager;
    }

    private final Set<Manager<E>> mgrs = new TreeSet<>((Manager<E> e1, Manager<E> e2) -> e1.getSystemPrefix().compareTo(e2.getSystemPrefix()));
    private Manager<E> internalManager = null;
    protected Manager<E> defaultManager = null;

    /**
     * Create specific internal manager as needed for concrete type.
     *
     * @return an internal manager
     */
    abstract protected Manager<E> makeInternalManager();

    /** {@inheritDoc} */
    @Override
    public E getNamedBean(@Nonnull String name) {
        E t = getByUserName(name);
        if (t != null) {
            return t;
        }
        return getBySystemName(name);
    }

    /** {@inheritDoc} */
    @Override
    @CheckReturnValue
    @CheckForNull
    public E getBySystemName(@Nonnull String systemName) {
        Manager<E> m = getManager(systemName);
        if (m == null) {
            log.debug("getBySystemName did not find manager from name {}, defer to default manager", systemName);
            m = getDefaultManager();
        }
        return m.getBySystemName(systemName);
    }

    /** {@inheritDoc} */
    @Override
    @CheckReturnValue
    @CheckForNull
    public E getByUserName(@Nonnull String userName) {
        for (Manager<E> m : this.mgrs) {
            E b = m.getByUserName(userName);
            if (b != null) {
                return b;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation locates a specific Manager based on the system name
     * and validates against that. If no matching Manager exists, the default
     * Manager attempts to validate the system name.
     */
    @Override
    @Nonnull
    public String validateSystemNameFormat(@Nonnull String systemName, @Nonnull Locale locale) {
        Manager<E> manager = getManager(systemName);
        if (manager == null) {
            manager = getDefaultManager();
        }
        return manager.validateSystemNameFormat(systemName, locale);
    }

    /**
     * Validate system name format. Locate a system specific Manager based on a
     * system name.
     *
     * @return if a manager is found, return its determination of validity of
     *         system name format. Return INVALID if no manager exists.
     */
    @Override
    public NameValidity validSystemNameFormat(@Nonnull String systemName) {
        Manager<E> m = getManager(systemName);
        return m == null ? NameValidity.INVALID : m.validSystemNameFormat(systemName);
    }

    /** {@inheritDoc} */
    @Override
    public void dispose() {
        mgrs.forEach(m -> m.dispose());
        mgrs.clear();
        if (internalManager != null) {
            internalManager.dispose(); // don't make if not made yet
        }
    }

    /**
     * Get the manager for the given system name.
     *
     * @param systemName the given name
     * @return the requested manager or null if there is no matching manager
     */
    @CheckForNull
    protected Manager<E> getManager(@Nonnull String systemName) {
        // make sure internal present
        initInternal();
        for (Manager<E> m : getManagerList()) {
            if (systemName.startsWith(m.getSystemNamePrefix())) {
                return m;
            }
        }
        return null;
    }

    /**
     * Get the manager for the given system name or the default manager if there
     * is no matching manager.
     *
     * @param systemName the given name
     * @return the requested manager or the default manager if there is no
     *         matching manager
     */
    @Nonnull
    protected Manager<E> getManagerOrDefault(@Nonnull String systemName) {
        Manager<E> manager = getManager(systemName);
        if (manager == null) {
            manager = getDefaultManager();
        }
        return manager;
    }

    /**
     * Shared method to create a systemName based on the address base, the prefix and manager class.
     *
     * @param curAddress base address to use
     * @param prefix system prefix to use
     * @param beanType Bean Type for manager (method is used for Turnout and Sensor Managers)
     * @return a valid system name for this connection
     * @throws JmriException if systemName cannot be created
     */
    String createSystemName(String curAddress, String prefix, Class<?> beanType) throws JmriException {
        for (Manager<E> m : mgrs) {
            if (prefix.equals(m.getSystemPrefix()) && beanType.equals(m.getNamedBeanClass())) {
                try {
                    if (beanType == Turnout.class) {
                        return ((TurnoutManager) m).createSystemName(curAddress, prefix);
                    } else if (beanType == Sensor.class) {
                        return ((SensorManager) m).createSystemName(curAddress, prefix);
                    }
                    else if (beanType == Light.class) {
                        return ((LightManager) m).createSystemName(curAddress, prefix);
                    }
                    else if (beanType == Reporter.class) {
                        return ((ReporterManager) m).createSystemName(curAddress, prefix);
                    }
                    else {
                        log.warn("createSystemName requested for incompatible Manager");
                    }
                } catch (jmri.JmriException ex) {
                    throw ex;
                }
            }
        }
        throw new jmri.JmriException("Manager could not be found for System Prefix " + prefix);
    }

    @Nonnull
    public String createSystemName(@Nonnull String curAddress, @Nonnull String prefix) throws jmri.JmriException {
        return createSystemName(curAddress, prefix, getNamedBeanClass());
    }

    public String getNextValidAddress(@Nonnull String curAddress, @Nonnull String prefix, char typeLetter) throws jmri.JmriException {
        for (Manager<E> m : mgrs) {
            log.debug("NextValidAddress requested for {}", curAddress);
            if (prefix.equals(m.getSystemPrefix()) && typeLetter == m.typeLetter()) {
                try {
                    switch (typeLetter) { // use #getDefaultManager() instead?
                        case 'T':
                            return ((TurnoutManager) m).getNextValidAddress(curAddress, prefix, false);
                        case 'S':
                            return ((SensorManager) m).getNextValidAddress(curAddress, prefix, false);
                        case 'R':
                            return ((ReporterManager) m).getNextValidAddress(curAddress, prefix, false);
                        default:
                            return null;
                    }
                } catch (jmri.JmriException ex) {
                    throw ex;
                }
            }
        }
        return null;
    }

    public String getNextValidAddress(@Nonnull String curAddress, @Nonnull String prefix, boolean ignoreInitialExisting, char typeLetter) throws jmri.JmriException {
        for (Manager<E> m : mgrs) {
            log.debug("NextValidAddress requested for {}", curAddress);
            if (prefix.equals(m.getSystemPrefix()) && typeLetter == m.typeLetter()) {
                try {
                    switch (typeLetter) { // use #getDefaultManager() instead?
                        case 'T':
                            return ((TurnoutManager) m).getNextValidAddress(curAddress, prefix, ignoreInitialExisting);
                        case 'S':
                            return ((SensorManager) m).getNextValidAddress(curAddress, prefix, ignoreInitialExisting);
                        case 'L':
                            return ((LightManager) m).getNextValidAddress(curAddress, prefix, ignoreInitialExisting);
                        case 'R':
                            return ((ReporterManager) m).getNextValidAddress(curAddress, prefix, ignoreInitialExisting);
                        default:
                            return null;
                    }
                } catch (jmri.JmriException ex) {
                    throw ex;
                }
            }
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void deleteBean(@Nonnull E s, @Nonnull String property) throws PropertyVetoException {
        Manager<E> m = getManager(s.getSystemName());
        if (m != null) {
            m.deleteBean(s, property);
        }
    }

    /**
     * Try to create a system manager. If this proxy manager is able to create
     * a system manager, the concrete class must implement this method.
     *
     * @param memo the system connection memo for this connection
     * @return the new manager or null if it's not possible to create the manager
     */
    protected Manager<E> createSystemManager(@Nonnull SystemConnectionMemo memo) {
        return null;
    }

    /**
     * Get the Default Manager ToolTip.
     * {@inheritDoc}
     */
    @Override
    public String getEntryToolTip() {
        return getDefaultManager().getEntryToolTip();
    }

    /**
     * Try to create a system manager.
     *
     * @param systemPrefix the system prefix
     * @return the new manager or null if it's not possible to create the manager
     */
    private Manager<E> createSystemManager(@Nonnull String systemPrefix) {
        Manager<E> m = null;

        ConnectionConfigManager manager = InstanceManager.getNullableDefault(ConnectionConfigManager.class);
        if (manager == null) return null;

        ConnectionConfig connections[] = manager.getConnections();

        for (ConnectionConfig connection : connections) {
            if (systemPrefix.equals(connection.getAdapter().getSystemPrefix())) {
                m = createSystemManager(connection.getAdapter().getSystemConnectionMemo());
            }
            if (m != null) break;
        }
//        if (m == null) throw new RuntimeException("Manager not created");
        return m;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Forwards the register request to the matching system.
     */
    @Override
    public void register(@Nonnull E s) {
        Manager<E> m = getManager(s.getSystemName());
        if (m == null) {
            String systemPrefix = Manager.getSystemPrefix(s.getSystemName());
            m = createSystemManager(systemPrefix);
        }
        if (m != null) {
            m.register(s);
        } else {
            log.error("Unable to register {} in this proxy manager. No system specific manager supports this bean.", s.getSystemName());
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Forwards the deregister request to the matching system.
     *
     * @param s the name
     */
    @Override
    public void deregister(@Nonnull E s) {
        Manager<E> m = getManager(s.getSystemName());
        if (m != null) {
            m.deregister(s);
        }
    }

    /**
     * {@inheritDoc}
     * List does not contain duplicates.
     */
    @Nonnull
    @Override
    public List<NamedBeanPropertyDescriptor<?>> getKnownBeanProperties() {
        // Create List as set to prevent duplicates from multiple managers
        // of the same hardware type.
        Set<NamedBeanPropertyDescriptor<?>> set = new HashSet<>();
        mgrs.forEach(m -> set.addAll(m.getKnownBeanProperties()));
        return new ArrayList<>(set);
    }

    /** {@inheritDoc} */
    @Override
    @OverridingMethodsMustInvokeSuper
    public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
        super.addPropertyChangeListener(l);
        mgrs.forEach(m -> m.addPropertyChangeListener(l));
    }

    /** {@inheritDoc} */
    @Override
    @OverridingMethodsMustInvokeSuper
    public synchronized void removePropertyChangeListener(PropertyChangeListener l) {
        super.removePropertyChangeListener(l);
        mgrs.forEach(m -> m.removePropertyChangeListener(l));
    }

    /** {@inheritDoc} */
    @Override
    @OverridingMethodsMustInvokeSuper
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        super.addPropertyChangeListener(propertyName, listener);
        boundPropertyNames.add(propertyName);
        mgrs.forEach(m -> m.addPropertyChangeListener(propertyName, listener));
    }

    /** {@inheritDoc} */
    @Override
    @OverridingMethodsMustInvokeSuper
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        super.removePropertyChangeListener(propertyName, listener);
        mgrs.forEach(m -> m.removePropertyChangeListener(propertyName, listener));
    }

    /** {@inheritDoc} */
    @Override
    @OverridingMethodsMustInvokeSuper
    public synchronized void addVetoableChangeListener(VetoableChangeListener l) {
        super.addVetoableChangeListener(l);
        mgrs.forEach(m -> m.addVetoableChangeListener(l));
    }

    /** {@inheritDoc} */
    @Override
    @OverridingMethodsMustInvokeSuper
    public synchronized void removeVetoableChangeListener(VetoableChangeListener l) {
        super.removeVetoableChangeListener(l);
        mgrs.forEach(m -> m.removeVetoableChangeListener(l));
    }

    /** {@inheritDoc} */
    @Override
    @OverridingMethodsMustInvokeSuper
    public void addVetoableChangeListener(String propertyName, VetoableChangeListener listener) {
        super.addVetoableChangeListener(propertyName, listener);
        vetoablePropertyNames.add(propertyName);
        mgrs.forEach(m -> m.addVetoableChangeListener(propertyName, listener));
    }

    /** {@inheritDoc} */
    @Override
    @OverridingMethodsMustInvokeSuper
    public void removeVetoableChangeListener(String propertyName, VetoableChangeListener listener) {
        super.removeVetoableChangeListener(propertyName, listener);
        mgrs.forEach(m -> m.removeVetoableChangeListener(propertyName, listener));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getPropertyName().equals("beans")) {
            recomputeNamedBeanSet();
        }
        event.setPropagationId(this);
        if (!silencedProperties.getOrDefault(event.getPropertyName(), false)) {
            firePropertyChange(event);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return The system connection memo for the manager returned by
     *         {@link #getDefaultManager()}, or the Internal system connection
     *         memo if there is no default manager
     */
    @Override
    @Nonnull
    public SystemConnectionMemo getMemo() {
        try {
            return getDefaultManager().getMemo();
        } catch (IndexOutOfBoundsException ex) {
            return InstanceManager.getDefault(InternalSystemConnectionMemo.class);
        }
    }

    /**
     * @return The system-specific prefix letter for the primary implementation
     */
    @Override
    @Nonnull
    public String getSystemPrefix() {
        try {
            return getDefaultManager().getSystemPrefix();
        } catch (IndexOutOfBoundsException ie) {
            return "?";
        }
    }

    /**
     * @return The type letter for for the primary implementation
     */
    @Override
    public char typeLetter() {
        return getDefaultManager().typeLetter();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public String makeSystemName(@Nonnull String s) {
        return getDefaultManager().makeSystemName(s);
    }

    /** {@inheritDoc} */
    @CheckReturnValue
    @Override
    public int getObjectCount() {
        return mgrs.stream().map(m -> m.getObjectCount()).reduce(0, Integer::sum);
    }

    private TreeSet<E> namedBeanSet = null;
    protected void recomputeNamedBeanSet() {
        if (namedBeanSet != null) { // only maintain if requested
            namedBeanSet.clear();
            mgrs.forEach(m -> namedBeanSet.addAll(m.getNamedBeanSet()));
        }
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public SortedSet<E> getNamedBeanSet() {
        if (namedBeanSet == null) {
            namedBeanSet = new TreeSet<>(new NamedBeanComparator<>());
            recomputeNamedBeanSet();
        }
        return Collections.unmodifiableSortedSet(namedBeanSet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @OverridingMethodsMustInvokeSuper
    public void setPropertyChangesSilenced(String propertyName, boolean silenced) {
        // since AbstractProxyManager has no explicit constructors, acccept
        // "beans" as well as anything needed to be accepted by subclasses
        if (!"beans".equals(propertyName) && !silenceableProperties.contains(propertyName)) {
            throw new IllegalArgumentException("Property " + propertyName + " cannot be silenced.");
        }
        silencedProperties.put(propertyName, silenced);
        if (propertyName.equals("beans") && !silenced) {
            fireIndexedPropertyChange("beans", getNamedBeanSet().size(), null, null);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void addDataListener(ManagerDataListener<E> e) {
        if (e != null) listeners.add(e);
    }

    /** {@inheritDoc} */
    @Override
    public void removeDataListener(ManagerDataListener<E> e) {
        if (e != null) listeners.remove(e);
    }

    final List<ManagerDataListener<E>> listeners = new ArrayList<>();

    /**
     * {@inheritDoc}
     * From Manager.ManagerDataListener, receives notifications from underlying
     * managers.
     */
    @Override
    public void contentsChanged(Manager.ManagerDataEvent<E> e) {
    }

    /**
     * {@inheritDoc}
     * From Manager.ManagerDataListener, receives notifications from underlying
     * managers.
     */
    @Override
    public void intervalAdded(AbstractProxyManager.ManagerDataEvent<E> e) {
        if (namedBeanSet != null && e.getIndex0() == e.getIndex1()) {
            // just one element added, and we have the object reference
            namedBeanSet.add(e.getChangedBean());
        } else {
            recomputeNamedBeanSet();
        }

        if (muted) return;

        int offset = 0;
        for (Manager<E> m : mgrs) {
            if (m == e.getSource()) break;
            offset += m.getObjectCount();
        }

        ManagerDataEvent<E> eOut = new ManagerDataEvent<>(this, Manager.ManagerDataEvent.INTERVAL_ADDED, e.getIndex0()+offset, e.getIndex1()+offset, e.getChangedBean());

        listeners.forEach(m -> m.intervalAdded(eOut));
    }

    /**
     * {@inheritDoc}
     * From Manager.ManagerDataListener, receives notifications from underlying
     * managers.
     */
    @Override
    public void intervalRemoved(AbstractProxyManager.ManagerDataEvent<E> e) {
        recomputeNamedBeanSet();

        if (muted) return;

        int offset = 0;
        for (Manager<E> m : mgrs) {
            if (m == e.getSource()) break;
            offset += m.getObjectCount();
        }

        ManagerDataEvent<E> eOut = new ManagerDataEvent<>(this, Manager.ManagerDataEvent.INTERVAL_REMOVED, e.getIndex0()+offset, e.getIndex1()+offset, e.getChangedBean());

        listeners.forEach(m -> m.intervalRemoved(eOut));
    }

    private boolean muted = false;
    /** {@inheritDoc} */
    @Override
    public void setDataListenerMute(boolean m) {
        if (muted && !m) {
            // send a total update, as we haven't kept track of specifics
            ManagerDataEvent<E> e = new ManagerDataEvent<>(this, ManagerDataEvent.CONTENTS_CHANGED, 0, getObjectCount()-1, null);
            listeners.forEach((listener) -> listener.contentsChanged(e));
        }
        this.muted = m;
    }

    // initialize logging
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractProxyManager.class);

}

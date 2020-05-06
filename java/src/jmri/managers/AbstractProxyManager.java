package jmri.managers;

import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.*;
import javax.annotation.CheckReturnValue;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import jmri.*;
import jmri.jmrix.SystemConnectionMemo;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.NamedBeanComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
abstract public class AbstractProxyManager<E extends NamedBean> implements ProxyManager<E>, ProvidingManager<E>, Manager.ManagerDataListener<E> {

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
        if (defaultManager != null) { retval.add(defaultManager); }
        for (Manager<E> manager : mgrs) {
            if (manager != defaultManager && manager != internalManager) {
                retval.add(manager);
            }
        }
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

        propertyVetoListenerList.stream().forEach((l) -> {
            m.addVetoableChangeListener(l);
        });
        propertyListenerList.stream().forEach((l) -> {
            m.addPropertyChangeListener(l);
        });
        namedPropertyVetoListenerMap.entrySet().forEach((e) -> {
            e.getValue().forEach((l) -> {
                m.addVetoableChangeListener(e.getKey(), l);
            });
        });
        namedPropertyListenerMap.entrySet().forEach((e) -> {
            e.getValue().forEach((l) -> {
                m.addPropertyChangeListener(e.getKey(), l);
            });
        });

        m.addDataListener(this);
        recomputeNamedBeanSet();
        log.debug("added manager {}", m.getClass());
    }

    private Manager<E> initInternal() {
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

    /**
     * Locate via user name, then system name if needed. If that fails, create a
     * new NamedBean: If the name is a valid system name, it will be used for
     * the new NamedBean. Otherwise, the makeSystemName method will attempt to
     * turn it into a valid system name. Subclasses use this to create provider methods such as
     * getSensor or getTurnout via casts.
     *
     * @param name the user name or system name of the bean
     * @return an existing or new NamedBean
     * @throws IllegalArgumentException if name is not usable in a bean
     */
    protected E provideNamedBean(String name) throws IllegalArgumentException {
        // make sure internal present
        initInternal();

        E t = getNamedBean(name);
        if (t != null) {
            return t;
        }
        // Doesn't exist. If the systemName was specified, find that system
        Manager<E> manager = getManager(name);
        if (manager != null) {
            return makeBean(manager, name, null);
        }
        log.debug("provideNamedBean did not find manager for name {}, defer to default", name); // NOI18N
        return makeBean(getDefaultManager(), getDefaultManager().makeSystemName(name), null);
    }

    /**
     * Defer creation of the proper type to the subclass.
     *
     * @param manager    the manager to invoke
     * @param systemName the system name
     * @param userName   the user name
     * @return a bean
     */
    abstract protected E makeBean(Manager<E> manager, String systemName, String userName);

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
        Manager manager = getManager(systemName);
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
        Manager m = getManager(systemName);
        return m == null ? NameValidity.INVALID : m.validSystemNameFormat(systemName);
    }

    /**
     * Return an instance with the specified system and user names. Note that
     * two calls with the same arguments will get the same instance; there is
     * i.e. only one Sensor object representing a given physical sensor and
     * therefore only one with a specific system or user name.
     * <p>
     * This will always return a valid object reference for a valid request; a
     * new object will be created if necessary. In that case:
     * <ul>
     * <li>If a null reference is given for user name, no user name will be
     * associated with the NamedBean object created; a valid system name must be
     * provided
     * <li>If a null reference is given for the system name, a system name will
     * _somehow_ be inferred from the user name. How this is done is system
     * specific. Note: a future extension of this interface will add an
     * exception to signal that this was not possible.
     * <li>If both names are provided, the system name defines the hardware
     * access of the desired turnout, and the user address is associated with
     * it.
     * </ul>
     * Note that it is possible to make an inconsistent request if both
     * addresses are provided, but the given values are associated with
     * different objects. This is a problem, and we don't have a good solution
     * except to issue warnings. This will mostly happen if you're creating
     * NamedBean when you should be looking them up.
     *
     * @param systemName the system name
     * @param userName   the user name
     * @return requested NamedBean object (never null)
     */
    public E newNamedBean(String systemName, String userName) {
        // make sure internal present
        initInternal();

        // if the systemName is specified, find that system
        Manager<E> m = getManager(systemName);
        if (m != null) {
            return makeBean(m, systemName, userName);
        }

        // did not find a manager, allow it to default to the primary
        log.debug("Did not find manager for system name {}, delegate to primary", systemName); // NOI18N
        return makeBean(getDefaultManager(), systemName, userName);
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
     * @param managerType BeanType manager (method is used for Turnout and Sensor Managers)
     * @return a valid system name for this connection
     * @throws JmriException if systemName cannot be created
     */
    String createSystemName(String curAddress, String prefix, Class managerType) throws JmriException {
        for (Manager<E> m : mgrs) {
            if (prefix.equals(m.getSystemPrefix()) && managerType.equals(m.getClass())) {
                try {
                    if (managerType == TurnoutManager.class) {
                        return ((TurnoutManager) m).createSystemName(curAddress, prefix);
                    } else if (managerType == SensorManager.class) {
                        return ((SensorManager) m).createSystemName(curAddress, prefix);
                    } else {
                        log.warn("createSystemName requested for incompatible Manager");
                    }
                } catch (jmri.JmriException ex) {
                    throw ex;
                }
            }
        }
        throw new jmri.JmriException("Manager could not be found for System Prefix " + prefix);
    }

    public String getNextValidAddress(@Nonnull String curAddress, @Nonnull String prefix, char typeLetter) throws jmri.JmriException {
        for (Manager<E> m : mgrs) {
            log.debug("NextValidAddress requested for {}", curAddress);
            if (prefix.equals(m.getSystemPrefix()) && typeLetter == m.typeLetter()) {
                try {
                    switch (typeLetter) { // use #getDefaultManager() instead?
                        case 'T':
                            return ((TurnoutManager) m).getNextValidAddress(curAddress, prefix);
                        case 'S':
                            return ((SensorManager) m).getNextValidAddress(curAddress, prefix);
                        case 'R':
                            return ((ReporterManager) m).getNextValidAddress(curAddress, prefix);
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
     * {@inheritDoc}
     * <p>
     * Forwards the register request to the matching system.
     */
    @Override
    public void register(@Nonnull E s) {
        Manager<E> m = getManager(s.getSystemName());
        if (m != null) {
            m.register(s);
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

    /** {@inheritDoc} */
    @Nonnull
    @Override
    public List<NamedBeanPropertyDescriptor<?>> getKnownBeanProperties() {
        List<NamedBeanPropertyDescriptor<?>> l = new ArrayList<>();
        mgrs.forEach(m -> l.addAll(m.getKnownBeanProperties()));
        return l;
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
        if (!propertyListenerList.contains(l)) {
            propertyListenerList.add(l);
        }
        for (Manager<E> m : mgrs) {
            m.addPropertyChangeListener(l);
        }
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void removePropertyChangeListener(PropertyChangeListener l) {
        if (propertyListenerList.contains(l)) {
            propertyListenerList.remove(l);
        }
        for (Manager<E> m : mgrs) {
            m.removePropertyChangeListener(l);
        }
    }

    /** {@inheritDoc} */
    @Override
    @OverridingMethodsMustInvokeSuper
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        if (!namedPropertyListenerMap.containsKey(propertyName)) {
            namedPropertyListenerMap.put(propertyName, new ArrayList<>());
        }
        if (!namedPropertyListenerMap.get(propertyName).contains(listener)) {
            namedPropertyListenerMap.get(propertyName).add(listener);
        }
        for (Manager<E> m : mgrs) {
            m.addPropertyChangeListener(propertyName, listener);
        }
    }

    /** {@inheritDoc} */
    @Override
    @OverridingMethodsMustInvokeSuper
    public PropertyChangeListener[] getPropertyChangeListeners() {
        ArrayList<PropertyChangeListener> listeners = new ArrayList<>(propertyListenerList);
        for (ArrayList<PropertyChangeListener> list : namedPropertyListenerMap.values()) {
            listeners.addAll(list);
        }
        return listeners.toArray(new PropertyChangeListener[listeners.size()]);
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    @OverridingMethodsMustInvokeSuper
    public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
        if (!namedPropertyListenerMap.containsKey(propertyName)) {
            namedPropertyListenerMap.put(propertyName, new ArrayList<>());
        }
        ArrayList<PropertyChangeListener> listeners = namedPropertyListenerMap.get(propertyName);
        return listeners.toArray(new PropertyChangeListener[listeners.size()]);
    }

    /** {@inheritDoc} */
    @Override
    @OverridingMethodsMustInvokeSuper
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        if (!namedPropertyListenerMap.containsKey(propertyName)) {
            namedPropertyListenerMap.put(propertyName, new ArrayList<>());
        }
        namedPropertyListenerMap.get(propertyName).remove(listener);
        for (Manager<E> m : mgrs) {
            m.removePropertyChangeListener(propertyName, listener);
        }
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void addVetoableChangeListener(VetoableChangeListener l) {
        if (!propertyVetoListenerList.contains(l)) {
            propertyVetoListenerList.add(l);
        }
        for (Manager<E> m : mgrs) {
            m.addVetoableChangeListener(l);
        }
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void removeVetoableChangeListener(VetoableChangeListener l) {
        if (propertyVetoListenerList.contains(l)) {
            propertyVetoListenerList.remove(l);
        }
        for (Manager<E> m : mgrs) {
            m.removeVetoableChangeListener(l);
        }
    }

    /** {@inheritDoc} */
    @Override
    @OverridingMethodsMustInvokeSuper
    public void addVetoableChangeListener(String propertyName, VetoableChangeListener listener) {
        if (!namedPropertyVetoListenerMap.containsKey(propertyName)) {
            namedPropertyVetoListenerMap.put(propertyName, new ArrayList<>());
        }
        if (!namedPropertyVetoListenerMap.get(propertyName).contains(listener)) {
            namedPropertyVetoListenerMap.get(propertyName).add(listener);
        }
        for (Manager<E> m : mgrs) {
            m.addVetoableChangeListener(propertyName, listener);
        }
    }

    /** {@inheritDoc} */
    @Override
    @OverridingMethodsMustInvokeSuper
    public VetoableChangeListener[] getVetoableChangeListeners() {
        ArrayList<VetoableChangeListener> listeners = new ArrayList<>(propertyVetoListenerList);
        for (ArrayList<VetoableChangeListener> list : namedPropertyVetoListenerMap.values()) {
            listeners.addAll(list);
        }
        return listeners.toArray(new VetoableChangeListener[listeners.size()]);
    }

    /** {@inheritDoc} */
    @Override
    @OverridingMethodsMustInvokeSuper
    public VetoableChangeListener[] getVetoableChangeListeners(String propertyName) {
        if (!namedPropertyVetoListenerMap.containsKey(propertyName)) {
            namedPropertyVetoListenerMap.put(propertyName, new ArrayList<>());
        }
        ArrayList<VetoableChangeListener> listeners = namedPropertyVetoListenerMap.get(propertyName);
        return listeners.toArray(new VetoableChangeListener[listeners.size()]);
    }

    /** {@inheritDoc} */
    @Override
    @OverridingMethodsMustInvokeSuper
    public void removeVetoableChangeListener(String propertyName, VetoableChangeListener listener) {
        if (!namedPropertyVetoListenerMap.containsKey(propertyName)) {
            namedPropertyVetoListenerMap.put(propertyName, new ArrayList<>());
        }
        namedPropertyVetoListenerMap.get(propertyName).remove(listener);
        for (Manager<E> m : mgrs) {
            m.removeVetoableChangeListener(propertyName, listener);
        }
    }

    ArrayList<PropertyChangeListener> propertyListenerList = new ArrayList<>();
    HashMap<String, ArrayList<PropertyChangeListener>> namedPropertyListenerMap = new HashMap<>();
    ArrayList<VetoableChangeListener> propertyVetoListenerList = new ArrayList<>();
    HashMap<String, ArrayList<VetoableChangeListener>> namedPropertyVetoListenerMap = new HashMap<>();

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
     * @return A system name from a user input, typically a number, from the
     *         primary system.
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
        int count = 0;
        for (Manager<E> m : mgrs) { count += m.getObjectCount(); }
        return count;
    }

    /** {@inheritDoc} */
    @Nonnull
    @Override
    @Deprecated  // will be removed when superclass method is removed due to @Override
    public List<String> getSystemNameList() {
        // jmri.util.Log4JUtil.deprecationWarning(log, "getSystemNameList"); // used by configureXML
        List<E> list = getNamedBeanList();
        ArrayList<String> retval = new ArrayList<>(list.size());
        for (E e : list) retval.add(e.getSystemName());
        return Collections.unmodifiableList(retval);
    }

    /** {@inheritDoc} */
    @Override
    @Deprecated  // will be removed when superclass method is removed due to @Override
    @Nonnull
    public List<E> getNamedBeanList() {
        // jmri.util.Log4JUtil.deprecationWarning(log, "getNamedBeanList"); // used by getSystemNameList
        // by doing this in order by manager and from each managers ordered sets, its finally in order
        ArrayList<E> tl = new ArrayList<>();
        for (Manager<E> m : mgrs) {
            tl.addAll(m.getNamedBeanSet());
        }
        return Collections.unmodifiableList(tl);
    }

    private TreeSet<E> namedBeanSet = null;
    protected void recomputeNamedBeanSet() {
        if (namedBeanSet == null) return; // only maintain if requested
        namedBeanSet.clear();
        for (Manager<E> m : mgrs) {
            namedBeanSet.addAll(m.getNamedBeanSet());
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

        ManagerDataEvent<E> eOut = new ManagerDataEvent<E>(this, Manager.ManagerDataEvent.INTERVAL_ADDED, e.getIndex0()+offset, e.getIndex1()+offset, e.getChangedBean());

        for (ManagerDataListener<E> m : listeners) {
            m.intervalAdded(eOut);
        }
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

        ManagerDataEvent<E> eOut = new ManagerDataEvent<E>(this, Manager.ManagerDataEvent.INTERVAL_REMOVED, e.getIndex0()+offset, e.getIndex1()+offset, e.getChangedBean());

        for (ManagerDataListener<E> m : listeners) {
            m.intervalRemoved(eOut);
        }
    }

    private boolean muted = false;
    /** {@inheritDoc} */
    @Override
    public void setDataListenerMute(boolean m) {
        if (muted && !m) {
            // send a total update, as we haven't kept track of specifics
            ManagerDataEvent<E> e = new ManagerDataEvent<E>(this, ManagerDataEvent.CONTENTS_CHANGED, 0, getObjectCount()-1, null);
            for (ManagerDataListener<E> listener : listeners) {
                listener.contentsChanged(e);
            }
        }
        this.muted = m;
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(AbstractProxyManager.class);

}

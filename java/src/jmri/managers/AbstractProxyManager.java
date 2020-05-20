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
@SuppressWarnings("deprecation")
abstract public class AbstractProxyManager<E extends NamedBean> extends VetoableChangeSupport implements ProxyManager<E>, ProvidingManager<E>, PropertyChangeListener, Manager.ManagerDataListener<E> {

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
    @SuppressWarnings("deprecation")
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
    public void propertyChange(PropertyChangeEvent e) {
        PropertyChangeEvent event = e;
        if (event.getPropertyName().equals("beans")) {
            recomputeNamedBeanSet();
        }
        event.setPropagationId(this);
        firePropertyChange(event);
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

    /** {@inheritDoc} */
    @Nonnull
    @Override
    @Deprecated  // will be removed when superclass method is removed due to @Override
    public List<String> getSystemNameList() {
        // jmri.util.Log4JUtil.deprecationWarning(log, "getSystemNameList"); // used by configureXML
        List<E> list = getNamedBeanList();
        ArrayList<String> retval = new ArrayList<>(list.size());
        list.forEach(e -> retval.add(e.getSystemName()));
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
        mgrs.forEach(m -> tl.addAll(m.getNamedBeanSet()));
        return Collections.unmodifiableList(tl);
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

    /** {@inheritDoc} */
    @Override
    @Deprecated
    public void addDataListener(ManagerDataListener<E> e) {
        if (e != null) listeners.add(e);
    }

    /** {@inheritDoc} */
    @Override
    @Deprecated
    public void removeDataListener(ManagerDataListener<E> e) {
        if (e != null) listeners.remove(e);
    }

    @SuppressWarnings("deprecation")
    final List<ManagerDataListener<E>> listeners = new ArrayList<>();

    /**
     * {@inheritDoc}
     * From Manager.ManagerDataListener, receives notifications from underlying
     * managers.
     */
    @Override
    @Deprecated
    public void contentsChanged(Manager.ManagerDataEvent<E> e) {
    }

    /**
     * {@inheritDoc}
     * From Manager.ManagerDataListener, receives notifications from underlying
     * managers.
     */
    @Override
    @Deprecated
    @SuppressWarnings("deprecation")
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
    @Deprecated
    @SuppressWarnings("deprecation")
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
    @Deprecated
    @SuppressWarnings("deprecation")
    public void setDataListenerMute(boolean m) {
        if (muted && !m) {
            // send a total update, as we haven't kept track of specifics
            ManagerDataEvent<E> e = new ManagerDataEvent<>(this, ManagerDataEvent.CONTENTS_CHANGED, 0, getObjectCount()-1, null);
            listeners.forEach((listener) -> listener.contentsChanged(e));
        }
        this.muted = m;
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(AbstractProxyManager.class);

}

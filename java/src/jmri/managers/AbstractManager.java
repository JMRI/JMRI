package jmri.managers;

import java.beans.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.Manager;
import jmri.NamedBean;
import jmri.NamedBean.DuplicateSystemNameException;
import jmri.NamedBeanPropertyDescriptor;
import jmri.jmrix.SystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract partial implementation for all Manager-type classes.
 * <p>
 * Note that this does not enforce any particular system naming convention at
 * the present time. They're just names...
 * <p>
 * It does include, with AbstractNamedBean, the implementation of the normalized
 * user name.
 * <p>
 * See source file for extensive implementation notes.
 *
 * @param <E> the class this manager supports
 * @see jmri.NamedBean#normalizeUserName
 *
 * @author Bob Jacobsen Copyright (C) 2003
 */
public abstract class AbstractManager<E extends NamedBean> implements Manager<E>, PropertyChangeListener, VetoableChangeListener {

    // The data model consists of several components:
    // * The primary reference is _beans, a SortedSet of NamedBeans, sorted automatically on system name.
    //      Currently that's implemented as a TreeSet; further performance work might change that
    //      Live access is available as an unmodifiableSortedSet via getNamedBeanSet()
    // * The manager also maintains synchronized maps from SystemName -> NamedBean (_tsys) and UserName -> NamedBean (_tuser)
    //      These are not made available: get access through the manager calls
    //      These use regular HashMaps instead of some sorted form for efficiency
    // * Caches for the String[] getSystemNameArray(), List<String> getSystemNameList() and List<E> getNamedBeanList() calls

    protected final SystemConnectionMemo memo;
    protected final TreeSet<E> _beans;
    protected final Hashtable<String, E> _tsys = new Hashtable<>();   // stores known E (NamedBean, i.e. Turnout) instances by system name
    protected final Hashtable<String, E> _tuser = new Hashtable<>();  // stores known E (NamedBean, i.e. Turnout) instances by user name

    // caches
    private String[] cachedSystemNameArray = null;
    private ArrayList<String> cachedSystemNameList = null;
    private ArrayList<E> cachedNamedBeanList = null;

    // Auto names. The atomic integer is always created even if not used, to
    // simplify concurrency.
    AtomicInteger lastAutoNamedBeanRef = new AtomicInteger(0);
    DecimalFormat paddedNumber = new DecimalFormat("0000");
            
    public AbstractManager(SystemConnectionMemo memo) {
        this.memo = memo;
        this._beans = new TreeSet<>(memo.getNamedBeanComparator(getNamedBeanClass()));
        registerSelf();
    }

    /**
     * By default, register this manager to store as configuration information.
     * Override to change that.
     */
    @OverridingMethodsMustInvokeSuper
    protected void registerSelf() {
        log.debug("registerSelf for config of type {}", getClass());
        InstanceManager.getOptionalDefault(ConfigureManager.class).ifPresent(cm -> {
            cm.registerConfig(this, getXMLOrder());
            log.debug("registering for config of type {}", getClass());
        });
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public SystemConnectionMemo getMemo() {
        return memo;
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public String makeSystemName(@Nonnull String s, boolean logErrors, Locale locale) {
        try {
            return Manager.super.makeSystemName(s, logErrors, locale);
        } catch (IllegalArgumentException ex) {
            if (logErrors || log.isTraceEnabled()) {
                log.error("Invalid system name for {}: {}", getBeanTypeHandled(), ex.getMessage());
            }
            throw ex;
        }
    }

    /** {@inheritDoc} */
    @Override
    @OverridingMethodsMustInvokeSuper
    public void dispose() {
        InstanceManager.getOptionalDefault(ConfigureManager.class).ifPresent(cm -> cm.deregister(this));
        _beans.clear();
        _tsys.clear();
        _tuser.clear();
    }

    /**
     * Get a NamedBean by its system name.
     *
     * @param systemName the system name
     * @return the result of {@link #getBeanBySystemName(java.lang.String)}
     *         with systemName
     * @deprecated since 4.15.6; use
     * {@link #getBeanBySystemName(java.lang.String)} instead
     */
    @Deprecated
    protected E getInstanceBySystemName(String systemName) {
        return getBeanBySystemName(systemName);
    }

    /**
     * Get a NamedBean by its user name.
     *
     * @param userName the user name
     * @return the result of {@link #getBeanByUserName(java.lang.String)} call,
     *         with userName
     * @deprecated since 4.15.6; use
     * {@link #getBeanByUserName(java.lang.String)} instead
     */
    @Deprecated
    protected E getInstanceByUserName(String userName) {
        return getBeanByUserName(userName);
    }

    /** {@inheritDoc} */
    @Override
    public E getBeanBySystemName(@Nonnull String systemName) {
        return _tsys.get(systemName);
    }

    /**
     * Protected method used by subclasses to over-ride the default behavior of
     * getBeanBySystemName when a simple string lookup is not sufficient
     *
     * @param systemName the system name to check.
     * @param comparator a Comparator encapsulating the system specific comparison behavior.
     * @return A named bean of the appropriate type, or null if not found.
     */
    protected E getBySystemName(String systemName,Comparator<String> comparator){
        for(Map.Entry<String,E> e:_tsys.entrySet()){
            if(0==comparator.compare(e.getKey(),systemName)){
                return e.getValue();
            }
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public E getBeanByUserName(@Nonnull String userName) {
        String normalizedUserName = NamedBean.normalizeUserName(userName);
        return normalizedUserName != null ? _tuser.get(normalizedUserName) : null;
    }

    /** {@inheritDoc} */
    @Override
    public E getNamedBean(@Nonnull String name) {
        String normalizedUserName = NamedBean.normalizeUserName(name);
        if (normalizedUserName != null) {
            E b = getBeanByUserName(normalizedUserName);
            if (b != null) {
                return b;
            }
        }
        return getBeanBySystemName(name);
    }

    /** {@inheritDoc} */
    @Override
    @OverridingMethodsMustInvokeSuper
    public void deleteBean(@Nonnull E bean, @Nonnull String property) throws PropertyVetoException {
        // throws PropertyVetoException if vetoed
        fireVetoableChange(property, bean, null);
        if (property.equals("DoDelete")) { // NOI18N
            deregister(bean);
            bean.dispose();
        }
    }

    /** {@inheritDoc} */
    @Override
    @OverridingMethodsMustInvokeSuper
    public void register(@Nonnull E s) {
        String systemName = s.getSystemName();

        E existingBean = getBeanBySystemName(systemName);
        if (existingBean != null) {
            if (s == existingBean) {
                log.debug("the named bean is registered twice: {}", systemName);
            } else {
                log.error("systemName is already registered: {}", systemName);
                throw new DuplicateSystemNameException("systemName is already registered: " + systemName);
            }
        } else {
            // Check if the manager already has a bean with a system name that is
            // not equal to the system name of the new bean, but there the two
            // system names are treated as the same. For example LT1 and LT01.
            if (_beans.contains(s)) {
                final AtomicReference<String> oldSysName = new AtomicReference<>();
                Comparator<E> c = memo.getNamedBeanComparator(getNamedBeanClass());
                _beans.forEach(t -> {
                    if (c.compare(s, t) == 0) {
                        oldSysName.set(t.getSystemName());
                    }
                });
                if (!systemName.equals(oldSysName.get())) {
                    String msg = String.format("systemName is already registered. Current system name: %s. New system name: %s",
                            oldSysName, systemName);
                    log.error(msg);
                    throw new DuplicateSystemNameException(msg);
                }
            }
        }

        // clear caches
        cachedSystemNameArray = null;
        cachedSystemNameList = null;
        cachedNamedBeanList = null;
        
        // save this bean
        _beans.add(s);
        _tsys.put(systemName, s);
        registerUserName(s);

        // notifications
        int position = getPosition(s);
        fireDataListenersAdded(position, position, s);
        fireIndexedPropertyChange("beans", position, null, s);
        firePropertyChange("length", null, _beans.size());
        // listen for name and state changes to forward
        s.addPropertyChangeListener(this);
    }

    // not efficient, but does job for now
    private int getPosition(E s) {
        int position = 0;
        for (E bean : _beans) {
            if (s == bean) {
                return position;
            }
            position++;
        }
        return -1;
    }

    /**
     * Invoked by {@link #register(NamedBean)} to register the user name of the
     * bean.
     *
     * @param s the bean to register
     */
    protected void registerUserName(E s) {
        String userName = s.getUserName();
        if (userName == null) {
            return;
        }

        handleUserNameUniqueness(s);
        // since we've handled uniqueness,
        // store the new bean under the name
        _tuser.put(userName, s);
    }

    /**
     * Invoked by {@link #registerUserName(NamedBean)} to ensure uniqueness of
     * the NamedBean during registration.
     *
     * @param s the bean to register
     */
    protected void handleUserNameUniqueness(E s) {
        String userName = s.getUserName();
        // enforce uniqueness of user names
        // by setting username to null in any existing bean with the same name
        // Note that this is not a "move" operation for the user name
        if (userName != null && _tuser.get(userName) != null && _tuser.get(userName) != s) {
            _tuser.get(userName).setUserName(null);
        }
    }

    /** {@inheritDoc} */
    @Override
    @OverridingMethodsMustInvokeSuper
    public void deregister(E s) {
        int position = getPosition(s);

        // clear caches
        cachedSystemNameArray = null;
        cachedSystemNameList = null;
        cachedNamedBeanList = null;

        // stop listening for user name changes
        s.removePropertyChangeListener(this);
        
        // remove bean from local storage
        String systemName = s.getSystemName();
        _beans.remove(s);
        _tsys.remove(systemName);
        String userName = s.getUserName();
        if (userName != null) {
            _tuser.remove(userName);
        }
        
        // notifications
        fireDataListenersRemoved(position, position, s);
        fireIndexedPropertyChange("beans", position, s, null);
        firePropertyChange("length", null, _beans.size());
    }

    /**
     * By default there are no custom properties.
     *
     * @return empty list
     */
    @Override
    @Nonnull
    public List<NamedBeanPropertyDescriptor<?>> getKnownBeanProperties() {
        return new LinkedList<>();
    }

    /**
     * The PropertyChangeListener interface in this class is intended to keep
     * track of user name changes to individual NamedBeans. It is not completely
     * implemented yet. In particular, listeners are not added to newly
     * registered objects.
     *
     * @param e the event
     */
    @Override
    @SuppressWarnings("unchecked") // The cast of getSource() to E can't be checked due to type erasure, but we catch errors
    @OverridingMethodsMustInvokeSuper
    public void propertyChange(PropertyChangeEvent e) {
        if (e.getPropertyName().equals("UserName")) {
            String old = (String) e.getOldValue();  // previous user name
            String now = (String) e.getNewValue();  // current user name
            try { // really should always succeed
                E t = (E) e.getSource();
                if (old != null) {
                    _tuser.remove(old); // remove old name for this bean
                }
                if (now != null) {
                    // was there previously a bean with the new name?
                    if (_tuser.get(now) != null && _tuser.get(now) != t) {
                        // If so, clear. Note that this is not a "move" operation
                        _tuser.get(now).setUserName(null);
                    }

                    _tuser.put(now, t); // put new name for this bean
                }
            } catch (ClassCastException ex) {
                log.error("Received event of wrong type {}", e.getSource().getClass().getName(), ex);
            }

            // called DisplayListName, as DisplayName might get used at some point by a NamedBean
            firePropertyChange("DisplayListName", old, now); //IN18N
        }
    }

    /** {@inheritDoc} */
    @Override
    @CheckReturnValue
    public int getObjectCount() { return _beans.size();}    

    /** {@inheritDoc} */
    @Override
    @Nonnull
    @Deprecated  // will be removed when superclass method is removed due to @Override
    public String[] getSystemNameArray() {
        jmri.util.Log4JUtil.deprecationWarning(log, "getSystemNameArray");
        if (log.isTraceEnabled()) log.trace("Manager#getSystemNameArray() called", new Exception("traceback"));

        if (cachedSystemNameArray == null) {
            cachedSystemNameArray = getSystemNameList().toArray(new String[_beans.size()]);
        }
        return cachedSystemNameArray;
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    @Deprecated  // will be removed when superclass method is removed due to @Override
    public List<String> getSystemNameList() {
        // jmri.util.Log4JUtil.deprecationWarning(log, "getSystemNameList");
        if (cachedSystemNameList == null) {
            cachedSystemNameList = new ArrayList<>();
            for (E b :_beans) {
                cachedSystemNameList.add(b.getSystemName());
            }
        }
        return Collections.unmodifiableList(cachedSystemNameList);
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    @Deprecated  // will be removed when superclass method is removed due to @Override
    public List<E> getNamedBeanList() {
        jmri.util.Log4JUtil.deprecationWarning(log, "getNamedBeanList");
        if (cachedNamedBeanList == null) {
            cachedNamedBeanList = new ArrayList<>(_beans);
        }
        return Collections.unmodifiableList(cachedNamedBeanList);
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public SortedSet<E> getNamedBeanSet() {
        return Collections.unmodifiableSortedSet(_beans);
    }

    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    /** {@inheritDoc} */
    @Override
    @OverridingMethodsMustInvokeSuper
    public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    /** {@inheritDoc} */
    @Override
    @OverridingMethodsMustInvokeSuper
    public synchronized void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    /** {@inheritDoc} */
    @Override
    @OverridingMethodsMustInvokeSuper
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    @OverridingMethodsMustInvokeSuper
    public PropertyChangeListener[] getPropertyChangeListeners() {
        return pcs.getPropertyChangeListeners();
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    @OverridingMethodsMustInvokeSuper
    public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
        return pcs.getPropertyChangeListeners(propertyName);
    }

    /** {@inheritDoc} */
    @Override
    @OverridingMethodsMustInvokeSuper
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(propertyName, listener);
    }

    @OverridingMethodsMustInvokeSuper
    protected void firePropertyChange(String p, Object old, Object n) {
        pcs.firePropertyChange(p, old, n);
    }

    @OverridingMethodsMustInvokeSuper
    protected void fireIndexedPropertyChange(String propertyName, int index, Object oldValue, Object newValue) {
        pcs.fireIndexedPropertyChange(propertyName, index, oldValue, newValue);
    }

    private VetoableChangeSupport vcs = new VetoableChangeSupport(this);

    /** {@inheritDoc} */
    @Override
    @OverridingMethodsMustInvokeSuper
    public synchronized void addVetoableChangeListener(VetoableChangeListener l) {
        vcs.addVetoableChangeListener(l);
    }

    /** {@inheritDoc} */
    @Override
    @OverridingMethodsMustInvokeSuper
    public synchronized void removeVetoableChangeListener(VetoableChangeListener l) {
        vcs.removeVetoableChangeListener(l);
    }

    /** {@inheritDoc} */
    @Override
    @OverridingMethodsMustInvokeSuper
    public void addVetoableChangeListener(String propertyName, VetoableChangeListener listener) {
        vcs.addVetoableChangeListener(propertyName, listener);
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    @OverridingMethodsMustInvokeSuper
    public VetoableChangeListener[] getVetoableChangeListeners() {
        return vcs.getVetoableChangeListeners();
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    @OverridingMethodsMustInvokeSuper
    public VetoableChangeListener[] getVetoableChangeListeners(String propertyName) {
        return vcs.getVetoableChangeListeners(propertyName);
    }

    /** {@inheritDoc} */
    @Override
    @OverridingMethodsMustInvokeSuper
    public void removeVetoableChangeListener(String propertyName, VetoableChangeListener listener) {
        vcs.removeVetoableChangeListener(propertyName, listener);
    }

    /**
     * Method to inform all registered listeners of a vetoable change. If the
     * propertyName is "CanDelete" ALL listeners with an interest in the bean
     * will throw an exception, which is recorded returned back to the invoking
     * method, so that it can be presented back to the user. However if a
     * listener decides that the bean can not be deleted then it should throw an
     * exception with a property name of "DoNotDelete", this is thrown back up
     * to the user and the delete process should be aborted.
     *
     * @param p   The programmatic name of the property that is to be changed.
     *            "CanDelete" will enquire with all listerners if the item can
     *            be deleted. "DoDelete" tells the listerner to delete the item.
     * @param old The old value of the property.
     * @param n   The new value of the property.
     * @throws PropertyVetoException - if the recipients wishes the delete to be
     *                               aborted.
     */
    @OverridingMethodsMustInvokeSuper
    protected void fireVetoableChange(String p, Object old, Object n) throws PropertyVetoException {
        PropertyChangeEvent evt = new PropertyChangeEvent(this, p, old, n);
        if (p.equals("CanDelete")) { //IN18N
            StringBuilder message = new StringBuilder();
            for (VetoableChangeListener vc : vcs.getVetoableChangeListeners()) {
                try {
                    vc.vetoableChange(evt);
                } catch (PropertyVetoException e) {
                    if (e.getPropertyChangeEvent().getPropertyName().equals("DoNotDelete")) { //IN18N
                        log.info(e.getMessage());
                        throw e;
                    }
                    message.append(e.getMessage());
                    message.append("<hr>"); //IN18N
                }
            }
            throw new PropertyVetoException(message.toString(), evt);
        } else {
            try {
                vcs.fireVetoableChange(evt);
            } catch (PropertyVetoException e) {
                log.error("Change vetoed.", e);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    @OverridingMethodsMustInvokeSuper
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {

        if ("CanDelete".equals(evt.getPropertyName())) { //IN18N
            StringBuilder message = new StringBuilder();
            message.append(Bundle.getMessage("VetoFoundIn", getBeanTypeHandled()))
                    .append("<ul>");
            boolean found = false;
            for (NamedBean nb : _beans) {
                try {
                    nb.vetoableChange(evt);
                } catch (PropertyVetoException e) {
                    if (e.getPropertyChangeEvent().getPropertyName().equals("DoNotDelete")) { //IN18N
                        throw e;
                    }
                    found = true;
                    message.append("<li>")
                            .append(e.getMessage())
                            .append("</li>");
                }
            }
            message.append("</ul>")
                    .append(Bundle.getMessage("VetoWillBeRemovedFrom", getBeanTypeHandled()));
            if (found) {
                throw new PropertyVetoException(message.toString(), evt);
            }
        } else {
            for (NamedBean nb : _beans) {
                // throws PropertyVetoException if vetoed
                nb.vetoableChange(evt);
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link jmri.Manager.NameValidity#INVALID} if system name does not
     *         start with
     *         {@link #getSystemNamePrefix()}; {@link jmri.Manager.NameValidity#VALID_AS_PREFIX_ONLY}
     *         if system name equals {@link #getSystemNamePrefix()}; otherwise
     *         {@link jmri.Manager.NameValidity#VALID} to allow Managers that do
     *         not perform more specific validation to be considered valid.
     */
    @Override
    public NameValidity validSystemNameFormat(@Nonnull String systemName) {
        if (getSystemNamePrefix().equals(systemName)) {
            return NameValidity.VALID_AS_PREFIX_ONLY;
        }
        return systemName.startsWith(getSystemNamePrefix()) ? NameValidity.VALID : NameValidity.INVALID;
    }

    /**
     * {@inheritDoc}
     * 
     * The implementation in {@link AbstractManager} should be final, but is not
     * for four managers that have arbitrary prefixes.
     */
    @Override
    @Nonnull
    public final String getSystemPrefix() {
        return memo.getSystemPrefix();
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

    private final List<ManagerDataListener<E>> listeners = new ArrayList<>();

    private boolean muted = false;
    
    /** {@inheritDoc} */
    @Override
    public void setDataListenerMute(boolean m) {
        if (muted && !m) {
            // send a total update, as we haven't kept track of specifics
            ManagerDataEvent<E> e = new ManagerDataEvent<>(this, ManagerDataEvent.CONTENTS_CHANGED, 0, getObjectCount()-1, null);
            for (ManagerDataListener<E> listener : listeners) {
                listener.contentsChanged(e);
            }          
        }
        this.muted = m;
    }

    protected void fireDataListenersAdded(int start, int end, E changedBean) {
        if (muted) return;
        ManagerDataEvent<E> e = new ManagerDataEvent<>(this, ManagerDataEvent.INTERVAL_ADDED, start, end, changedBean);
        for (ManagerDataListener<E> m : listeners) {
            m.intervalAdded(e);
        }
    }
    protected void fireDataListenersRemoved(int start, int end, E changedBean) {
        if (muted) return;
        ManagerDataEvent<E> e = new ManagerDataEvent<>(this, ManagerDataEvent.INTERVAL_REMOVED, start, end, changedBean);
        for (ManagerDataListener<E> m : listeners) {
            m.intervalRemoved(e);
        }
    }

    public void updateAutoNumber(String systemName) {
        /* The following keeps track of the last created auto system name.
         currently we do not reuse numbers, although there is nothing to stop the
         user from manually recreating them */
        if (systemName.startsWith(getSystemNamePrefix() + ":AUTO:")) {
            try {
                int autoNumber = Integer.parseInt(systemName.substring(8));
                lastAutoNamedBeanRef.accumulateAndGet(autoNumber, Math::max);
            } catch (NumberFormatException e) {
                log.warn("Auto generated SystemName {} is not in the correct format", systemName);
            }
        }
    }

    public String getAutoSystemName() {
        int nextAutoBlockRef = lastAutoNamedBeanRef.incrementAndGet();
        StringBuilder b = new StringBuilder(getSystemNamePrefix() + ":AUTO:");
        String nextNumber = paddedNumber.format(nextAutoBlockRef);
        b.append(nextNumber);
        return b.toString();
    }

    private static final Logger log = LoggerFactory.getLogger(AbstractManager.class);

}

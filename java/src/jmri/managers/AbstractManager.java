package jmri.managers;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.util.*;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.Manager;
import jmri.NamedBean;
import jmri.NamedBeanPropertyDescriptor;
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
abstract public class AbstractManager<E extends NamedBean> implements Manager<E>, PropertyChangeListener, VetoableChangeListener {

    // The data model consists of several components:
    // * The primary reference is _beans, a SortedSet of NamedBeans, sorted automatically on system name.
    //      Currently that's implemented as a TreeSet; further performance work might change that
    //      Live access is available as a unmodifiableSortedSet via getNamedBeanSet()
    // * The manager also maintains synchronized maps from SystemName -> NamedBean (_tsys) and UserName -> NamedBean (_tuser)
    //      These are not made available: get access through the manager calls
    //      These use regular HashMaps instead of some sorted form for efficiency
    // * An unmodifiable ArrayList<String> in the original add order, _originalOrderList, remains available 
    //      for the deprecated getSystemNameAddedOrderList
    //      This is present so that ConfigureXML can still store in the original order
    // * Caches for the String[] getSystemNameArray(), List<String> getSystemNameList() and List<E> getNamedBeanList() calls
            
    public AbstractManager() {
        registerSelf();
    }

    /**
     * By default, register this manager to store as configuration information.
     * Override to change that.
     */
    @OverridingMethodsMustInvokeSuper
    protected void registerSelf() {
        log.debug("registerSelf for config of type {}", getClass());
        InstanceManager.getOptionalDefault(ConfigureManager.class).ifPresent((cm) -> {
            cm.registerConfig(this, getXMLOrder());
            log.debug("registering for config of type {}", getClass());
        });
    }

    /** {@inheritDoc} */
    @Override
    abstract public int getXMLOrder();

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public String makeSystemName(@Nonnull String s) {
        return getSystemPrefix() + typeLetter() + s;
    }

    /** {@inheritDoc} */
    @Override
    @OverridingMethodsMustInvokeSuper
    public void dispose() {
        InstanceManager.getOptionalDefault(ConfigureManager.class).ifPresent((cm) -> {
            cm.deregister(this);
        });
        _beans.clear();
        _tsys.clear();
        _tuser.clear();
    }

    protected TreeSet<E> _beans = new TreeSet<>(new jmri.util.NamedBeanComparator());
    protected Hashtable<String, E> _tsys = new Hashtable<>();   // stores known E (NamedBean, i.e. Turnout) instances by system name
    protected Hashtable<String, E> _tuser = new Hashtable<>();   // stores known E (NamedBean, i.e. Turnout) instances by user name
    // Storage for getSystemNameOriginalList
    protected ArrayList<String> _originalOrderList = new ArrayList<>();
    // caches
    private String[] cachedSystemNameArray = null;
    private ArrayList<String> cachedSystemNameList = null;
    private ArrayList<E> cachedNamedBeanList = null;
    
    /**
     * Locate an instance based on a system name. Returns null if no instance
     * already exists. This is intended to be used by concrete classes to
     * implement their getBySystemName method. We can't call it that here
     * because Java doesn't have polymorphic return types.
     *
     * @param systemName the system name
     * @return requested NamedBean object or null if none exists
     */
    protected E getInstanceBySystemName(String systemName) {
        return _tsys.get(systemName);
    }

    /**
     * Locate an instance based on a user name. Returns null if no instance
     * already exists. This is intended to be used by concrete classes to
     * implement their getBySystemName method. We cant call it that here because
     * Java doesn't have polymorphic return types.
     *
     * @param userName the user name
     * @return requested E (NamedBean, i.e. Turnout) object or null if none exists
     */
    protected E getInstanceByUserName(String userName) {
        String normalizedUserName = NamedBean.normalizeUserName(userName);
        return normalizedUserName != null ? _tuser.get(normalizedUserName) : null;
    }

    /** {@inheritDoc} */
    @Override
    public E getBeanBySystemName(String systemName) {
        return _tsys.get(systemName);
    }

    /** {@inheritDoc} */
    @Override
    public E getBeanByUserName(String userName) {
        String normalizedUserName = NamedBean.normalizeUserName(userName);
        return normalizedUserName != null ? _tuser.get(normalizedUserName) : null;
    }

    /** {@inheritDoc} */
    @Override
    public E getNamedBean(String name) {
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
        try {
            fireVetoableChange(property, bean, null);
        } catch (PropertyVetoException e) {
            throw e;  // don't go on to check for delete.
        }
        if (property.equals("DoDelete")) { //IN18N
            deregister(bean);
            bean.dispose();
        }
    }

    /** {@inheritDoc} */
    @Override
    @OverridingMethodsMustInvokeSuper
    public void register(E s) {
        String systemName = s.getSystemName();

        // clear caches
        cachedSystemNameArray = null;
        cachedSystemNameList = null;
        cachedNamedBeanList = null;
        
        // save this bean
        _beans.add(s);
        _tsys.put(systemName, s);
        _originalOrderList.add(systemName);
        registerUserName(s);

        // notifications
        firePropertyChange("length", null, _beans.size());
        int position = getPosition(s);
        fireDataListenersAdded(position, position, s);
        // listen for name and state changes to forward
        s.addPropertyChangeListener(this, "", "Manager");
    }

    // not efficient, but does job for now
    private int getPosition(E s) {
        int position = 0;
        Iterator<E> iter = _beans.iterator();
        while (iter.hasNext()) {
            if (s == iter.next()) return position;
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
        if (userName != null) {
            // enforce uniqueness of user names
            // by setting username to null in any existing bean with the same name
            // Note that this is not a "move" operation for the user name
            if (_tuser.get(userName) != null && _tuser.get(userName) != s) {
                _tuser.get(userName).setUserName(null);
            }
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
        _originalOrderList.remove(systemName);
        
        // notifications
        firePropertyChange("length", null, _beans.size());
        fireDataListenersRemoved(position, position, s);
    }

    /**
     * By default there are no custom properties.
     * @return empty list
     */
    @Override
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
    public String[] getSystemNameArray() {
        if (cachedSystemNameArray == null) {
            cachedSystemNameArray = getSystemNameList().toArray(new String[_beans.size()]);
        }
        return cachedSystemNameArray;
    }

    
    /** {@inheritDoc} */
    @Override
    public List<String> getSystemNameList() {
        if (cachedSystemNameList == null) {
            cachedSystemNameList = new ArrayList<>();
            for (E b : _beans) {
                cachedSystemNameList.add(b.getSystemName());
            }
        }
        return Collections.unmodifiableList(cachedSystemNameList);
    }


    /** {@inheritDoc} */
    @Override
    public List<String> getSystemNameAddedOrderList() {
        return Collections.unmodifiableList(_originalOrderList);
    }


    /** {@inheritDoc} */
    @Override
    public List<E> getNamedBeanList() {
        if (cachedNamedBeanList == null) {
            cachedNamedBeanList = new ArrayList<>(_beans);
        }
        return Collections.unmodifiableList(cachedNamedBeanList);
    }

    /** {@inheritDoc} */
    @Override
    public SortedSet<E> getNamedBeanSet() {
        return Collections.unmodifiableSortedSet(_beans);
    }

    /** {@inheritDoc} */
    @Override
    abstract public String getBeanTypeHandled();

    PropertyChangeSupport pcs = new PropertyChangeSupport(this);

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

    @OverridingMethodsMustInvokeSuper
    protected void firePropertyChange(String p, Object old, Object n) {
        pcs.firePropertyChange(p, old, n);
    }

    VetoableChangeSupport vcs = new VetoableChangeSupport(this);

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

    /**
     * Method to inform all registered listerners of a vetoable change. If the
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
                try {
                    nb.vetoableChange(evt);
                } catch (PropertyVetoException e) {
                    throw e;
                }
            }
        }
    }

    /** {@inheritDoc} */
    @CheckReturnValue
    @Override
    @Nonnull
    public String normalizeSystemName(@Nonnull String inputName) throws NamedBean.BadSystemNameException {
        return inputName;
    }

    /**
     * {@inheritDoc}
     *
     * @return always 'VALID' to let undocumented connection system
     *         managers pass entry validation.
     */
    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        return NameValidity.VALID;
    }

    /** {@inheritDoc} */
    public void addDataListener(ManagerDataListener<E> e) {
        if (e != null) listeners.add(e);
    }

    /** {@inheritDoc} */
    public void removeDataListener(ManagerDataListener<E> e) {
        if (e != null) listeners.remove(e);
    }

    final List<ManagerDataListener<E>> listeners = new ArrayList<>();    

    private boolean muted = false;
    
    /** {@inheritDoc} */
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

    protected void fireDataListenersAdded(int start, int end, E changedBean) {
        if (muted) return;
        ManagerDataEvent<E> e = new ManagerDataEvent<E>(this, ManagerDataEvent.INTERVAL_ADDED, start, end, changedBean);
        for (ManagerDataListener<E> m : listeners) {
            m.intervalAdded(e);
        }
    }
    protected void fireDataListenersRemoved(int start, int end, E changedBean) {
        if (muted) return;
        ManagerDataEvent<E> e = new ManagerDataEvent<E>(this, ManagerDataEvent.INTERVAL_REMOVED, start, end, changedBean);
        for (ManagerDataListener<E> m : listeners) {
            m.intervalRemoved(e);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(AbstractManager.class);

}

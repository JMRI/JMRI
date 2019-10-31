package jmri.jmrit.logix;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.ResourceBundle;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import jmri.InstanceManager;
import jmri.Manager;
import jmri.NamedBean;
import jmri.beans.PropertyChangeProvider;
import jmri.beans.VetoableChangeProvider;
import jmri.jmrix.SystemConnectionMemo;
import jmri.managers.AbstractManager;
import jmri.util.NamedBeanComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic implementation of a PortalManager.
 * <p>
 * Note that this does not enforce any particular system naming convention.
 * <p>
 * Note this is an 'after thought' manager. Portals have been in use since 2009.
 * Their use has now expanded well beyond what was expected. A Portal factory is
 * needed for development to continue.
 *
 * Portal system names will be numbers and they will not be shown to users. The
 * UI will treat Portal names as it does now as user names.
 *
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author Pete Cressman Copyright (C) 2014
 */
public class PortalManager implements PropertyChangeProvider, VetoableChangeProvider,
        PropertyChangeListener, VetoableChangeListener, jmri.InstanceManagerAutoDefault {

    public PortalManager() {
    }

    public int getXMLOrder() {
        return jmri.Manager.OBLOCKS;
    }

    public char typeLetter() {
        return 'P';
    }

    /*
     * Method to create a new Portal. Returns null if a
     * Portal with the same userName already exists. 
     */
    public Portal createNewPortal(String userName) {
        // Check that Portal does not already exist
        Portal portal;
        if (userName != null && userName.trim().length() > 0) {
            portal = getByUserName(userName);
            if (portal != null) {
                return null;
            }
        } else {  // must have a user name for backward compatibility
            return null;
        }
        // Portal does not exist, create a new Portal
        portal = new Portal(userName);
        // save in the maps
        register(portal);

        return portal;
    }

    /*
     * Method to create a new Portal. Returns null if a
     * Portal with the same userName already exists. 
     * @deprecated since 4.17.5.
     */
    @Deprecated // 4.17.5
    public Portal createNewPortal(String sName, String userName) {
        return createNewPortal(userName);
    }

    /**
     * Method for a UI to delete a bean.
     * <p>
     * The UI should first request a "CanDelete", this will return a list of
     * locations (and descriptions) where the bean is in use via throwing a
     * VetoException, then if that comes back clear, or the user agrees with the
     * actions, then a "DoDelete" can be called which inform the listeners to
     * delete the bean, then it will be deregistered and disposed of.
     * <p>
     * If a property name of "DoNotDelete" is thrown back in the VetoException
     * then the delete process should be aborted.
     *
     * @param bean     The Portal to be deleted
     * @param property The programmatic name of the request. "CanDelete" will
     *                 enquire with all listeners if the item can be deleted.
     *                 "DoDelete" tells the listener to delete the item.
     * @throws java.beans.PropertyVetoException - If the recipients wishes the
     *                                          delete to be aborted (see
     *                                          above).
     */
    @OverridingMethodsMustInvokeSuper
    public void deleteBean(@Nonnull Portal bean, @Nonnull String property) throws PropertyVetoException {
        try {
            fireVetoableChange(property, bean, null);
        } catch (PropertyVetoException e) {
            throw e;  // don't go on to check for delete.
        }
        if (property.equals("DoDelete")) { // NOI18N
            deregister(bean);
            bean.dispose();
        }
    }

    /**
     * Remember a Portal created outside the manager.
     *
     * @param s the bean
     * @throws DuplicateSystemNameException if a different bean with the same system
     *                                    name is already registered in the
     *                                    manager
     */
    public void register(@Nonnull Portal s) {
        String userName = s.getUserName();

        Portal existingPortal = getBeanByUserName(userName);
        if (existingPortal != null) {
            if (s == existingPortal) {
                log.debug("the named bean is registered twice: {}", userName);
            } else {
                log.error("systemName is already registered: {}", userName);
                throw new NamedBean.DuplicateSystemNameException("systemName is already registered: " + userName);
            }
        }

        // clear caches
        cachedSystemNameArray = null;
        cachedSystemNameList = null;
        cachedNamedBeanList = null;
        
        // save this bean
        _beans.add(s);
        _tuser.put(userName, s);

        // notifications
        int position = getPosition(s);
        fireDataListenersAdded(position, position, s);
        fireIndexedPropertyChange("beans", position, null, s);
        firePropertyChange("length", null, _beans.size());
        // listen for name and state changes to forward
        s.addPropertyChangeListener(this);
    }

    /** {@inheritDoc} */
    @OverridingMethodsMustInvokeSuper
    public void deregister(Portal s) {
        int position = getPosition(s);

        // clear caches
        cachedSystemNameArray = null;
        cachedSystemNameList = null;
        cachedNamedBeanList = null;

        // stop listening for user name changes
        s.removePropertyChangeListener(this);
        
        // remove bean from local storage
        _beans.remove(s);
        String userName = s.getUserName();
        _tuser.remove(userName);
        
        // notifications
        fireDataListenersRemoved(position, position, s);
        fireIndexedPropertyChange("beans", position, s, null);
        firePropertyChange("length", null, _beans.size());
    }

    /**
     * Locate an existing instance based on a user name. Returns null if no
     * instance already exists.
     *
     * @param userName System Name of the required NamedBean
     * @return requested NamedBean object or null if none exists
     */
    public Portal getBeanByUserName(@Nonnull String userName) {
        return _tuser.get(userName);
    }

    // not efficient, but does job for now
    private int getPosition(Portal s) {
        int position = 0;
        for (Portal portal : _beans) {
            if (s == portal) {
                return position;
            }
            position++;
        }
        return -1;
    }

    private final List<ManagerDataListener<Portal>> listeners = new ArrayList<>();

    private boolean muted = false;
    
    /**
     * Temporarily suppress DataListener notifications.
     * <p>
     * This avoids O(N^2) behavior when doing bulk updates, i.e. when loading
     * lots of Beans. Note that this is (1) optional, in the sense that the
     * manager is not required to mute and (2) if present, its' temporary, in
     * the sense that the manager must do a cumulative notification when done.
     *
     * @param muted true if notifications should be suppressed; false otherwise
     */
    public void setDataListenerMute(boolean m) {
        if (muted && !m) {
            // send a total update, as we haven't kept track of specifics
            ManagerDataEvent<Portal> e = new ManagerDataEvent<>(this, ManagerDataEvent.CONTENTS_CHANGED, 0, getObjectCount()-1, null);
            for (ManagerDataListener<Portal> listener : listeners) {
                listener.contentsChanged(e);
            }          
        }
        this.muted = m;
    }

    protected void fireDataListenersAdded(int start, int end, Portal changedBean) {
        if (muted) return;
        ManagerDataEvent<Portal> e = new ManagerDataEvent<>(this, ManagerDataEvent.INTERVAL_ADDED, start, end, changedBean);
        for (ManagerDataListener<Portal> m : listeners) {
            m.intervalAdded(e);
        }
    }
    protected void fireDataListenersRemoved(int start, int end, Portal changedBean) {
        if (muted) return;
        ManagerDataEvent<Portal> e = new ManagerDataEvent<>(this, ManagerDataEvent.INTERVAL_REMOVED, start, end, changedBean);
        for (ManagerDataListener<Portal> m : listeners) {
            m.intervalRemoved(e);
        }
    }

    /**
     * Method to get an existing Portal. First looks up assuming that name is a
     * User Name. If this fails looks up assuming that name is a System Name. If
     * both fail, returns null.
     * @param name  either System name or user name
     * @return Portal, if found
     */
    public Portal getPortal(String name) {
        if (name == null) {
            return null;
        }
        Portal portal = getByUserName(name);
        if (portal != null) {
            if (log.isDebugEnabled()) log.debug("getPortal with User Name \"{}\"", name);
            return portal;
        }
        if (name.length() > 2 && name.startsWith("IP")) {
            portal = getBySystemName(name);
            if (portal != null) {
                if (log.isDebugEnabled()) log.debug("getPortal with System Name \"{}\"", name);
                return portal;
            }
        }
        return null;
    }

    /**
     * @deprecated since 4.17.5.
     */
    @Deprecated // 4.17.5
    public Portal getBySystemName(String name) {
        return null;
    }

    public Portal getByUserName(String key) {
        if (key == null || key.trim().length() == 0) {
            return null;
        }
        return _tuser.get(key);
    }

    public Portal providePortal(String name) {
        if (name == null || name.trim().length() == 0) {
            return null;
        }
        Portal portal = getPortal(name);
        if (portal == null) {
            portal = createNewPortal(name);
        }
        return portal;
    }

    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    /** {@inheritDoc} */
    @OverridingMethodsMustInvokeSuper
    @Override
    public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    /** {@inheritDoc} */
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
            for (Portal nb : _beans) {
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
        }
    }

    public String getBeanTypeHandled() {
        return Bundle.getMessage("BeanNamePortal");
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
                Portal t = (Portal) e.getSource();
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

    /**
     * This provides an
     * {@linkplain java.util.Collections#unmodifiableSet unmodifiable} SortedSet
     * of Portals in user-name order.
     * <p>
     * Note: This is the fastest of the accessors, and is the only long-term
     * form.
     * <p>
     * Note: This is a live set; the contents are kept up to date
     *
     * @return Unmodifiable access to a SortedSet of Portals
     */
    @Nonnull
    public SortedSet<Portal> getNamedBeanSet() {
        return Collections.unmodifiableSortedSet(_beans);
    }

    /** {@inheritDoc} */
    @CheckReturnValue
    public int getObjectCount() {
        return _beans.size();
    }

    /**
     * This provides an
     * {@linkplain java.util.Collections#unmodifiableList unmodifiable} List of
     * system names.
     * <p>
     * Note: this is ordered by the underlying NamedBeans, not on the Strings
     * themselves.
     * <p>
     * Note: Access via {@link #getNamedBeanSet()} is faster.
     * <p>
     * Note: This is not a live list; the contents don't stay up to date
     *
     * @return Unmodifiable access to a list of system names
     * @deprecated 4.17.5 - use direct access via {@link #getNamedBeanSet()}
     */
    @Nonnull
    @Deprecated // 4.17.5
    public List<String> getSystemNameList() {
        // jmri.util.Log4JUtil.deprecationWarning(log, "getSystemNameList");
        if (cachedSystemNameList == null) {
            cachedSystemNameList = new ArrayList<>();
            for (Portal b : _beans) {
                cachedSystemNameList.add(b.getSystemName());
            }
        }
        return Collections.unmodifiableList(cachedSystemNameList);
    }

    public String getBeanTypeHandled(boolean plural) {
        return Bundle.getMessage(plural ? "BeanNamePortals" : "BeanNamePortal");
    }




    /**
     * Intended to be equivalent to {@link javax.swing.event.ListDataListener}
     * without introducing a Swing dependency into core JMRI.
     *
     * @param <Portal> the type to support listening for
     * @since JMRI 4.11.4
     */
    interface ManagerDataListener<Portal> {

        /**
         * Sent when the contents of the list has changed in a way that's too
         * complex to characterize with the previous methods.
         *
         * @param e encapsulates event information
         */
        void contentsChanged(ManagerDataEvent<Portal> e);

        /**
         * Sent after the indices in the index0,index1 interval have been
         * inserted in the data model.
         *
         * @param e encapsulates the event information
         */
        void intervalAdded(ManagerDataEvent<Portal> e);

        /**
         * Sent after the indices in the index0,index1 interval have been
         * removed from the data model.
         *
         * @param e encapsulates the event information
         */
        void intervalRemoved(ManagerDataEvent<Portal> e);
    }

    /**
     * Defines an event that encapsulates changes to a list.
     * <p>
     * Intended to be equivalent to {@link javax.swing.event.ListDataEvent}
     * without introducing a Swing dependency into core JMRI.
     *
     * @param <Portal> the type to support in the event
     * @since JMRI 4.11.4
     */
    @javax.annotation.concurrent.Immutable
    public final class ManagerDataEvent<Portal> extends java.util.EventObject {

        /**
         * Equal to {@link javax.swing.event.ListDataEvent#CONTENTS_CHANGED}
         */
        final static public int CONTENTS_CHANGED = 0;
        /**
         * Equal to {@link javax.swing.event.ListDataEvent#INTERVAL_ADDED}
         */
        final static public int INTERVAL_ADDED = 1;
        /**
         * Equal to {@link javax.swing.event.ListDataEvent#INTERVAL_REMOVED}
         */
        final static public int INTERVAL_REMOVED = 2;

        final private int type;
        final private int index0;
        final private int index1;
        final private Portal changedBean; // used when just one bean is added or removed as an efficiency measure
        final private PortalManager source;

        /**
         * Creates a <code>ListDataEvent</code> object.
         *
         * @param source      the source of the event (<code>null</code> not
         *                    permitted).
         * @param type        the type of the event (should be one of
         *                    {@link #CONTENTS_CHANGED}, {@link #INTERVAL_ADDED}
         *                    or {@link #INTERVAL_REMOVED}, although this is not
         *                    enforced).
         * @param index0      the index for one end of the modified range of
         *                    list elements.
         * @param index1      the index for the other end of the modified range
         *                    of list elements.
         * @param changedBean used when just one bean is added or removed,
         *                    otherwise null
         */
        public ManagerDataEvent(@Nonnull PortalManager source, int type, int index0, int index1, Portal changedBean) {
            super(source);
            this.source = source;
            this.type = type;
            this.index0 = Math.min(index0, index1);  // from javax.swing.event.ListDataEvent implementation
            this.index1 = Math.max(index0, index1);  // from javax.swing.event.ListDataEvent implementation
            this.changedBean = changedBean;
        }

        /**
         * Returns the source of the event in a type-safe manner.
         *
         * @return the event source
         */
        @Override
        public PortalManager getSource() {
            return source;
        }

        /**
         * Returns the index of the first item in the range of modified list
         * items.
         *
         * @return The index of the first item in the range of modified list
         *         items.
         */
        public int getIndex0() {
            return index0;
        }

        /**
         * Returns the index of the last item in the range of modified list
         * items.
         *
         * @return The index of the last item in the range of modified list
         *         items.
         */
        public int getIndex1() {
            return index1;
        }

        /**
         * Returns the changed portal or null
         *
         * @return null if more than one bean was changed
         */
        public Portal getChangedBean() {
            return changedBean;
        }

        /**
         * Returns a code representing the type of this event, which is usually
         * one of {@link #CONTENTS_CHANGED}, {@link #INTERVAL_ADDED} or
         * {@link #INTERVAL_REMOVED}.
         *
         * @return The event type.
         */
        public int getType() {
            return type;
        }

        /**
         * Returns a string representing the state of this event.
         *
         * @return A string.
         */
        @Override
        public String toString() {
            return getClass().getName() + "[type=" + type + ",index0=" + index0 + ",index1=" + index1 + "]";
        }
    }





    protected TreeSet<Portal> _beans = new TreeSet<>();
    protected Hashtable<String, Portal> _tuser = new Hashtable<>();  // stores known Portal instances by user name

    // caches
    private String[] cachedSystemNameArray = null;
    private ArrayList<String> cachedSystemNameList = null;
    private ArrayList<Portal> cachedNamedBeanList = null;

    private final static Logger log = LoggerFactory.getLogger(PortalManager.class);

}



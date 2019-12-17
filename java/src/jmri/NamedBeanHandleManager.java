package jmri;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.CheckForNull;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.managers.AbstractManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Instance for controlling the issuing of NamedBeanHandles.
 * <hr>
 * The NamedBeanHandleManager, deals with controlling and updating {@link NamedBean} objects
 * across JMRI. When a piece of code requires persistent access to a bean, it
 * should use a {@link NamedBeanHandle}. The {@link NamedBeanHandle} stores not only the bean
 * that has been requested but also the named that was used to request it
 * (either User or System Name).
 * <p>
 * This Manager will only issue out one {@link NamedBeanHandle} per Bean/Name request.
 * The Manager also deals with updates and changes to the names of {@link NamedBean} objects, along
 * with moving usernames between different beans.
 * <p>
 * If a beans username is changed by the user, then the name will be updated in
 * the NamedBeanHandle. If a username is moved from one bean to another, then
 * the bean reference will be updated and the propertyChangeListener attached to
 * that bean will also be moved, so long as the correct method of adding the
 * listener has been used.
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
 * @see jmri.NamedBean
 * @see jmri.NamedBeanHandle
 *
 * @author Kevin Dickerson Copyright (C) 2011
 */
public class NamedBeanHandleManager extends AbstractManager<NamedBean> implements InstanceManagerAutoDefault {

    public NamedBeanHandleManager() {
        // use Internal memo as connection for this manager
        super(InstanceManager.getDefault(InternalSystemConnectionMemo.class));
    }

    @SuppressWarnings("unchecked") // namedBeanHandles contains multiple types of NameBeanHandles<T>
    @Nonnull
    @CheckReturnValue
    public <T extends NamedBean> NamedBeanHandle<T> getNamedBeanHandle(@Nonnull String name, @Nonnull T bean) {
        Objects.requireNonNull(bean, "bean must be nonnull");
        Objects.requireNonNull(name, "name must be nonnull");
        if (name.isEmpty()) {
            throw new IllegalArgumentException("name cannot be empty in getNamedBeanHandle");
        }
        NamedBeanHandle<T> temp = new NamedBeanHandle<>(name, bean);
        for (NamedBeanHandle<T> h : namedBeanHandles) {
            if (temp.equals(h)) {
                return h;
            }
        }
        namedBeanHandles.add(temp);
        return temp;
    }

    /**
     * Update the name of a bean in its references.
     * <p>
     * <strong>Note</strong> this does not change the name on the bean, it only
     * changes the references.
     *
     * @param <T>     the type of the bean
     * @param oldName the name changing from
     * @param newName the name changing to
     * @param bean    the bean being renamed
     */
    @SuppressWarnings("unchecked") // namedBeanHandles contains multiple types of NameBeanHandles<T>
    public <T extends NamedBean> void renameBean(@Nonnull String oldName, @Nonnull String newName, @Nonnull T bean) {

        /*Gather a list of the beans in the system with the oldName ref.
         Although when we get a new bean we always return the first one that exists,
         when a rename is performed it doesn't delete the bean with the old name;
         it simply updates the name to the new one. So hence you can end up with
         multiple named bean entries for one name.
         */
        NamedBeanHandle<T> oldBean = new NamedBeanHandle<>(oldName, bean);
        for (NamedBeanHandle<T> h : namedBeanHandles) {
            if (oldBean.equals(h)) {
                h.setName(newName);
            }
        }
        updateListenerRef(oldName, newName, bean);
    }

    /**
     * Effectively move a name from one bean to another.
     * <p>
     * <strong>Note</strong> only updates the references to point to the new
     * bean; does not move the name provided from one bean to another.
     *
     * @param <T>     the bean type
     * @param oldBean bean loosing the name
     * @param name    name being moved
     * @param newBean bean gaining the name
     */
    //Checks are performed to make sure that the beans are the same type before being moved
    @SuppressWarnings("unchecked") // namedBeanHandles contains multiple types of NameBeanHandles<T>
    public <T extends NamedBean> void moveBean(@Nonnull T oldBean, @Nonnull T newBean, @Nonnull String name) {
        /*Gather a list of the beans in the system with the oldBean ref.
         Although when a new bean is requested, we always return the first one that exists
         when a move is performed it doesn't delete the namedbeanhandle with the oldBean
         it simply updates the bean to the new one. So hence you can end up with
         multiple bean entries with the same name.
         */

        NamedBeanHandle<T> oldNamedBean = new NamedBeanHandle<>(name, oldBean);
        for (NamedBeanHandle<T> h : namedBeanHandles) {
            if (oldNamedBean.equals(h)) {
                h.setBean(newBean);
            }
        }
        moveListener(oldBean, newBean, name);
    }

    public void updateBeanFromUserToSystem(@Nonnull NamedBean bean) {
        String systemName = bean.getSystemName();
        String userName = bean.getUserName();
        if (userName == null) {
            log.warn("updateBeanFromUserToSystem requires non-blank user name: \"{}\" not renamed", systemName);
            return;
        }
        renameBean(userName, systemName, bean);
    }

    public void updateBeanFromSystemToUser(@Nonnull NamedBean bean) throws JmriException {
        String userName = bean.getUserName();
        String systemName = bean.getSystemName();

        if ((userName == null) || (userName.equals(""))) {
            log.error("UserName is empty, can not update items to use UserName");
            throw new JmriException("UserName is empty, can not update items to use UserName");
        }
        renameBean(systemName, userName, bean);
    }

    @CheckReturnValue
    public <T extends NamedBean> boolean inUse(@Nonnull String name, @Nonnull T bean) {
        NamedBeanHandle<T> temp = new NamedBeanHandle<>(name, bean);
        return namedBeanHandles.stream().anyMatch((h) -> (temp.equals(h)));
    }

    @CheckForNull
    @CheckReturnValue
    public <T extends NamedBean> NamedBeanHandle<T> newNamedBeanHandle(@Nonnull String name, @Nonnull T bean, @Nonnull Class<T> type) {
        return getNamedBeanHandle(name, bean);
    }

    /**
     * A method to update the listener reference from oldName to a newName
     */
    private void updateListenerRef(@Nonnull String oldName, @Nonnull String newName, @Nonnull NamedBean nBean) {
        java.beans.PropertyChangeListener[] listeners = nBean.getPropertyChangeListenersByReference(oldName);
        for (java.beans.PropertyChangeListener listener : listeners) {
            nBean.updateListenerRef(listener, newName);
        }
    }

    /**
     * Moves a propertyChangeListener from one bean to another, where the
     * listener reference matches the currentName.
     */
    private void moveListener(@Nonnull NamedBean oldBean, @Nonnull NamedBean newBean, @Nonnull String currentName) {
        java.beans.PropertyChangeListener[] listeners = oldBean.getPropertyChangeListenersByReference(currentName);
        for (java.beans.PropertyChangeListener l : listeners) {
            String listenerRef = oldBean.getListenerRef(l);
            oldBean.removePropertyChangeListener(l);
            newBean.addPropertyChangeListener(l, currentName, listenerRef);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    @SuppressWarnings("rawtypes") // namedBeanHandles contains multiple types of NameBeanHandles<T>
    ArrayList<NamedBeanHandle> namedBeanHandles = new ArrayList<>();

    /**
     * Don't want to store this information
     */
    @Override
    protected void registerSelf() {
    }

    @Override
    @CheckReturnValue
    public char typeLetter() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    @Nonnull
    @CheckReturnValue
    public String makeSystemName(@Nonnull String s) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    @Deprecated  // will be removed when superclass method is removed due to @Override
    public String[] getSystemNameArray() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    @CheckReturnValue
    @Deprecated  // will be removed when superclass method is removed due to @Override
    public List<String> getSystemNameList() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);

    @Override
    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    @Override
    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    @Override
    protected void firePropertyChange(String p, Object old, Object n) {
        pcs.firePropertyChange(p, old, n);
    }

    @Override
    public void register(@Nonnull NamedBean n) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void deregister(NamedBean n) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    @CheckReturnValue
    public int getXMLOrder() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    @Nonnull
    @CheckReturnValue
    public String getBeanTypeHandled(boolean plural) {
        return Bundle.getMessage(plural ? "BeanNames" : "BeanName");
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Class<NamedBean> getNamedBeanClass() {
        return NamedBean.class;
    }

    private final static Logger log = LoggerFactory.getLogger(NamedBeanHandleManager.class);

}

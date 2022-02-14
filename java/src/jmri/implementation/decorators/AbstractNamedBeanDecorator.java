package jmri.implementation.decorators;

import jmri.NamedBean;
import jmri.beans.BeanUtil;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Abstract base for the NamedBean Decorators.
 * <p>
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Paul Bender Copyright (C) 2020
 */
public abstract class AbstractNamedBeanDecorator implements NamedBean {

    private NamedBean decorated;

    protected AbstractNamedBeanDecorator(NamedBean decorated){
        this.decorated = decorated;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    final public String getComment() {
        return decorated.getComment();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    final public void setComment(String comment) {
        decorated.setComment(comment);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @CheckReturnValue
    @Nonnull
    final public String getDisplayName() {
        return decorated.getDisplayName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @CheckReturnValue
    @Nonnull
    final public String getDisplayName(DisplayOptions displayOptions) {
        return decorated.getDisplayName(displayOptions);
    }

    // implementing classes will typically have a function/listener to get
    // updates from the layout, which will then call
    //  public void firePropertyChange(String propertyName,
    //             Object oldValue,
    //      Object newValue)
    // _once_ if anything has changed state
    // since we can't do a "super(this)" in the ctor to inherit from PropertyChangeSupport, we'll
    // reflect to it
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    protected final HashMap<PropertyChangeListener, String> register = new HashMap<>();
    protected final HashMap<PropertyChangeListener, String> listenerRefs = new HashMap<>();

    @Override
    @OverridingMethodsMustInvokeSuper
    public synchronized void addPropertyChangeListener(@Nonnull PropertyChangeListener l,
                                                       String beanRef, String listenerRef) {
        pcs.addPropertyChangeListener(l);
        if (beanRef != null) {
            register.put(l, beanRef);
        }
        if (listenerRef != null) {
            listenerRefs.put(l, listenerRef);
        }
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public synchronized void addPropertyChangeListener(@Nonnull String propertyName,
                                                       @Nonnull PropertyChangeListener l, String beanRef, String listenerRef) {
        pcs.addPropertyChangeListener(propertyName, l);
        if (beanRef != null) {
            register.put(l, beanRef);
        }
        if (listenerRef != null) {
            listenerRefs.put(l, listenerRef);
        }
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public synchronized void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
        if (listener != null && !BeanUtil.contains(pcs.getPropertyChangeListeners(), listener)) {
            register.remove(listener);
            listenerRefs.remove(listener);
        }
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public synchronized void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(propertyName, listener);
        if (listener != null && !BeanUtil.contains(pcs.getPropertyChangeListeners(), listener)) {
            register.remove(listener);
            listenerRefs.remove(listener);
        }
    }

    @Override
    @Nonnull
    public synchronized PropertyChangeListener[] getPropertyChangeListenersByReference(@Nonnull String name) {
        ArrayList<PropertyChangeListener> list = new ArrayList<>();
        register.entrySet().forEach((entry) -> {
            PropertyChangeListener l = entry.getKey();
            if (entry.getValue().equals(name)) {
                list.add(l);
            }
        });
        return list.toArray(new PropertyChangeListener[list.size()]);
    }

    /**
     * Get a meaningful list of places where the bean is in use.
     *
     * @return ArrayList of the listeners
     */
    @Override
    public synchronized ArrayList<String> getListenerRefs() {
        return new ArrayList<>(listenerRefs.values());
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public synchronized void updateListenerRef(PropertyChangeListener l, String newName) {
        if (listenerRefs.containsKey(l)) {
            listenerRefs.put(l, newName);
        }
    }

    @Override
    public synchronized String getListenerRef(PropertyChangeListener l) {
        return listenerRefs.get(l);
    }

    /**
     * Get the number of current listeners.
     *
     * @return -1 if the information is not available for some reason.
     */
    @Override
    public synchronized int getNumPropertyChangeListeners() {
        return pcs.getPropertyChangeListeners().length;
    }

    @Override
    @Nonnull
    public synchronized PropertyChangeListener[] getPropertyChangeListeners() {
        return pcs.getPropertyChangeListeners();
    }

    @Override
    @Nonnull
    public synchronized PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
        return pcs.getPropertyChangeListeners(propertyName);
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    final public String getSystemName() {
        return decorated.getSystemName();
    }

    /** {@inheritDoc}
    */
    @Nonnull
    @Override
    final public String toString() {
        return decorated.getSystemName();
    }

    @Override
    final public String getUserName() {
        return decorated.getUserName();
    }

    @Nonnull
    @Override
    public String getBeanType() {
        return decorated.getBeanType();
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void setUserName(String s) throws BadUserNameException {
        decorated.setUserName(s);
    }

    @OverridingMethodsMustInvokeSuper
    protected void firePropertyChange(String p, Object old, Object n) {
        pcs.firePropertyChange(p, old, n);
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void dispose() {
        PropertyChangeListener[] listeners = pcs.getPropertyChangeListeners();
        for (PropertyChangeListener l : listeners) {
            pcs.removePropertyChangeListener(l);
            register.remove(l);
            listenerRefs.remove(l);
        }
    }

    @Override
    @Nonnull
    public String describeState(int state) {
        return decorated.describeState(state);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @OverridingMethodsMustInvokeSuper
    public void setProperty(@Nonnull String key,Object value){
        decorated.setProperty(key,value);
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public Object getProperty(@Nonnull String key) {
        return decorated.getProperty(key);
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    @Nonnull
    public Set<String> getPropertyKeys() {
        return decorated.getPropertyKeys();
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void removeProperty(String key) {
        decorated.removeProperty(key);
    }

    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        decorated.vetoableChange(evt);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation tests that
     * {@link NamedBean#getSystemName()}
     * is equal for this and obj.
     *
     * @param obj the reference object with which to compare.
     * @return {@code true} if this object is the same as the obj argument;
     *         {@code false} otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;  // for efficiency
        if (obj == null) return false; // by contract

        if (obj instanceof AbstractNamedBeanDecorator) {  // NamedBeans are not equal to things of other types
            AbstractNamedBeanDecorator b = (AbstractNamedBeanDecorator) obj;
            return this.getSystemName().equals(b.getSystemName());
        }

        if(obj.equals(this.decorated)){
            // this isn't the same object, but it is decorating the object
            return true;
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.getSystemName().hashCode();
    }
    
    /**
     * {@inheritDoc} 
     */
    @CheckReturnValue
    @Override
    public int compareSystemNameSuffix(@Nonnull String suffix1, @Nonnull String suffix2, @Nonnull NamedBean n) {
        return decorated.compareSystemNameSuffix(suffix1,suffix2,n);
    }

}

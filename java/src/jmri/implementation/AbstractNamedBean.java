package jmri.implementation;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.CheckForNull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import jmri.NamedBean;
import jmri.beans.BeanUtil;

/**
 * Abstract base for the NamedBean interface.
 * <p>
 * Implements the parameter binding support.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public abstract class AbstractNamedBean implements NamedBean {

    // force changes through setUserName() to ensure rules are applied
    // as a side effect require reads through getUserName()
    private String mUserName;
    // final so does not need to be private to protect against changes
    protected final String mSystemName;

    /**
     * Create a new NamedBean instance using only a system name.
     *
     * @param sys the system name for this bean; must not be null and must
     *            be unique within the layout
     */
    protected AbstractNamedBean(@Nonnull String sys) {
        this(sys, null);
    }

    /**
     * Create a new NamedBean instance using both a system name and
     * (optionally) a user name.
     * <p>
     * Refuses construction if unable to use the normalized user name, to prevent
     * subclass from overriding {@link #setUserName(java.lang.String)} during construction.
     *
     * @param sys  the system name for this bean; must not be null
     * @param user the user name for this bean; will be normalized if needed; can be null
     * @throws jmri.NamedBean.BadUserNameException   if the user name cannot be
     *                                               normalized
     * @throws jmri.NamedBean.BadSystemNameException if the system name is null
     */
    protected AbstractNamedBean(@Nonnull String sys, @CheckForNull String user)
            throws NamedBean.BadUserNameException, NamedBean.BadSystemNameException {

        if (Objects.isNull(sys)) {
            throw new NamedBean.BadSystemNameException();
        }
        mSystemName = sys;
        // normalize the user name or refuse construction if unable to
        // use this form, to prevent subclass from overriding {@link #setUserName()}
        // during construction
        AbstractNamedBean.this.setUserName(user);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getComment() {
        return this.comment;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setComment(String comment) {
        String old = this.comment;
        if (comment == null || comment.trim().isEmpty()) {
            this.comment = null;
        } else {
            this.comment = comment;
        }
        firePropertyChange(PROPERTY_COMMENT, old, comment);
    }
    private String comment;

    /**
     * {@inheritDoc}
     */
    @Override
    @CheckReturnValue
    @Nonnull
    public final String getDisplayName() {
        return NamedBean.super.getDisplayName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @CheckReturnValue
    @Nonnull
    public final String getDisplayName(DisplayOptions displayOptions) {
        return NamedBean.super.getDisplayName(displayOptions);
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

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @Override
    @OverridingMethodsMustInvokeSuper
    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    /** {@inheritDoc} */
    @Override
    @OverridingMethodsMustInvokeSuper
    public synchronized void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    /** {@inheritDoc} */
    @Override
    @OverridingMethodsMustInvokeSuper
    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
        if (listener != null && !BeanUtil.contains(pcs.getPropertyChangeListeners(), listener)) {
            register.remove(listener);
            listenerRefs.remove(listener);
        }
    }

    /** {@inheritDoc} */
    @Override
    @OverridingMethodsMustInvokeSuper
    public synchronized void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(propertyName, listener);
        if (listener != null && !BeanUtil.contains(pcs.getPropertyChangeListeners(), listener)) {
            register.remove(listener);
            listenerRefs.remove(listener);
        }
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public synchronized PropertyChangeListener[] getPropertyChangeListeners() {
        return pcs.getPropertyChangeListeners();
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public synchronized PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
        return pcs.getPropertyChangeListeners(propertyName);
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public final String getSystemName() {
        return mSystemName;
    }

    /** {@inheritDoc}
    */
    @Nonnull
    @Override
    public final String toString() {
        /*
         * Implementation note:  This method is final to ensure that the
         * contract for toString is properly implemented.  See the
         * comment in NamedBean#toString() for more info.
         * If a subclass wants to add extra info at the end of the
         * toString output, extend {@link #toStringSuffix}.
         */
        return getSystemName()+toStringSuffix();
    }

    /**
     * Overload this in a sub-class to add extra info to the results of toString()
     * @return a suffix to add at the end of #toString() result
     */
    protected String toStringSuffix() {
        return "";
    }

    /** {@inheritDoc} */
    @Override
    public final String getUserName() {
        return mUserName;
    }

    /** {@inheritDoc} */
    @Override
    @OverridingMethodsMustInvokeSuper
    public void setUserName(String s) throws NamedBean.BadUserNameException {
        String old = mUserName;
        mUserName = NamedBean.normalizeUserName(s);
        firePropertyChange(PROPERTY_USERNAME, old, mUserName);
    }

    @OverridingMethodsMustInvokeSuper
    protected void firePropertyChange(String p, Object old, Object n) {
        pcs.firePropertyChange(p, old, n);
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public String describeState(int state) {
        switch (state) {
            case UNKNOWN:
                return Bundle.getMessage("BeanStateUnknown");
            case INCONSISTENT:
                return Bundle.getMessage("BeanStateInconsistent");
            default:
                return Bundle.getMessage("BeanStateUnexpected", state);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @OverridingMethodsMustInvokeSuper
    public void setProperty(@Nonnull String key, Object value) {
        if (parameters == null) {
            parameters = new HashMap<>();
        }
        Set<String> keySet = getPropertyKeys();
        if (keySet.contains(key)) {
            // key already in the map, replace the value.
            Object oldValue = getProperty(key);
            if (!Objects.equals(oldValue, value)) {
                removeProperty(key); // make sure the old value is removed.
                parameters.put(key, value);
                firePropertyChange(key, oldValue, value);
            }
        } else {
            parameters.put(key, value);
            firePropertyChange(key, null, value);
        }
    }

    /** {@inheritDoc} */
    @Override
    @OverridingMethodsMustInvokeSuper
    public Object getProperty(@Nonnull String key) {
        if (parameters == null) {
            parameters = new HashMap<>();
        }
        return parameters.get(key);
    }

    /** {@inheritDoc} */
    @Override
    @OverridingMethodsMustInvokeSuper
    @Nonnull
    public java.util.Set<String> getPropertyKeys() {
        if (parameters == null) {
            parameters = new HashMap<>();
        }
        return parameters.keySet();
    }

    /** {@inheritDoc} */
    @Override
    @OverridingMethodsMustInvokeSuper
    public void removeProperty(String key) {
        if (parameters == null || Objects.isNull(key)) {
            return;
        }
        parameters.remove(key);
    }

    private HashMap<String, Object> parameters = null;

    /** {@inheritDoc} */
    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation tests that
     * {@link jmri.NamedBean#getSystemName()}
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

        if (obj instanceof AbstractNamedBean) {  // NamedBeans are not equal to things of other types
            AbstractNamedBean b = (AbstractNamedBean) obj;
            return this.getSystemName().equals(b.getSystemName());
        }
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @return hash code value is based on the system name.
     */
    @Override
    public int hashCode() {
        return getSystemName().hashCode(); // as the
    }

    /**
     * {@inheritDoc}
     *
     * By default, does an alphanumeric-by-chunks comparison.
     */
    @CheckReturnValue
    @Override
    public int compareSystemNameSuffix(@Nonnull String suffix1, @Nonnull String suffix2, @Nonnull NamedBean n) {
        jmri.util.AlphanumComparator ac = new jmri.util.AlphanumComparator();
        return ac.compare(suffix1, suffix2);
    }

}

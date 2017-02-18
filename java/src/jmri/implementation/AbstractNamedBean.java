package jmri.implementation;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javax.annotation.CheckReturnValue;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import jmri.NamedBean;

/**
 * Abstract base for the NamedBean interface.
 * <P>
 * Implements the parameter binding support.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public abstract class AbstractNamedBean implements NamedBean, java.io.Serializable {

    protected AbstractNamedBean(String sys) {
        mSystemName = sys;
        mUserName = null;
    }

    protected AbstractNamedBean(String sys, String user) {
        this(sys);
        mUserName = user;
    }

    /**
     * Get associated comment text.
     */
    @Override
    public String getComment() {
        return this.comment;
    }

    /**
     * Set associated comment text.
     * <p>
     * Comments can be any valid text.
     *
     * @param comment Null means no comment associated.
     */
    @Override
    public void setComment(String comment) {
        String old = this.comment;
        if (comment == null || comment.isEmpty() || comment.trim().length() < 1 ) {
            this.comment = null;
        } else {
            this.comment = comment;
        }
        firePropertyChange("Comment", old, comment);
    }
    private String comment;

    @Override
    public String getDisplayName() {
        String name = getUserName();
        if (name != null && name.length() > 0) {
            return name;
        } else {
            return getSystemName();
        }
    }

    @Override
    public String getFullyFormattedDisplayName() {
        String name = getUserName();
        if (name != null && name.length() > 0 && !name.equals(getSystemName())) {
            name = getSystemName() + "(" + name + ")";
        } else {
            name = getSystemName();
        }
        return name;
    }

    // implementing classes will typically have a function/listener to get
    // updates from the layout, which will then call
    //		public void firePropertyChange(String propertyName,
    //					       	Object oldValue,
    //						Object newValue)
    // _once_ if anything has changed state
    // since we can't do a "super(this)" in the ctor to inherit from PropertyChangeSupport, we'll
    // reflect to it
    final java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);
    final Hashtable<PropertyChangeListener, String> register = new Hashtable<>();
    final Hashtable<PropertyChangeListener, String> listenerRefs = new Hashtable<>();

    @Override
    public synchronized void addPropertyChangeListener(PropertyChangeListener l, String beanRef, String listenerRef) {
        pcs.addPropertyChangeListener(l);
        if (beanRef != null) {
            register.put(l, beanRef);
        }
        if (listenerRef != null) {
            listenerRefs.put(l, listenerRef);
        }
    }

    @Override
    public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    @Override
    public synchronized void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
        if (l != null) {
            register.remove(l);
            listenerRefs.remove(l);
        }
    }

    @Override
    public synchronized PropertyChangeListener[] getPropertyChangeListenersByReference(String name) {
        ArrayList<PropertyChangeListener> list = new ArrayList<>();
        Enumeration<PropertyChangeListener> en = register.keys();
        while (en.hasMoreElements()) {
            PropertyChangeListener l = en.nextElement();
            if (register.get(l).equals(name)) {
                list.add(l);
            }
        }
        return list.toArray(new PropertyChangeListener[list.size()]);
    }

    /* This allows a meaning full list of places where the bean is in use!*/
    @Override
    public synchronized ArrayList<String> getListenerRefs() {
        ArrayList<String> list = new ArrayList<String>();
        Enumeration<PropertyChangeListener> en = listenerRefs.keys();
        while (en.hasMoreElements()) {
            PropertyChangeListener l = en.nextElement();
            list.add(listenerRefs.get(l));
        }
        return list;
    }

    @Override
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
     * Number of current listeners. May return -1 if the information is not
     * available for some reason.
     */
    @Override
    public synchronized int getNumPropertyChangeListeners() {
        return pcs.getPropertyChangeListeners().length;
    }

    public synchronized PropertyChangeListener[] getPropertyChangeListeners() {
        return pcs.getPropertyChangeListeners();
    }

    @Override
    public String getSystemName() {
        return mSystemName;
    }

    @Override
    public String getUserName() {
        return mUserName;
    }

    @Override
    public void setUserName(String s) {
        String old = mUserName;
        mUserName = s;
        firePropertyChange("UserName", old, s);
    }

    @SuppressFBWarnings(value = "IS2_INCONSISTENT_SYNC",
            justification = "Sync of mUserName protected by ctor invocation")
    protected String mUserName;

    protected String mSystemName;

    protected void firePropertyChange(String p, Object old, Object n) {
        pcs.firePropertyChange(p, old, n);
    }

    @Override
    public void dispose() {
        PropertyChangeListener[] listeners = pcs.getPropertyChangeListeners();
        for (PropertyChangeListener l : listeners) {
            pcs.removePropertyChangeListener(l);
            register.remove(l);
            listenerRefs.remove(l);
       }
    }

    @Override
    @CheckReturnValue
    public String describeState(int state) {
        switch (state) {
            case UNKNOWN: return Bundle.getMessage("BeanStateUnknown");
            case INCONSISTENT: return Bundle.getMessage("BeanStateInconsistent");
            default: return Bundle.getMessage("BeanStateUnexpected", state);
        }
    }

    @Override
    public void setProperty(String key, Object value) {
        if (parameters == null) {
            parameters = new HashMap<String, Object>();
        }
        parameters.put(key, value);
    }

    @Override
    public Object getProperty(String key) {
        if (parameters == null) {
            parameters = new HashMap<String, Object>();
        }
        return parameters.get(key);
    }

    @Override
    public java.util.Set<String> getPropertyKeys() {
        if (parameters == null) {
            parameters = new HashMap<String, Object>();
        }
        return parameters.keySet();
    }

    @Override
    public void removeProperty(String key) {
        if (parameters == null || key == null) {
            return;
        }
        parameters.remove(key);
    }

    HashMap<String, Object> parameters = null;

    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
    }
}

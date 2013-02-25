// AbstractNamedBean.java

package jmri.implementation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.*;
import java.util.ArrayList;
import java.util.Enumeration;

import java.util.HashMap;
import java.util.Hashtable;

/**
 * Abstract base for the NamedBean interface.
 * <P>
 * Implements the parameter binding support.
 *
 * @author      Bob Jacobsen Copyright (C) 2001
 * @version     $Revision$
 */
public abstract class AbstractNamedBean implements NamedBean, java.io.Serializable {

    //private AbstractNamedBean() {
    //    mSystemName = null;
    //    mUserName = null;
    //    log.warn("Unexpected use of null ctor");
    //    Exception e = new Exception();
    //    e.printStackTrace();
    //}

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
    public String getComment() { return this.comment; }
    
    /**
     * Set associated comment text.
     * <p>
     * Comments can be any valid text.
     * @param comment Null means no comment associated.
     */
    public void setComment(String comment) {
        String old = this.comment;
        this.comment = comment;
        firePropertyChange("Comment", old, comment);
    }
    private String comment;

    public String getDisplayName() {
        String name = getUserName();
        if (name != null && name.length() > 0) {
            return name;
        } else {
            return getSystemName();
        }
    }

    // implementing classes will typically have a function/listener to get
    // updates from the layout, which will then call
    //		public void firePropertyChange(String propertyName,
    //					       	Object oldValue,
    //						Object newValue)
    // _once_ if anything has changed state

    // since we can't do a "super(this)" in the ctor to inherit from PropertyChangeSupport, we'll
    // reflect to it
    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);
    Hashtable<java.beans.PropertyChangeListener, String> register = new Hashtable<java.beans.PropertyChangeListener, String>();
    Hashtable<java.beans.PropertyChangeListener, String> listenerRefs = new Hashtable<java.beans.PropertyChangeListener, String>();
    
    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l, String beanRef, String listenerRef) {
        pcs.addPropertyChangeListener(l);
        if(beanRef!=null)
            register.put(l, beanRef);
        if(listenerRef!=null)
            listenerRefs.put(l, listenerRef);
    }
    
    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    
    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
        register.remove(l);
        listenerRefs.remove(l);
    }
    
    public synchronized ArrayList<java.beans.PropertyChangeListener> getPropertyChangeListeners(String name) {
        ArrayList<java.beans.PropertyChangeListener> list = new ArrayList<java.beans.PropertyChangeListener>();
        Enumeration<java.beans.PropertyChangeListener> en = register.keys();
        while (en.hasMoreElements()) {
            java.beans.PropertyChangeListener l = en.nextElement();
            if(register.get(l).equals(name)){
                list.add(l);
            }
        }
        return list;
    }
    
    /* This allows a meaning full list of places where the bean is in use!*/
    public synchronized ArrayList<String> getListenerRefs() {
        ArrayList<String> list = new ArrayList<String>();
        Enumeration<java.beans.PropertyChangeListener> en = listenerRefs.keys();
        while (en.hasMoreElements()) {
            java.beans.PropertyChangeListener l = en.nextElement();
            list.add(listenerRefs.get(l));
        }
        return list;
    }
    
    public synchronized void updateListenerRef(java.beans.PropertyChangeListener l, String newName){
        if(listenerRefs.containsKey(l)){
            listenerRefs.put(l, newName);
        }
    }
    
    public synchronized String getListenerRef(java.beans.PropertyChangeListener l) {
        return listenerRefs.get(l);
    }
    
    /**
     * Number of current listeners. May return -1 if the 
     * information is not available for some reason.
     */
    public synchronized int getNumPropertyChangeListeners() {
        return pcs.getPropertyChangeListeners().length;
    }

    public synchronized java.beans.PropertyChangeListener[] getPropertyChangeListeners() {
        return pcs.getPropertyChangeListeners();
    }

    public String getSystemName() {return mSystemName;}
    public String getUserName() {return mUserName;}
    public void   setUserName(String s) {
        String old = mUserName;
        mUserName = s;
        firePropertyChange("UserName", old, s);
    }

    protected String mUserName;
    protected String mSystemName;

    protected void firePropertyChange(String p, Object old, Object n) { 
        if (pcs!=null) pcs.firePropertyChange(p,old,n);
    }

    public void dispose() {
        pcs = null;
    }

    public void setProperty(Object key, Object value) {
        if (parameters == null) 
            parameters = new HashMap<Object, Object>();
        parameters.put(key, value);
    }
    
    public Object getProperty(Object key) {
        if (parameters == null) return null;
        return parameters.get(key);
    }

    public java.util.Set<Object> getPropertyKeys() {
        if (parameters == null) return null;
        return parameters.keySet();
    }

    HashMap<Object, Object> parameters = null;
    
    static Logger log = LoggerFactory.getLogger(AbstractNamedBean.class.getName());
}

/* @(#)AbstractNamedBean.java */

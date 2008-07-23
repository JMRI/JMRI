// AbstractNamedBean.java

package jmri;

/**
 * Abstract base for the NamedBean interface.
 * <P>
 * Implements the parameter binding support.
 *
 * @author      Bob Jacobsen Copyright (C) 2001
 * @version     $Revision: 1.4 $
 */
public abstract class AbstractNamedBean implements NamedBean, java.io.Serializable {

    private AbstractNamedBean() {
        mSystemName = null;
        mUserName = null;
        log.warn("Unexpected use of null ctor");
        Exception e = new Exception();
        e.printStackTrace();
    }

    public AbstractNamedBean(String sys) {
        mSystemName = sys.toUpperCase();
        mUserName = null;
    }
    public AbstractNamedBean(String sys, String user) {
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

    // implementing classes will typically have a function/listener to get
    // updates from the layout, which will then call
    //		public void firePropertyChange(String propertyName,
    //					       	Object oldValue,
    //						Object newValue)
    // _once_ if anything has changed state

    // since we can't do a "super(this)" in the ctor to inherit from PropertyChangeSupport, we'll
    // reflect to it
    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);
    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    /**
     * Number of current listeners. May return -1 if the 
     * information is not available for some reason.
     */
    public synchronized int getNumPropertyChangeListeners() {
        return pcs.getPropertyChangeListeners().length;
    }

    public String getSystemName() {return mSystemName;}
    public String getUserName() {return mUserName;}
    public void   setUserName(String s) {
        String old = mUserName;
        mUserName = s;
        firePropertyChange("UserName", old, s);
    }

    private String mUserName;
    private String mSystemName;

    protected void firePropertyChange(String p, Object old, Object n) { pcs.firePropertyChange(p,old,n);}

    public void dispose() {
        pcs = null;
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(AbstractNamedBean.class.getName());
}

/* @(#)AbstractNamedBean.java */

// NamedBean.java

package jmri;

/**
 * The NamedBean interface provides common services for classes representing objects
 * on the layout, and allows the of common code by their Managers.
 * <P>
 * Each object has a two names.  The "user" name is entirely free form, and
 * can be used for any purpose.  The "system" name is provided by the system-specific
 * implementations, and provides a unique mapping to the layout control system
 * (e.g. LocoNet, NCE, etc) and address within that system.
 *
 * @author	Bob Jacobsen  Copyright (C) 2001
 * @version	$Revision: 1.2 $
 * @see         jmri.AbstractManager
 * @see         jmri.AbstractNamedBean
 */
public interface NamedBean {

    // user identification, _bound_ parameter so manager(s) can listen
    public String getUserName();
    public void setUserName(String s);
    
    /**
     * Get a system-specific name.  This encodes the hardware addressing
     * information.
     */
    public String getSystemName();
    
    /**
     * Request a call-back when a bound property changes.
     * Bound properties are the known state, commanded state, user and system names.
     */
    public void addPropertyChangeListener(java.beans.PropertyChangeListener l);
    
    /**
     * Remove a request for a call-back when a bound property changes.
     */
    public void removePropertyChangeListener(java.beans.PropertyChangeListener l);
    
    /**
     * Remove references to and from this object, so that it can
     * eventually be garbage-collected.
     */
    public void dispose();  // remove _all_ connections!
    
}

/* @(#)NamedBean.java */

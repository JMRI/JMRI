// SystemConnectionMemo.java

package jmri.jmrix;

/**
 * Lightweight abstract class to denote that a system is active,
 * and provide general information.
 * <p>
 * Objects of specific subtypes are registered
 * in the instance manager to activate their
 * particular system.
 *
 * @author		Bob Jacobsen  Copyright (C) 2010
 * @version             $Revision: 1.1 $
 */
abstract public class SystemConnectionMemo {

    protected SystemConnectionMemo(String prefix, String userName) {
        this.prefix = prefix;
        this.userName = userName;
    }
    
    /**
     * Store in InstanceManager with 
     * proper ID for later retrieval as a 
     * generic system
     */
    public void register() {
        jmri.InstanceManager.store(this, SystemConnectionMemo.class);
    }
    
    /**
     * Provides access to the system prefix string.
     * This was previously called the "System letter"
     */
    public String getSystemPrefix() { return prefix; }
    private String prefix;
    
    /**
     * Provides access to the system user name string.
     * This was previously fixed at configuration time.
     */
    public String getUserName() { return userName; }
    private String userName;
    
    /**
     * Provide the system menu
     */
     public javax.swing.JMenu getMenu() { return null; }
    
}


/* @(#)SystemConnectionMemo.java */

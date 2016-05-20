// SystemConnectionMemo.java

package jmri.jmrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;

import java.util.ResourceBundle;
import java.util.Enumeration;

/**
 * Lightweight abstract class to denote that a system is active,
 * and provide general information.
 * <p>
 * Objects of specific subtypes are registered
 * in the instance manager to activate their
 * particular system.
 *
 * @author		Bob Jacobsen  Copyright (C) 2010
 * @version             $Revision$
 */
abstract public class SystemConnectionMemo {

    protected SystemConnectionMemo(String prefix, String userName) {
        initialise();
        if(!setSystemPrefix(prefix)){
            for (int x = 2; x<50; x++){
                if(setSystemPrefix(prefix+x)){
                    break;
                }
            }
        }

        if(!setUserName(userName)){
            for (int x = 2; x<50; x++){
                if(setUserName(userName+x)){
                    break;
                }
            }
        }
        addToActionList();
    }
    
    private static boolean initialised = false;
    /**
     * Provides a method to reserve System Names and prefixes at creation
     */
    private static void initialise(){
        if (!initialised){
            addUserName("Internal");
            addSystemPrefix("I");
            initialised = true;
        }
    }
    
    final protected static ArrayList<String> userNames = new ArrayList<String>();
    final protected static ArrayList<String> sysPrefixes = new ArrayList<String>();
    
    private synchronized static boolean addUserName(String userName){      
        if (userNames.contains(userName))
            return false;

        userNames.add(userName);
        return true;
    }
    
    //This should probably throwing an exception
    private synchronized static boolean addSystemPrefix(String systemPrefix){
        if (sysPrefixes.contains(systemPrefix))
            return false;
        sysPrefixes.add(systemPrefix);
        return true;
    }
    
    private synchronized static void removeUserName(String userName){
        if(userNames!=null){
            if (userNames.contains(userName)){
                int index = userNames.indexOf(userName);
                userNames.remove(index);
            }
        }
    }
    
    private synchronized static void removeSystemPrefix(String systemPrefix){
        if(sysPrefixes!=null){
            if (sysPrefixes.contains(systemPrefix)){
                int index = sysPrefixes.indexOf(systemPrefix);
                sysPrefixes.remove(index);
            }
        }
    }

    /**
     * Store in InstanceManager with 
     * proper ID for later retrieval as a 
     * generic system
     */
    public void register() {
        jmri.InstanceManager.store(this, SystemConnectionMemo.class);
        notifyPropertyChangeListener("ConnectionAdded", null, null);
    }
    
    /**
     * Provides access to the system prefix string.
     * This was previously called the "System letter"
     */
    public String getSystemPrefix() { return prefix; }
    private String prefix;
    //This should probably throwing an exception
    public boolean setSystemPrefix(String systemPrefix) {
        if (systemPrefix.equals(prefix)) {
            return true;
        }
        String oldPrefix = prefix;
        if(addSystemPrefix(systemPrefix)){
            prefix = systemPrefix;
            removeSystemPrefix(oldPrefix);
            notifyPropertyChangeListener("ConnectionPrefixChanged", oldPrefix, systemPrefix);
            return true;
        }
        return false;
    }
    
    /**
     * Provides access to the system user name string.
     * This was previously fixed at configuration time.
     */
    public String getUserName() { return userName; }
    private String userName;
    //This should probably throwing an exception
    public boolean setUserName(String name) {
        if (name.equals(userName))
            return true;
        String oldUserName = this.userName;
        if(addUserName(name)){
            this.userName = name;
            removeUserName(oldUserName);
            notifyPropertyChangeListener("ConnectionNameChanged", oldUserName, name);
            return true;
        }
        return false;
    }
    
    /** 
     * Does this connection provide a manager of this type?
     */
    public boolean provides(Class<?> c) {
        return false; // nothing, by default
    }
    
    /** 
     * Does this connection provide a manager of this type?
     */
    public <T> T get(Class<?> T) {
        return null; // nothing, by default
    }
    
    public void dispose(){
        removeFromActionList();
        removeUserName(userName);
        removeSystemPrefix(prefix);
        jmri.InstanceManager.deregister(this, SystemConnectionMemo.class);
        notifyPropertyChangeListener("ConnectionRemoved", userName, null);
    }
    
    private boolean mDisabled = false;
    public boolean getDisabled() { return mDisabled; }
    public void setDisabled(boolean disabled) { 
        if(disabled==mDisabled)
            return;
        boolean oldDisabled = mDisabled;
        mDisabled = disabled;
        notifyPropertyChangeListener("ConnectionDisabled", oldDisabled, disabled);
    }
    
    public static void removePropertyChangeListener(PropertyChangeListener l) {
        if (listeners.contains(l)) {
            listeners.removeElement(l);
        }
    }

    public static void addPropertyChangeListener(PropertyChangeListener l) {
        // add only if not already registered
        if (!listeners.contains(l)) {
            listeners.addElement(l);
        }
    }

    /**
     * Trigger the notification of all PropertyChangeListeners
     */
    @SuppressWarnings("unchecked")
	protected void notifyPropertyChangeListener(String property, Object oldValue, Object newValue) {
        // make a copy of the listener vector to synchronized not needed for transmit
        Vector<PropertyChangeListener> v;
        synchronized(this)
            {
                v = (Vector<PropertyChangeListener>) listeners.clone();
            }
        // forward to all listeners
        int cnt = v.size();
        for (int i=0; i < cnt; i++) {
            PropertyChangeListener client = v.elementAt(i);
            client.propertyChange(new PropertyChangeEvent(this, property, oldValue, newValue));
        }
    }
    
    abstract protected ResourceBundle getActionModelResourceBundle();
    
    protected void addToActionList(){
        apps.CreateButtonModel bm = jmri.InstanceManager.getDefault(apps.CreateButtonModel.class);
        ResourceBundle rb = getActionModelResourceBundle();
        if (rb==null || bm==null)
            return;
        Enumeration<String> e = rb.getKeys();
        while (e.hasMoreElements()) {
            String key = e.nextElement();
            try {
                bm.addAction(key, rb.getString(key));
            } catch (ClassNotFoundException ex) {
                log.error("Did not find class "+key);
            }
        }
    }
    
    protected void removeFromActionList(){
        apps.CreateButtonModel bm = jmri.InstanceManager.getDefault(apps.CreateButtonModel.class);
         ResourceBundle rb = getActionModelResourceBundle();
        if (rb==null || bm==null)
            return;
        Enumeration<String> e = rb.getKeys();
        while (e.hasMoreElements()) {
            String key = e.nextElement();
            try {
                bm.removeAction(key);
            } catch (ClassNotFoundException ex) {
                log.error("Did not find class "+key);
            }
        }
    }

    // data members to hold contact with the property listeners
    final private static Vector<PropertyChangeListener> listeners = new Vector<PropertyChangeListener>();
    
    static Logger log = LoggerFactory.getLogger(SystemConnectionMemo.class.getName());
}


/* @(#)SystemConnectionMemo.java */

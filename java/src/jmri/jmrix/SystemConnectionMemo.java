package jmri.jmrix;

import apps.startup.StartupActionModelUtil;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lightweight abstract class to denote that a system is active, and provide
 * general information.
 * <p>
 * Objects of specific subtypes are registered in the instance manager to
 * activate their particular system.
 *
 * @author	Bob Jacobsen Copyright (C) 2010
 */
abstract public class SystemConnectionMemo {

    private boolean disabled = false;
    private Boolean disabledAsLoaded = null; // Boolean can be true, false, or null
    private String prefix;
    private String prefixAsLoaded;
    private String userName;
    private String userNameAsLoaded;

    protected SystemConnectionMemo(String prefix, String userName) {
        log.debug("SystemConnectionMemo created for prefix \"{}\" user name \"{}\"", prefix, userName);
        initialise();
        if (!setSystemPrefix(prefix)) {
            for (int x = 2; x < 50; x++) {
                if (setSystemPrefix(prefix + x)) {
                    break;
                }
            }
        }

        if (!setUserName(userName)) {
            for (int x = 2; x < 50; x++) {
                if (setUserName(userName + x)) {
                    break;
                }
            }
        }
        addToActionList();
        // reset to null so these get set by the first setPrefix/setUserName
        // call after construction
        this.prefixAsLoaded = null;
        this.userNameAsLoaded = null;
    }

    private static boolean initialised = false;

    /**
     * Provides a method to reserve System Names and prefixes at creation
     */
    private static void initialise() {
        if (!initialised) {
//             addUserName("Internal");
//             addSystemPrefix("I");
//             initialised = true;
        }
    }

    /**
     * For use in testing, undo any initialization that's been done.
     */
    public static void reset() {
        userNames = new ArrayList<>();
        sysPrefixes = new ArrayList<>();
        listeners = new HashSet<>();
        
        initialised = false;
    }
    
    protected static ArrayList<String> userNames = new ArrayList<>();
    protected static ArrayList<String> sysPrefixes = new ArrayList<>();

    private synchronized static boolean addUserName(String userName) {
        if (userNames.contains(userName)) {
            return false;
        }

        userNames.add(userName);
        return true;
    }

    //This should probably throwing an exception
    private synchronized static boolean addSystemPrefix(String systemPrefix) {
        if (sysPrefixes.contains(systemPrefix)) {
            return false;
        }
        sysPrefixes.add(systemPrefix);
        return true;
    }

    private synchronized static void removeUserName(String userName) {
        if (userNames != null) {
            if (userNames.contains(userName)) {
                int index = userNames.indexOf(userName);
                userNames.remove(index);
            }
        }
    }

    private synchronized static void removeSystemPrefix(String systemPrefix) {
        if (sysPrefixes != null) {
            if (sysPrefixes.contains(systemPrefix)) {
                int index = sysPrefixes.indexOf(systemPrefix);
                sysPrefixes.remove(index);
            }
        }
    }

    /**
     * Store in InstanceManager with proper ID for later retrieval as a generic
     * system
     */
    public void register() {
        jmri.InstanceManager.store(this, SystemConnectionMemo.class);
        notifyPropertyChangeListener("ConnectionAdded", null, null);
    }

    /**
     * Provides access to the system prefix string. This was previously called
     * the "System letter"
     *
     * @return System prefix
     */
    public String getSystemPrefix() {
        return prefix;
    }

    /**
     * Set the system prefix.
     *
     * @param systemPrefix
     * @throws java.lang.NullPointerException if systemPrefix is null
     * @return true if the system prefix could be set
     */
    public boolean setSystemPrefix(@Nonnull String systemPrefix) {
        if (systemPrefix == null) {
            throw new NullPointerException();
        }
        if (systemPrefix.equals(prefix)) {
            if (this.prefixAsLoaded == null) {
                this.prefixAsLoaded = systemPrefix;
            }
            return true;
        }
        String oldPrefix = prefix;
        if (addSystemPrefix(systemPrefix)) {
            prefix = systemPrefix;
            if (this.prefixAsLoaded == null) {
                this.prefixAsLoaded = systemPrefix;
            }
            removeSystemPrefix(oldPrefix);
            notifyPropertyChangeListener("ConnectionPrefixChanged", oldPrefix, systemPrefix);
            return true;
        }
        log.debug("setSystemPrefix false for \"{}\"", systemPrefix);
        return false;
    }

    /**
     * Provides access to the system user name string. This was previously fixed
     * at configuration time.
     *
     * @return User name
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Set the user name for the system connection.
     *
     * @param name
     * @throws java.lang.NullPointerException if name is null
     * @return true if the user name could be set.
     */
    public boolean setUserName(@Nonnull String name) {
        if (name == null) {
            throw new NullPointerException();
        }
        if (name.equals(userName)) {
            if (this.userNameAsLoaded == null) {
                this.userNameAsLoaded = name;
            }
            return true;
        }
        String oldUserName = this.userName;
        if (addUserName(name)) {
            this.userName = name;
            if (this.userNameAsLoaded == null) {
                this.userNameAsLoaded = name;
            }
            removeUserName(oldUserName);
            notifyPropertyChangeListener("ConnectionNameChanged", oldUserName, name);
            return true;
        }
        return false;
    }

    /**
     * Does this connection provide a manager of this type?
     *
     * @param c The class type for the manager to be provided
     * @return true if the specified manager is provided
     */
    public boolean provides(Class<?> c) {
        return false; // nothing, by default
    }

    /**
     * Does this connection provide a manager of this type?
     *
     * @param <T> Type of manager to get
     * @param T   Type of manager to get
     * @return The manager or null
     */
    public <T> T get(Class<?> T) {
        return null; // nothing, by default
    }

    public void dispose() {
        removeFromActionList();
        removeUserName(userName);
        removeSystemPrefix(prefix);
        jmri.InstanceManager.deregister(this, SystemConnectionMemo.class);
        notifyPropertyChangeListener("ConnectionRemoved", userName, null);
    }

    public boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        if (this.disabledAsLoaded == null) {
            // only set first time
            this.disabledAsLoaded = disabled;
        }
        if (disabled == this.disabled) {
            return;
        }
        boolean oldDisabled = this.disabled;
        this.disabled = disabled;
        notifyPropertyChangeListener("ConnectionDisabled", oldDisabled, disabled);
    }

    public static void removePropertyChangeListener(PropertyChangeListener l) {
        if (listeners.contains(l)) {
            listeners.remove(l);
        }
    }

    public static void addPropertyChangeListener(PropertyChangeListener l) {
        // add only if not already registered
        if (!listeners.contains(l)) {
            listeners.add(l);
        }
    }

    /**
     * Trigger the notification of all PropertyChangeListeners
     *
     * @param property The property name
     * @param oldValue The property's old value
     * @param newValue The property's new value
     */
    protected void notifyPropertyChangeListener(String property, Object oldValue, Object newValue) {
        // make a copy of the listener vector to synchronized not needed for transmit
        Set<PropertyChangeListener> v;
        synchronized (this) {
            v = new HashSet<>(listeners);
        }
        // forward to all listeners
        for (PropertyChangeListener listener : v) {
            listener.propertyChange(new PropertyChangeEvent(this, property, oldValue, newValue));
        }
    }

    abstract protected ResourceBundle getActionModelResourceBundle();

    protected void addToActionList() {
        StartupActionModelUtil util = jmri.InstanceManager.getDefault(StartupActionModelUtil.class);
        ResourceBundle rb = getActionModelResourceBundle();
        if (rb == null || util == null) {
            return;
        }
        Enumeration<String> e = rb.getKeys();
        while (e.hasMoreElements()) {
            String key = e.nextElement();
            try {
                util.addAction(key, rb.getString(key));
            } catch (ClassNotFoundException ex) {
                log.error("Did not find class \"{}\"", key);
            }
        }
    }

    protected void removeFromActionList() {
        StartupActionModelUtil util = jmri.InstanceManager.getDefault(StartupActionModelUtil.class);
        ResourceBundle rb = getActionModelResourceBundle();
        if (rb == null || util == null) {
            return;
        }
        Enumeration<String> e = rb.getKeys();
        while (e.hasMoreElements()) {
            String key = e.nextElement();
            try {
                util.removeAction(key);
            } catch (ClassNotFoundException ex) {
                log.error("Did not find class \"{}\"", key);
            }
        }
    }

    public boolean isDirty() {
        return ((this.disabledAsLoaded == null || this.disabledAsLoaded != this.disabled)
                || (this.prefixAsLoaded == null || !this.prefixAsLoaded.equals(this.prefix))
                || (this.userNameAsLoaded == null || !this.userNameAsLoaded.equals(this.userName)));
    }

    public boolean isRestartRequired() {
        return this.isDirty();
    }

    // data members to hold contact with the property listeners
    private static Set<PropertyChangeListener> listeners = new HashSet<>();

    private final static Logger log = LoggerFactory.getLogger(SystemConnectionMemo.class.getName());
}

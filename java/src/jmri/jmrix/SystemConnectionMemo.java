package jmri.jmrix;

import apps.startup.StartupActionModelUtil;

import java.util.Comparator;
import java.util.Enumeration;
import java.util.Objects;
import java.util.ResourceBundle;
import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import jmri.ConsistManager;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.beans.Bean;
import jmri.implementation.DccConsistManager;
import jmri.implementation.NmraConsistManager;
import jmri.util.NamedBeanComparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lightweight abstract class to denote that a system is active, and provide
 * general information.
 * <p>
 * Objects of specific subtypes of this are registered in the
 * {@link InstanceManager} to activate their particular system.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 */
public abstract class SystemConnectionMemo extends Bean {

    public static final String DISABLED = "ConnectionDisabled";
    public static final String USER_NAME = "ConnectionNameChanged";
    public static final String SYSTEM_PREFIX = "ConnectionPrefixChanged";
    private boolean disabled = false;
    private Boolean disabledAsLoaded = null; // Boolean can be true, false, or null
    private String prefix;
    private String prefixAsLoaded;
    private String userName;
    private String userNameAsLoaded;

    @SuppressWarnings("deprecation")
    protected SystemConnectionMemo(@Nonnull String prefix, @Nonnull String userName) {
        if (this instanceof ConflictingSystemConnectionMemo) {
            this.prefix = prefix;
            this.userName = userName;
            return;
        }
        log.debug("SystemConnectionMemo created for prefix \"{}\" user name \"{}\"", prefix, userName);
        if (!setSystemPrefix(prefix)) {
            int x = 2;
            while (!setSystemPrefix(prefix + x)) {
                x++;
            }
            log.debug("created system prefix {}{}", prefix, x);
        }

        if (!setUserName(userName)) {
            int x = 2;
            while (!setUserName(userName + x)) {
                x++;
            }
            log.debug("created user name {}{}", prefix, x);
        }
        addToActionList();
        // reset to null so these get set by the first setPrefix/setUserName
        // call after construction
        this.prefixAsLoaded = null;
        this.userNameAsLoaded = null;
    }

    /**
     * Store in InstanceManager with proper ID for later retrieval as a generic
     * system.
     */
    public void register() {
        log.debug("register as SystemConnectionMemo, really of type {}", this.getClass());
        SystemConnectionMemoManager.getDefault().register(this);
    }

    /**
     * Provide access to the system prefix string.
     * <p>
     * This was previously called the "System letter".
     *
     * @return System prefix
     */
    public String getSystemPrefix() {
        return prefix;
    }

    /**
     * Set the system prefix.
     *
     * @param systemPrefix prefix to use for this system connection
     * @throws java.lang.NullPointerException if systemPrefix is null
     * @return true if the system prefix could be set
     */
    public final boolean setSystemPrefix(@Nonnull String systemPrefix) {
        Objects.requireNonNull(systemPrefix);
        // return true if systemPrefix is not being changed
        if (systemPrefix.equals(prefix)) {
            if (this.prefixAsLoaded == null) {
                this.prefixAsLoaded = systemPrefix;
            }
            return true;
        }
        String oldPrefix = prefix;
        if (SystemConnectionMemoManager.getDefault().isSystemPrefixAvailable(systemPrefix)) {
            prefix = systemPrefix;
            if (this.prefixAsLoaded == null) {
                this.prefixAsLoaded = systemPrefix;
            }
            this.propertyChangeSupport.firePropertyChange(SYSTEM_PREFIX, oldPrefix, systemPrefix);
            return true;
        }
        log.debug("setSystemPrefix false for \"{}\"", systemPrefix);
        return false;
    }

    /**
     * Provide access to the system user name string.
     * <p>
     * This was previously fixed at configuration time.
     *
     * @return User name of the connection
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Set the user name for the system connection.
     *
     * @param userName user name to use for this system connection
     * @throws java.lang.NullPointerException if name is null
     * @return true if the user name could be set.
     */
    public final boolean setUserName(@Nonnull String userName) {
        Objects.requireNonNull(userName);
        if (userName.equals(this.userName)) {
            if (this.userNameAsLoaded == null) {
                this.userNameAsLoaded = userName;
            }
            return true;
        }
        String oldUserName = this.userName;
        if (SystemConnectionMemoManager.getDefault().isUserNameAvailable(userName)) {
            this.userName = userName;
            if (this.userNameAsLoaded == null) {
                this.userNameAsLoaded = userName;
            }
            this.propertyChangeSupport.firePropertyChange(USER_NAME, oldUserName, userName);
            return true;
        }
        return false;
    }

    /**
     * Check if this connection provides a specific manager type. This method
     * <strong>must</strong> return false if a manager for the specific type is
     * not provided, and <strong>must</strong> return true if a manager for the
     * specific type is provided.
     *
     * @param c The class type for the manager to be provided
     * @return true if the specified manager is provided
     * @see #get(java.lang.Class)
     */
    @OverridingMethodsMustInvokeSuper
    public boolean provides(Class<?> c) {
        if (c.equals(jmri.ConsistManager.class)) {
            if (consistManager != null) {
                return true; // we have a consist manager already
            } else if (provides(jmri.CommandStation.class)) {
                return true; // we can construct an NMRAConsistManager
            } else {
                // true if we can construct a DccConsistManager
                return provides(jmri.AddressedProgrammerManager.class);
            }
        } else {
            return false; // nothing, by default
        }
    }

    /**
     * Get a manager for a specific type. This method <strong>must</strong>
     * return a non-null value if {@link #provides(java.lang.Class)} is true for
     * the type, and <strong>must</strong> return null if provides() is false
     * for the type.
     *
     * @param <T>  Type of manager to get
     * @param type Type of manager to get
     * @return The manager or null if provides() is false for T
     * @see #provides(java.lang.Class)
     */
    @OverridingMethodsMustInvokeSuper
    @SuppressWarnings("unchecked") // dynamic checking done on cast of getConsistManager
    public <T> T get(Class<?> type) {
        if (type.equals(ConsistManager.class)) {
            return (T) getConsistManager();
        } else {
            return null; // nothing, by default
        }
    }

    public void dispose() {
        removeFromActionList();
        SystemConnectionMemoManager.getDefault().deregister(this);
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
        this.propertyChangeSupport.firePropertyChange(DISABLED, oldDisabled, disabled);
    }

    /**
     * Get the Comparator to be used for two NamedBeans. This is typically an
     * {@link NamedBeanComparator}, but may be any Comparator that works for
     * this connection type.
     * 
     * @param <B>  the type of NamedBean
     * @param type the class of NamedBean
     * @return the Comparator
     */
    public abstract <B extends NamedBean> Comparator<B> getNamedBeanComparator(Class<B> type);

    protected abstract ResourceBundle getActionModelResourceBundle();

    protected final void addToActionList() {
        changeActionList(true);
    }

    protected final void removeFromActionList() {
        changeActionList(false);
    }

    private void changeActionList(boolean add) {
        StartupActionModelUtil util = StartupActionModelUtil.getDefault();
        ResourceBundle rb = getActionModelResourceBundle();
        if (rb == null) {
            // don't bother trying if there is no ActionModelResourceBundle
            return;
        }
        log.debug("Removing actions from bundle {}", rb.getBaseBundleName());
        rb.keySet().forEach(key -> {
            try {
                if (add) {
                    util.addAction(key, rb.getString(key));
                } else {
                    util.removeAction(key);
                }
            } catch (ClassNotFoundException ex) {
                log.error("Did not find class \"{}\"", key);
            }
        });
    }

    public boolean isDirty() {
        return ((this.disabledAsLoaded == null || this.disabledAsLoaded != this.disabled)
                || (this.prefixAsLoaded == null || !this.prefixAsLoaded.equals(this.prefix))
                || (this.userNameAsLoaded == null || !this.userNameAsLoaded.equals(this.userName)));
    }

    public boolean isRestartRequired() {
        return this.isDirty();
    }

    /**
     * Provide access to the ConsistManager for this particular connection.
     *
     * @return the provided ConsistManager or null if the connection does not
     *         provide a ConsistManager
     */
    public ConsistManager getConsistManager() {
        if (consistManager == null) {
            // a consist manager doesn't exist, so we can create it.
            if (provides(jmri.CommandStation.class)) {
                setConsistManager(new NmraConsistManager(get(jmri.CommandStation.class)));
            } else if (provides(jmri.AddressedProgrammerManager.class)) {
                setConsistManager(new DccConsistManager(get(jmri.AddressedProgrammerManager.class)));
            }
        }
        return consistManager;
    }

    public void setConsistManager(ConsistManager c) {
        consistManager = c;
        jmri.InstanceManager.store(consistManager, ConsistManager.class);
    }

    private ConsistManager consistManager = null;

    private static final Logger log = LoggerFactory.getLogger(SystemConnectionMemo.class);

}

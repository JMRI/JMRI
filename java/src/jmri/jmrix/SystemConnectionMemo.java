package jmri.jmrix;

import apps.startup.StartupActionModelUtil;
import java.util.Enumeration;
import java.util.Objects;
import java.util.ResourceBundle;
import javax.annotation.Nonnull;
import jmri.InstanceManager;
import jmri.beans.Bean;
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
abstract public class SystemConnectionMemo extends Bean {

    public static final String DISABLED = "ConnectionDisabled";
    public static final String USER_NAME = "ConnectionNameChanged";
    public static final String SYSTEM_PREFIX = "ConnectionPrefixChanged";
    private boolean disabled = false;
    private Boolean disabledAsLoaded = null; // Boolean can be true, false, or null
    private String prefix;
    private String prefixAsLoaded;
    private String userName;
    private String userNameAsLoaded;

    protected SystemConnectionMemo(@Nonnull String prefix, @Nonnull String userName) {
        log.debug("SystemConnectionMemo created for prefix \"{}\" user name \"{}\"", prefix, userName);
        if (!setSystemPrefix(prefix)) {
            int x = 2;
            while (!setSystemPrefix(prefix + x)) {
                x++;
            }
            log.debug("created system prefix {}", prefix + x);
        }

        if (!setUserName(userName)) {
            int x = 2;
            while (!setUserName(userName + x)) {
                x++;
            }
            log.debug("created user name {}", prefix + x);
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
     * This was previously fixed at configuration time.
     *
     * @return User name
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

    abstract protected ResourceBundle getActionModelResourceBundle();

    protected final void addToActionList() {
        StartupActionModelUtil util = StartupActionModelUtil.getDefault();
        ResourceBundle rb = getActionModelResourceBundle();
        if (rb == null) {
            // don't bother trying if there is no ActionModelResourceBundle
            return;
        }
        log.debug("Adding actions from bundle {}", rb.getBaseBundleName());
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

    protected final void removeFromActionList() {
        StartupActionModelUtil util = StartupActionModelUtil.getDefault();
        ResourceBundle rb = getActionModelResourceBundle();
        if (rb == null) {
            // don't bother trying if there is no ActionModelResourceBundle
            return;
        }
        log.debug("Removing actions from bundle {}", rb.getBaseBundleName());
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

    private final static Logger log = LoggerFactory.getLogger(SystemConnectionMemo.class);
}

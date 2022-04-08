package jmri.jmrix;

import java.util.*;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;

import jmri.*;
import jmri.SystemConnectionMemo;
import jmri.beans.Bean;
import jmri.implementation.DccConsistManager;
import jmri.implementation.NmraConsistManager;
import jmri.util.NamedBeanComparator;

import jmri.util.startup.StartupActionFactory;

/**
 * Lightweight abstract class to denote that a system is active, and provide
 * general information.
 * <p>
 * Objects of specific subtypes of this are registered in the
 * {@link InstanceManager} to activate their particular system.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 */
public abstract class DefaultSystemConnectionMemo extends Bean implements SystemConnectionMemo, Disposable {

    private boolean disabled = false;
    private Boolean disabledAsLoaded = null; // Boolean can be true, false, or null
    private String prefix;
    private String prefixAsLoaded;
    private String userName;
    private String userNameAsLoaded;
    protected Map<Class<?>,Object> classObjectMap;

    protected DefaultSystemConnectionMemo(@Nonnull String prefix, @Nonnull String userName) {
        classObjectMap = new HashMap<>();
        if (this instanceof CaptiveSystemConnectionMemo) {
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
        // reset to null so these get set by the first setPrefix/setUserName
        // call after construction
        this.prefixAsLoaded = null;
        this.userNameAsLoaded = null;
    }

    /**
     * Register with the SystemConnectionMemoManager and InstanceManager with proper
     * ID for later retrieval as a generic system.
     * <p>
     * This operation should occur only when the SystemConnectionMemo is ready for use.
     */
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
    public boolean provides(Class<?> c) {
        if (disabled) {
            return false;
        }
        if (c.equals(jmri.ConsistManager.class)) {
            return classObjectMap.get(c) != null || provides(CommandStation.class) || provides(AddressedProgrammerManager.class);
        } else {
            return classObjectMap.containsKey(c);
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
    @Override
    public <T> T get(Class<?> type) {
        if (disabled) {
            return null;
        }
        if (type.equals(ConsistManager.class)) {
            return (T) getConsistManager();
        } else {
            return (T) classObjectMap.get(type); // nothing, by default
        }
    }

    @Override
    public void dispose() {
        Set<Class<?>> keySet = new HashSet<>(classObjectMap.keySet());
        keySet.forEach(this::removeRegisteredObject);
        SystemConnectionMemoManager.getDefault().deregister(this);
    }

    private <T> void removeRegisteredObject(Class<T> c) {
        T object = get(c);
        if (object != null) {
            InstanceManager.deregister(object, c);
            deregister(object, c);
            disposeIfPossible(c, object);
        }
    }

    private <T> void disposeIfPossible(Class<T> c, T object) {
        if(object instanceof Disposable) {
            try {
                ((Disposable)object).dispose();
            } catch (Exception e) {
                log.warn("Exception while disposing object of type {} in memo of type {}.", c.getName(), this.getClass().getName(), e);
            }
        }
    }

    /**
     * Get if the System Connection is currently Disabled.
     *
     * @return true if Disabled, else false.
     */
    @Override
    public boolean getDisabled() {
        return disabled;
    }

    /**
     * Set if the System Connection is currently Disabled.
     * <p>
     * disabledAsLoaded is only set once.
     * Sends PropertyChange on change of disabled status.
     *
     * @param disabled true to disable, false to enable.
     */
    @Override
    public void setDisabled(boolean disabled) {
        if (this.disabledAsLoaded == null) {
            // only set first time
            this.disabledAsLoaded = disabled;
        }
        if (disabled != this.disabled) {
            boolean oldDisabled = this.disabled;
            this.disabled = disabled;
            this.propertyChangeSupport.firePropertyChange(DISABLED, oldDisabled, disabled);
        }
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
    @Override
    public abstract <B extends NamedBean> Comparator<B> getNamedBeanComparator(Class<B> type);

    /**
     * Provide a factory for getting startup actions.
     * <p>
     * This is a bound, read-only, property under the name "actionFactory".
     *
     * @return the factory
     */
    @Nonnull
    @Override
    public StartupActionFactory getActionFactory() {
        return new ResourceBundleStartupActionFactory(getActionModelResourceBundle());
    }

    protected abstract ResourceBundle getActionModelResourceBundle();

    /**
     * Get if connection is dirty.
     * Checked fields are disabled, prefix, userName
     *
     * @return true if changed since loaded
     */
    @Override
    public boolean isDirty() {
        return ((this.disabledAsLoaded == null || this.disabledAsLoaded != this.disabled)
                || (this.prefixAsLoaded == null || !this.prefixAsLoaded.equals(this.prefix))
                || (this.userNameAsLoaded == null || !this.userNameAsLoaded.equals(this.userName)));
    }

    @Override
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
        return (ConsistManager) classObjectMap.computeIfAbsent(ConsistManager.class,(Class<?> c) -> { return generateDefaultConsistManagerForConnection(); });
    }

    private ConsistManager generateDefaultConsistManagerForConnection(){
        if (provides(jmri.CommandStation.class)) {
            return new NmraConsistManager(get(jmri.CommandStation.class));
        } else if (provides(jmri.AddressedProgrammerManager.class)) {
            return new DccConsistManager(get(jmri.AddressedProgrammerManager.class));
        }
        return null;
    }

    public void setConsistManager(ConsistManager c) {
        store(c, ConsistManager.class);
        jmri.InstanceManager.store(c, ConsistManager.class);
    }

    public <T> void store(@Nonnull T item, @Nonnull Class<T> type){
        classObjectMap.put(type,item);
    }

    public <T> void deregister(@Nonnull T item, @Nonnull Class<T> type){
        classObjectMap.remove(type,item);
    }

    /**
     * Duration in milliseconds of interval between separate Turnout commands on the same connection.
     * <p>
     * Change from e.g. connection config dialog and scripts using {@link #setOutputInterval(int)}
     */
    private int _interval = getDefaultOutputInterval();

    /**
     * Default interval 250ms.
     * {@inheritDoc}
     */
    @Override
    public int getDefaultOutputInterval(){
        return 250;
    }

    /**
     * Get the connection specific OutputInterval (in ms) to wait between/before commands
     * are sent, configured in AdapterConfig.
     * Used in {@link jmri.implementation.AbstractTurnout#setCommandedStateAtInterval(int)}.
     */
    @Override
    public int getOutputInterval() {
        log.debug("Getting interval {}", _interval);
        return _interval;
    }

    @Override
    public void setOutputInterval(int newInterval) {
        log.debug("Setting interval from {} to {}", _interval, newInterval);
        this.propertyChangeSupport.firePropertyChange(INTERVAL, _interval, newInterval);
        _interval = newInterval;
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultSystemConnectionMemo.class);

}

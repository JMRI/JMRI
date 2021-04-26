package jmri;

import java.util.Comparator;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;

import jmri.util.startup.StartupActionFactory;

/**
 * Lightweight interface denoting that a system is active, and provide
 * general information.
 * <p>
 * Objects of specific subtypes of this are registered in the
 * {@link InstanceManager} to activate their particular system.
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
public interface SystemConnectionMemo extends jmri.beans.PropertyChangeProvider {

    void dispose();

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
    @SuppressWarnings(value = "unchecked")
    <T> T get(Class<?> type);

    public static final String DISABLED = "ConnectionDisabled";
    public static final String USER_NAME = "ConnectionNameChanged";
    public static final String SYSTEM_PREFIX = "ConnectionPrefixChanged";
    public static final String INTERVAL = "OutputInterval";

    /**
     * Provide a factory for getting startup actions.
     * <p>
     * This is a bound, read-only, property under the name "actionFactory".
     *
     * @return the factory
     */
    @Nonnull
    StartupActionFactory getActionFactory();

    /**
     * Get if the System Connection is currently Disabled.
     *
     * @return true if Disabled, else false.
     */
    boolean getDisabled();

    /**
     * Get the Comparator to be used for two NamedBeans. This is typically an
     * {@link jmri.util.NamedBeanComparator}, but may be any Comparator that works for
     * this connection type.
     *
     * @param <B>  the type of NamedBean
     * @param type the class of NamedBean
     * @return the Comparator
     */
    <B extends NamedBean> Comparator<B> getNamedBeanComparator(Class<B> type);

    /**
     * Provide access to the system prefix string.
     * <p>
     * This was previously called the "System letter".
     *
     * @return System prefix
     */
    String getSystemPrefix();

    /**
     * Provide access to the system user name string.
     * <p>
     * This was previously fixed at configuration time.
     *
     * @return User name of the connection
     */
    String getUserName();

    /**
     * Get if connection is dirty. Checked fields are disabled, prefix, userName
     *
     * @return true if changed since loaded
     */
    boolean isDirty();

    boolean isRestartRequired();

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
    boolean provides(Class<?> c);

    /**
     * Store in InstanceManager with proper ID for later retrieval as a generic
     * system.
     */
    void register();

    /**
     * Set if the System Connection is currently Disabled.
     * <p>
     * disabledAsLoaded is only set once. Sends PropertyChange on change of
     * disabled status.
     *
     * @param disabled true to disable, false to enable.
     */
    void setDisabled(boolean disabled);

    /**
     * Set the system prefix.
     *
     * @param systemPrefix prefix to use for this system connection
     * @throws java.lang.NullPointerException if systemPrefix is null
     * @return true if the system prefix could be set
     */
    boolean setSystemPrefix(@Nonnull String systemPrefix);

    /**
     * Set the user name for the system connection.
     *
     * @param userName user name to use for this system connection
     * @throws java.lang.NullPointerException if name is null
     * @return true if the user name could be set
     */
    boolean setUserName(@Nonnull String userName);

    /**
     * Get the connection specific OutputInterval to wait between/before commands
     * are sent, configured in AdapterConfig.
     * Used in {@link jmri.implementation.AbstractTurnout#setCommandedStateAtInterval(int)}.
     *
     * @return the output interval time in ms
     */
    int getOutputInterval();
    
    /**
     * Get the Default connection specific OutputInterval to wait between/before commands
     * are sent.
     * @return the default output interval time in ms.
     */
    int getDefaultOutputInterval();

    void setOutputInterval(int newInterval);

}

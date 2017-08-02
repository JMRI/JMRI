package jmri.util;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import jmri.InstanceManager;
import jmri.jmrix.SystemConnectionMemo;

/**
 * Common utility method for returning the System Connection Name from the
 * System Name Prefix
 *
 * @author Kevin Dickerson Copyright 2010
 */
public class ConnectionNameFromSystemName {

    /**
     * Locates the connected systems name from a given prefix.
     *
     * @param prefix the system prefix
     * @return The Connection System Name or null if no connection has the given
     *         prefix
     */
    @CheckForNull
    static public String getConnectionName(@Nonnull String prefix) {
        SystemConnectionMemo memo = getSystemConnectionMemoFromSystemPrefix(prefix);
        if (memo != null) {
            return memo.getUserName();
        }
        return null;
    }

    /**
     * Locates the connected systems prefix from a given System name.
     *
     * @param name The user name
     * @return The system prefix or null if no connection has the given name
     */
    @CheckForNull
    static public String getPrefixFromName(@Nonnull String name) {
        SystemConnectionMemo memo = getSystemConnectionMemoFromUserName(name);
        if (memo != null) {
            return memo.getSystemPrefix();
        }
        return null;
    }

    /**
     * Get the {@link jmri.jmrix.SystemConnectionMemo} for a given system
     * prefix.
     *
     * @param systemPrefix the system prefix
     * @return the SystemConnectionMemo or null if no memo exists
     */
    @CheckForNull
    static public SystemConnectionMemo getSystemConnectionMemoFromSystemPrefix(@Nonnull String systemPrefix) {
        for (SystemConnectionMemo memo : InstanceManager.getList(SystemConnectionMemo.class)) {
            if (memo.getSystemPrefix().equals(systemPrefix)) {
                return memo;
            }
        }
        return null;
    }

    /**
     * Get the {@link jmri.jmrix.SystemConnectionMemo} for a given user name.
     *
     * @param userName the user name
     * @return the SystemConnectionMemo or null if no memo exists
     */
    @CheckForNull
    static public SystemConnectionMemo getSystemConnectionMemoFromUserName(@Nonnull String userName) {
        for (SystemConnectionMemo memo : InstanceManager.getList(SystemConnectionMemo.class)) {
            if (memo.getUserName().equals(userName)) {
                return memo;
            }
        }
        return null;
    }

}

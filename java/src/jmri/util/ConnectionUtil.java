package jmri.util;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.SystemConnectionMemo;

/**
 * Utility methods for connections.
 *
 * @author Daniel Bergqvist (C) 2022
 */
public class ConnectionUtil {

    /**
     * Find the connection by its name.
     * <P>
     * Example:
     * LocoNetSystemConnectionMemo memo = getConnection("L2", LocoNetSystemConnectionMemo.class);
     *
     * @param <T>                   The type of connection
     * @param systemConnectionName  The connection name
     * @param clazz                 The class of the connection type
     * @return                      The memo if found, null otherwise
     */
    @CheckForNull
    public static <T extends SystemConnectionMemo> T getConnection(
            @Nonnull String systemConnectionName,
            @Nonnull Class<T> clazz) {

        List<T> systemConnections = jmri.InstanceManager.getList(clazz);

        for (T memo : systemConnections) {
            if (memo.getSystemPrefix().equals(systemConnectionName)) {
                return memo;
            }
        }

        // Connection is not found
        return null;
    }
}

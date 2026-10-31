package jmri.jmrit.simulation;

import jmri.InvokeOnAnyThread;
import jmri.InstanceManager;
import jmri.jmrix.ConnectionConfig;
import jmri.jmrix.ConnectionConfigManager;

/**
 * Says whether the current profile is fully simulated: whether every layout
 * connection in it is a simulator. Simulated trains can only be placed in a
 * fully simulated profile, so they can never fake occupancy on a real layout.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.1
 */
public final class SimulationCheck {

    private SimulationCheck() {
        // static methods only
    }

    /**
     * Is the current profile fully simulated? It is when every connection
     * the {@link ConnectionConfigManager} lists says it is a simulator. A
     * profile with no connections has no hardware, so it counts as simulated.
     * With no connection manager the connections can't be known, so the
     * profile does not count as simulated.
     *
     * @return true if every connection is known to be a simulator
     */
    @InvokeOnAnyThread
    public static boolean isFullySimulated() {
        ConnectionConfigManager manager = InstanceManager.getNullableDefault(ConnectionConfigManager.class);
        if (manager == null) {
            return false;
        }
        return isFullySimulated(manager.getConnections());
    }

    /**
     * Are all these connections simulators?
     *
     * @param connections the connections to check
     * @return true if every one is a simulator
     */
    @InvokeOnAnyThread
    public static boolean isFullySimulated(ConnectionConfig[] connections) {
        for (ConnectionConfig c : connections) {
            if (!c.isSimulator()) {
                return false;
            }
        }
        return true;
    }
}

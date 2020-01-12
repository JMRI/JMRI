package jmri.jmrix;

import java.util.Map;
import java.util.HashMap;
import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interface for classes that wish to get notification when the connection to
 * the layout changes.
 * <p>
 * Maintains a single instance, as there is only one set of connections for the
 * running program.
 * <p>
 * The "system name" referred to here is the human-readable name like "LocoNet 2"
 * which can be obtained from i.e. 
 * {@link SystemConnectionMemo#getUserName}. 
 * Not clear whether {@link ConnectionConfig#getConnectionName} is correct.
 * It's not intended to be the prefix from i.e. {@link PortAdapter#getSystemPrefix}.
 * Maybe the right thing is to pass in the SystemConnectionMemo?
 *
 * @author Daniel Boudreau Copyright (C) 2007
 * @author Paul Bender Copyright (C) 2016
 */
public class ConnectionStatus {

    public static final String CONNECTION_UNKNOWN = "Unknown";
    public static final String CONNECTION_UP = "Connected";
    public static final String CONNECTION_DOWN = "Not Connected";

    // hashmap of ConnectionKey objects and their status
    private final HashMap<ConnectionKey, String> portStatus = new HashMap<>();

    /**
     * Record the single instance *
     */
    private static ConnectionStatus _instance = null;

    public static synchronized ConnectionStatus instance() {
        if (_instance == null) {
            log.debug("ConnectionStatus creating instance");
            // create and load
            _instance = new ConnectionStatus();
        }
        // log.debug("ConnectionStatus returns instance {}", _instance);
        return _instance;
    }

    /**
     * Add a connection with a given name and port name to the portStatus set
     * if not yet present in the set.
     *
     * @param systemName human-readable name for system like "LocoNet 2"
     *                   which can be obtained from i.e. {@link SystemConnectionMemo#getUserName}.
     * @param portName   the port name
     */
    public synchronized void addConnection(String systemName, @Nonnull String portName) {
        log.debug("addConnection systemName {} portName {}", systemName, portName);
        ConnectionKey newKey = new ConnectionKey(systemName, portName);
        if (!portStatus.containsKey(newKey)) {
            portStatus.put(newKey, CONNECTION_UNKNOWN);
            firePropertyChange("add", null, portName);
        }
    }

    /**
     * Set the connection state of a communication port.
     *
     * @param systemName human-readable name for system like "LocoNet 2"
     *                      which can be obtained from i.e. {@link SystemConnectionMemo#getUserName}.
     * @param portName   the port name
     * @param state      one of ConnectionStatus.UP, ConnectionStatus.DOWN, or
     *                   ConnectionStatus.UNKNOWN.
     */
    public synchronized void setConnectionState(String systemName, @Nonnull String portName, @Nonnull String state) {
        log.debug("setConnectionState systemName: {} portName: {} state: {}", systemName, portName, state);
        ConnectionKey newKey = new ConnectionKey(systemName, portName);
        if (!portStatus.containsKey(newKey)) {
            portStatus.put(newKey, state);
            firePropertyChange("add", null, portName);
            log.debug("New Connection added: {} ", newKey);
        } else {
            firePropertyChange("change", portStatus.put(newKey, state), portName);
        }
    }

    /**
     * Get the status of a communication port, based on the port name only.
     *
     * @param portName the port name
     * @return status
     * @deprecated since 4.7.1 use
     * {@link #getConnectionState(java.lang.String, java.lang.String)} instead.
     */
    @Deprecated
    public synchronized String getConnectionState(@Nonnull String portName) {
        log.debug("Deprecated getConnectionState portName: {} ", portName);
        ConnectionKey newKey = new ConnectionKey(null, portName);
        if (portStatus.containsKey(newKey)) {
            return getConnectionState(null, portName);
        } else {
            // see if there is a key that has portName as the port value
            for (Map.Entry<ConnectionKey, String> entry : portStatus.entrySet()) {
                if ((entry.getKey().getPortName() != null) && (entry.getKey().getPortName().equals(portName))) {
                    // if we find a match, return it
                    return entry.getValue();
                }
            }
        }
        // If we still don't find a match, then we don't know the status
        log.warn("Didn't find system status for port {}", portName);
        return CONNECTION_UNKNOWN;
    }

    /**
     * Get the status of a communication port with a given name.
     *
     * @param systemName human-readable name for system like "LocoNet 2"
     *                      which can be obtained from i.e. {@link SystemConnectionMemo#getUserName}.
     * @param portName   the port name
     * @return the status
     */
    public synchronized String getConnectionState(String systemName, @Nonnull String portName) {
        log.debug("144 getConnectionState systemName: {} portName: {}", systemName, portName);
        String stateText = CONNECTION_UNKNOWN;
        ConnectionKey newKey = new ConnectionKey(systemName, portName);
        if (portStatus.containsKey(newKey)) {
            stateText = portStatus.get(newKey);
            log.debug("connection found : {}", stateText);
        } else {
            log.debug("connection systemName {} portName {} not found, {}", systemName, portName, stateText);
        }
        return stateText;
    }

    /**
     * Get the status of a communication port, based on the system name only.
     *
     * @param systemName human-readable name for system like "LocoNet 2"
     *                      which can be obtained from i.e. {@link SystemConnectionMemo#getUserName}.
     * @return the status
     */
    public synchronized String getSystemState(@Nonnull String systemName) {
        log.debug("getSystemState systemName: {} ", systemName);
        // see if there is a key that has systemName as the port value.
        for (Map.Entry<ConnectionKey, String> entry : portStatus.entrySet()) {
            if ((entry.getKey().getSystemName() != null) && (entry.getKey().getSystemName().equals(systemName))) {
                // if we find a match, return it
                return entry.getValue();
            }
        }
        // If we still don't find a match, then we don't know the status
        log.warn("Didn't find system status for system {}", systemName);
        return CONNECTION_UNKNOWN;
    }

    /**
     * Confirm status of a communication port is not down, based on the port name.
     *
     * @param portName the port name
     * @return true if port connection is operational or unknown, false if not
     * @deprecated since 4.7.1; use
     * {@link #isConnectionOk(java.lang.String, java.lang.String)} instead.
     */
    @Deprecated
    public synchronized boolean isConnectionOk(@Nonnull String portName) {
        return isConnectionOk(null, portName);
    }

    /**
     * Confirm status of a communication port is not down.
     *
     * @param systemName human-readable name for system like "LocoNet 2"
     *                      which can be obtained from i.e. {@link SystemConnectionMemo#getUserName}.
     * @param portName   the port name
     * @return true if port connection is operational or unknown, false if not
     */
    public synchronized boolean isConnectionOk(String systemName, @Nonnull String portName) {
        String stateText = getConnectionState(systemName, portName);
        return !stateText.equals(CONNECTION_DOWN);
    }

    /**
     * Confirm status of a communication port is not down, based on the system name.
     *
     * @param systemName human-readable name for system like "LocoNet 2"
     *                      which can be obtained from i.e. {@link SystemConnectionMemo#getUserName}.
     * @return true if port connection is operational or unknown, false if not. This includes
     *                      returning true if the connection is not recognized.
     */
    public synchronized boolean isSystemOk(@Nonnull String systemName) {
        // see if there is a key that has systemName as the port value.
        for (Map.Entry<ConnectionKey, String> entry : portStatus.entrySet()) {
            if ((entry.getKey().getSystemName() != null) && (entry.getKey().getSystemName().equals(systemName))) {
                // if we find a match, return it
                return !portStatus.get(entry.getKey()).equals(CONNECTION_DOWN);
            }
        }
        // and if we still don't find a match, go ahead and reply true
        // as we consider the state unknown.
        return true;
    }

    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);

    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    protected void firePropertyChange(@Nonnull String p, Object old, Object n) {
        log.debug("firePropertyChange {} old: {} new: {}", p, old, n);
        pcs.firePropertyChange(p, old, n);
    }

    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    /**
     * ConnectionKey is an internal class containing the port name and system
     * name of a connection.
     * <p>
     * ConnectionKey is used as a key in a HashMap of the connections on the
     * system.
     * <p>
     * It is allowable for either the port name or the system name to be null,
     * but not both.
     */
    static private class ConnectionKey {

        String portName = null;
        String systemName = null;  // human-readable name for system

        /**
         * constructor
         *
         * @param system human-readable name for system like "LocoNet 2"
         *                      which can be obtained from i.e. {@link SystemConnectionMemo#getUserName}.
         * @param port   port name
         * @throws IllegalArgumentException if both system and port are null;
         */
        public ConnectionKey(String system, @Nonnull String port) {
            if (system == null && port == null) {
                throw new IllegalArgumentException("At least one of system name or port name must be provided");
            }
            systemName = system;
            portName = port;
        }

        public String getSystemName() {
            return systemName;
        }

        public String getPortName() {
            return portName;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || !(o instanceof ConnectionKey)) {
                return false;
            }
            ConnectionKey other = (ConnectionKey) o;

            return (systemName == null ? other.getSystemName() == null : systemName.equals(other.getSystemName()))
                    && (portName == null ? other.getPortName() == null : portName.equals(other.getPortName()));
        }

        @Override
        public int hashCode() {
            if (systemName == null) {
                return portName.hashCode();
            } else if (portName == null) {
                return systemName.hashCode();
            } else {
                return (systemName.hashCode() + portName.hashCode());
            }
        }

    }

    private final static Logger log = LoggerFactory.getLogger(ConnectionStatus.class);

}

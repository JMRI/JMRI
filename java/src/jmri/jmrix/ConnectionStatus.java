package jmri.jmrix;

import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interface for classes that wish to get notification when the connection to
 * the layout changes.
 * <p>
 * Maintains a single instance, as there is only one set of connections for the
 * running program.
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
     * record the single instance *
     */
    private static ConnectionStatus _instance = null;

    public static synchronized ConnectionStatus instance() {
        if (_instance == null) {
            if (log.isDebugEnabled()) {
                log.debug("ConnectionStatus creating instance");
            }
            // create and load
            _instance = new ConnectionStatus();
        }
        if (log.isDebugEnabled()) {
            log.debug("ConnectionStatus returns instance " + _instance);
        }
        return _instance;
    }

    /**
     * Set the connection state of a communication port.
     *
     * @param systemName the system name
     * @param portName   the port name
     */
    public synchronized void addConnection(String systemName, String portName) {
        log.debug("add connection to monitor {} {}", systemName, portName);
        ConnectionKey newKey = new ConnectionKey(systemName, portName);
        if (!portStatus.containsKey(newKey)) {
            portStatus.put(newKey, CONNECTION_UNKNOWN);
            firePropertyChange("add", null, portName);
        }
    }

    /**
     * Set the connection state of a communication port.
     *
     * @param portName communication port name
     * @param state    port state
     * @deprecated since 4.7.1 use
     * {@link #setConnectionState(java.lang.String, java.lang.String, java.lang.String)}
     * instead.
     */
    @Deprecated
    public synchronized void setConnectionState(String portName, String state) {
        setConnectionState(null, portName, state);
    }

    /**
     * Set the connection state of a communication port.
     *
     * @param systemName the system name
     * @param portName   the port name
     * @param state      one of ConnectionStatus.UP, ConnectionStatus.DOWN, or
     *                   ConnectionStatus.UNKNOWN.
     */
    public synchronized void setConnectionState(String systemName, String portName, String state) {
        log.debug("set {} connection status: {}", portName, state);
        ConnectionKey newKey = new ConnectionKey(systemName, portName);
        if (!portStatus.containsKey(newKey)) {
            portStatus.put(newKey, state);
            firePropertyChange("add", null, portName);
        } else {
            firePropertyChange("change", portStatus.put(newKey, state), portName);
        }
    }

    /**
     * Get the status of a communication port.
     *
     * @param portName the port name
     * @return status
     * @deprecated since 4.7.1 use
     * {@link #getConnectionState(java.lang.String, java.lang.String)} instead.
     */
    @Deprecated
    public synchronized String getConnectionState(String portName) {
        ConnectionKey newKey = new ConnectionKey(null, portName);
        if (portStatus.containsKey(newKey)) {
            return getConnectionState(null, portName);
        } else {
            // we have to see if there is a key that has portName as the port value.
            for (ConnectionKey c : portStatus.keySet()) {
                if (c.getPortName() == null ? portName == null : c.getPortName().equals(portName)) {
                    // if we find a match, return it.
                    return getConnectionState(c.getSystemName(), c.getPortName());
                }
            }
        }
        // and if we still don't find a match, go ahead and try with null as
        // the system name.
        return getConnectionState(null, portName);
    }

    /**
     * get the status of a communication port based on the system name.
     *
     * @param systemName the system name
     * @return the status
     */
    public synchronized String getSystemState(String systemName) {
        ConnectionKey newKey = new ConnectionKey(systemName, null);
        if (portStatus.containsKey(newKey)) {
            return getConnectionState(systemName, null);
        } else {
            // we have to see if there is a key that has systemName as the port value.
            for (ConnectionKey c : portStatus.keySet()) {
                if (c.getSystemName() == null ? systemName == null : c.getSystemName().equals(systemName)) {
                    // if we find a match, return it.
                    return getConnectionState(c.getSystemName(), c.getPortName());
                }
            }
        }
        // and if we still don't find a match, go ahead and try with null as
        // the port name.
        return getConnectionState(systemName, null);
    }

    /**
     * Get the status of a communication port.
     *
     * @param systemName the system name
     * @param portName   the port name
     * @return the status
     */
    public synchronized String getConnectionState(String systemName, String portName) {
        String stateText = CONNECTION_UNKNOWN;
        ConnectionKey newKey = new ConnectionKey(systemName, portName);
        if (portStatus.containsKey(newKey)) {
            stateText = portStatus.get(newKey);
        }
        log.debug("get connection status: {} {}", portName, stateText);
        return stateText;
    }

    /**
     * Get the status of a communication port.
     *
     * @param portName the port name
     * @return true if port connection is operational or unknown, false if not
     * @deprecated since 4.7.1; use
     * {@link #isConnectionOk(java.lang.String, java.lang.String)} instead.
     */
    @Deprecated
    public synchronized boolean isConnectionOk(String portName) {
        return isConnectionOk(null, portName);
    }

    /**
     * Get the status of a communication port.
     *
     * @param systemName the system name
     * @param portName   the port name
     * @return true if port connection is operational or unknown, false if not
     */
    public synchronized boolean isConnectionOk(String systemName, String portName) {
        String stateText = getConnectionState(systemName, portName);
        return !stateText.equals(CONNECTION_DOWN);
    }

    /**
     * Get the status of a communication port based on the system name.
     *
     * @param systemName the system name
     * @return true if port connection is operational or unknown, false if not
     */
    public synchronized boolean isSystemOk(String systemName) {
        ConnectionKey newKey = new ConnectionKey(systemName, null);
        if (portStatus.containsKey(newKey)) {
            return isConnectionOk(systemName, null);
        } else {
            // we have to see if there is a key that has systemName as the port value.
            for (ConnectionKey c : portStatus.keySet()) {
                if (c.getSystemName() == null ? systemName == null : c.getSystemName().equals(systemName)) {
                    // if we find a match, return it.
                    return isConnectionOk(c.getSystemName(), c.getPortName());
                }
            }
        }
        // and if we still don't find a match, go ahead and try with null as
        // the port name.
        return isConnectionOk(systemName, null);
    }

    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);

    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    protected void firePropertyChange(String p, Object old, Object n) {
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
        String systemName = null;

        /**
         * constructor
         *
         * @param system String system name
         * @param port   String port name
         * @throws IllegalArgumentException if both system and port are null;
         */
        public ConnectionKey(String system, String port) {
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

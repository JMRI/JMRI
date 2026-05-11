package jmri.jmrix;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import jmri.SystemConnectionMemo;

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
 * {@link jmri.SystemConnectionMemo#getUserName}.
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

    // hashmap of SystemConnectionMemo's and their status
    private final HashMap<SystemConnectionMemo, String> portStatus = new HashMap<>();

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

    // Used by ConnectionStatusTest
    static synchronized void clearInstance() {
        _instance = null;
    }

    private ConnectionStatus() {
        // Private constructor to protect singleton
    }

    /**
     * Add a connection with a given memo to the portStatus set
     * if not yet present in the set.
     *
     * @param memo the system memo
     */
    public synchronized void addConnection(@Nonnull SystemConnectionMemo memo) {
        log.debug("addConnection memo {}", memo);
        if (!portStatus.containsKey(memo)) {
            portStatus.put(memo, CONNECTION_UNKNOWN);
            firePropertyChange("add", null, memo);
        }
    }

    /**
     * Set the connection state of a communication port.
     *
     * @param memo the system memo
     * @param state      one of ConnectionStatus.UP, ConnectionStatus.DOWN, or
     *                   ConnectionStatus.UNKNOWN.
     */
    public synchronized void setConnectionState(@Nonnull SystemConnectionMemo memo, @Nonnull String state) {
        log.debug("setConnectionState memo: {} state: {}", memo, state);
        if (!portStatus.containsKey(memo)) {
            portStatus.put(memo, state);
            firePropertyChange("add", null, memo);
            log.debug("New Connection added: {} ", memo);
        } else {
            firePropertyChange("change", portStatus.put(memo, state), memo);
        }
    }

    /**
     * Get the status of a communication port with a given name.
     *
     * @param memo the system memo
     * @return the status
     */
    public synchronized String getConnectionState(@Nonnull SystemConnectionMemo memo) {
        log.debug("getConnectionState memo {}", memo);
        String stateText = CONNECTION_UNKNOWN;
        if (portStatus.containsKey(memo)) {
            stateText = portStatus.get(memo);
            log.debug("connection found : {}", stateText);
        } else {
            log.debug("connection memo {} not found, {}", memo, stateText);
        }
        return stateText;
    }

    /**
     * Confirm status of a communication port is not down.
     *
     * @param memo the system memo
     * @return true if port connection is operational or unknown, false if not
     */
    public synchronized boolean isConnectionOk(@Nonnull SystemConnectionMemo memo) {
        String stateText = getConnectionState(memo);
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
        for (var entry : portStatus.entrySet()) {
            var memoSystemName = entry.getKey().getUserName();
            if ((memoSystemName != null) && (memoSystemName.equals(systemName))) {
                // if we find a match, return it
                return !portStatus.get(entry.getKey()).equals(CONNECTION_DOWN);
            }
        }
        // and if we still don't find a match, go ahead and reply true
        // as we consider the state unknown.
        return true;
    }

    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);
    Map<SystemConnectionMemo, java.beans.PropertyChangeSupport> pcsMap = new HashMap<>();

    public synchronized void addPropertyChangeListener(
            @Nonnull java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public synchronized void addPropertyChangeListener(
            @Nonnull SystemConnectionMemo memo, @Nonnull java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
        pcsMap.computeIfAbsent(memo, k -> new java.beans.PropertyChangeSupport(this))
                .addPropertyChangeListener(l);
    }

    protected void firePropertyChange(@Nonnull String p, Object old, @Nonnull SystemConnectionMemo memo) {
        log.debug("firePropertyChange {} old: {} new: {}", p, old, memo);
        pcs.firePropertyChange(p, old, memo);
        var memoPCS = pcsMap.get(memo);
        if (memoPCS != null) {
            memoPCS.firePropertyChange(p, old, memo);
        }
    }

    public synchronized void removePropertyChangeListener(
            @Nonnull java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    public synchronized void removePropertyChangeListener(
            @Nonnull SystemConnectionMemo memo, @Nonnull java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
        var memoPCS = pcsMap.get(memo);
        if (memoPCS != null) {
            memoPCS.removePropertyChangeListener(l);
        }
    }

    private static final Logger log = LoggerFactory.getLogger(ConnectionStatus.class);

}

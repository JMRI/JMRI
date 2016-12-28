package jmri.jmrix;

import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interface for classes that wish to get notification when the connection to
 * the layout changes.
 * <p>
 * Maintains a single instance, as there is only one set of connections for 
 * the running program.
 *
 * @author Daniel Boudreau Copyright (C) 2007
 * @author Paul Bender Copyright (C) 2016
 */
public class ConnectionStatus {

    public static final String CONNECTION_UNKNOWN = "Unknown";
    public static final String CONNECTION_UP = "Connected";
    public static final String CONNECTION_DOWN = "Not Connected";

    // hashmap of ConnectionKey objects and their status
    private HashMap<ConnectionKey,String> portStatus = new HashMap<ConnectionKey,String>();

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
     * sets the connection state of a communication port
     *
     * @param systemName String containing the system name
     * @param portName String containing the port name
     */
    public synchronized void addConnection(String systemName, String portName) {
        log.debug("add connection to monitor " + systemName + " " + portName);
        ConnectionKey newKey = new ConnectionKey(systemName,portName);
        if (portStatus.containsKey(newKey)) {
            return;
        } else {
            portStatus.put(newKey,CONNECTION_UNKNOWN);
            firePropertyChange("add", null, portName);
        }
    }

    /**
     * sets the connection state of a communication port
     *
     * @param portName = communication port name
     * @deprecated since 4.7.1 use setConnectionSTate(String,String,String) instead.
     */
    @Deprecated
    public synchronized void setConnectionState(String portName, String state) {
       setConnectionState(null,portName,state);
    }

    /**
     * sets the connection state of a communication port
     *
     * @param systemName String containing the system name
     * @param portName String containing the port name
     * @param state one of ConnectionStatus.UP, ConnectionStatus.DOWN, 
     *        or ConnectionStatus.UNKNOWN.
     */
    public synchronized void setConnectionState(String systemName,String portName, String state) {
        log.debug("set " + portName + " connection status: " + state);
        ConnectionKey newKey = new ConnectionKey(systemName,portName);
        if (!portStatus.containsKey(newKey)) {
            portStatus.put(newKey,state);
            firePropertyChange("add", null, portName);
        } else {
            firePropertyChange("change", portStatus.put(newKey,state), portName);
        }
    }

    /**
     * get the status of a communication port
     *
     * @return status string
     * @deprecated since 4.7.1 use getConnectionState(String,String) instead.
     */
    @Deprecated
    public synchronized String getConnectionState(String portName) {
        ConnectionKey newKey = new ConnectionKey(null,portName);
        if(portStatus.containsKey(newKey)) {
           return getConnectionState(null,portName);
        } else {
           // we have to see if there is a key that has portName as the port value.
           for( ConnectionKey c :portStatus.keySet()) {
              if(c.getPortName() == portName) {
                // if we find a match, return it.
                return getConnectionState(c.getSystemName(),c.getPortName());
              }
           }
        }
        // and if we still don't find a match, go ahead and try with null as
        // the system name.
        return getConnectionState(null,portName);
    }

    /**
     * get the status of a communication port
     *
     * @param systemName String containing the system name
     * @param portName String containing the port name
     * @return status string
     */
    public synchronized String getConnectionState(String systemName,String portName) {
        String stateText = CONNECTION_UNKNOWN;
        ConnectionKey newKey = new ConnectionKey(systemName,portName);
        if(portStatus.containsKey(newKey)) {
            stateText = portStatus.get(newKey);
        }
        log.debug("get connection status: " + portName + " " + stateText);
        return stateText;
    }

    /**
     * Returns status of a communication port
     *
     * @return true if port connection is operational or unknown, false if not
     * @deprecated since 4.7.1.  use isConnectionOk(String,String) instead.
     */
    @Deprecated
    public synchronized boolean isConnectionOk(String portName) {
         return isConnectionOk(null,portName);
    }

    /**
     * Returns status of a communication port
     *
     * @param systemName String containing the system name
     * @param portName String containing the port name
     * @return true if port connection is operational or unknown, false if not
     */
    public synchronized boolean isConnectionOk(String systemName,String portName) { 
        String stateText = getConnectionState(systemName,portName);
        if (stateText.equals(CONNECTION_DOWN)) {
            return false;
        } else {
            return true;
        }
    }

    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);

    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    protected void firePropertyChange(String p, Object old, Object n) {
        log.debug("firePropertyChange " + p + " old: " + old + " new: " + n);
        pcs.firePropertyChange(p, old, n);
    }

    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    /*
     *  ConnectionKey is an internal class containing the port name 
     *  and system name of a connection.
     *  <p>
     *  ConnectionKey is used as a key in a hashmap of the connections
     *  on the system.
     *  <p>
     *  It is allowable for either the port name or the system name to be null,      *  but not both.
     */
    private class ConnectionKey {
       String portName = null;
       String systemName = null;

       /*
        * constructor
        * @param system String system name
        * @param port String port name
        * @throws IllegalArgumentException if both system and port are null;
        */
       public ConnectionKey(String system,String port){
           if(system == null && port == null) {
              throw new IllegalArgumentException("At least one of system name or port name must be provided");
           }
           systemName=system;
           portName=port;
       }

       public String getSystemName(){
           return systemName;
       }

       public String getPortName(){
           return portName;
       }

       /*
        * Compares an object to see if it is equal to this obect.
        * @return true if equal, false otherwise.
        */
       @Override
       public boolean equals(Object o){
          if(o == null || !(o instanceof ConnectionKey) ) {
             return false;
          }
          ConnectionKey other = (ConnectionKey)o;

          if(systemName == other.systemName && portName == other.portName ) {
             return true;
          }
          return false;
       }

       @Override
       public int hashCode(){
          if(systemName==null) {
             return portName.hashCode();
          } else if(portName==null){
             return systemName.hashCode();
          } else {
             return(systemName.hashCode() + portName.hashCode());
          }
       }

    }

    private final static Logger log = LoggerFactory.getLogger(ConnectionStatus.class.getName());
}

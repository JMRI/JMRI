package jmri.jmrix;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import jmri.SystemConnectionMemo;

/**
 * Enables basic setup of a network client interface for a jmrix implementation.
 *
 * @author Kevin Dickerson Copyright (C) 2010
 * @author Based upon work originally done by Paul Bender Copyright (C) 2009
 * @see jmri.jmrix.NetworkConfigException
 */
abstract public class AbstractNetworkPortController extends AbstractPortController implements NetworkPortAdapter {

    // the host name and port number identify what we are talking to.
    protected String m_HostName = null;
    private String m_HostAddress = null;  // Internal IP address for  ZeroConf
    // configured clients.
    protected int m_port = 0;
    // keep the socket provides our connection.
    protected Socket socketConn = null;
    protected int connTimeout = 0; // connection timeout for read operations.
    // Default is 0, an infinite timeout.
    // the MQTT Credentials.

    protected AbstractNetworkPortController(SystemConnectionMemo connectionMemo) {
        super(connectionMemo);
        setHostName(""); // give the host name a default value of the empty string.
    }

    @Override
    public void connect() throws IOException {
        log.debug("connect() starts to {}:{}", getHostName(), getPort());
        opened = false;
        if (getHostAddress() == null || m_port == 0) {
            log.error("No host name or port set: {}:{}", m_HostName, m_port);
            return;
        }
        try {
            socketConn = new Socket(getHostAddress(), m_port);
            socketConn.setKeepAlive(true);
            socketConn.setSoTimeout(getConnectionTimeout());
            opened = true;
        } catch (IOException e) {
            log.error("Error opening network connection: {}", e.getMessage()); // nothing to help user in full exception
            if (m_port != 0) {
                ConnectionStatus.instance().setConnectionState(
                        getUserName(), m_HostName + ":" + m_port, ConnectionStatus.CONNECTION_DOWN);
            } else {
                ConnectionStatus.instance().setConnectionState(
                        getUserName(), m_HostName, ConnectionStatus.CONNECTION_DOWN);
            }
            throw (e);
        }
        if (opened && m_port != 0) {
            ConnectionStatus.instance().setConnectionState(
                    getUserName(), m_HostName + ":" + m_port, ConnectionStatus.CONNECTION_UP);
        } else if (opened) {
            ConnectionStatus.instance().setConnectionState(
                    getUserName(), m_HostName, ConnectionStatus.CONNECTION_UP);
        }
        log.trace("connect ends");
    }

    /**
     * Remember the associated host name.
     *
     * @param s the host name; if empty will use MDNS to get host name
     */
    @Override
    public void setHostName(String s) {
        log.trace("setHostName({})", s, new Exception("traceback only"));
        m_HostName = s;
        if ((s == null || s.equals("")) && !getMdnsConfigure()) {
            m_HostName = JmrixConfigPane.NONE;
        }
    }

    @Override
    public String getHostName() {
        return m_HostName;
    }

    /**
     * Remember the associated IP Address This is used internally for mDNS
     * configuration. Public access to the IP address is through the hostname
     * field.
     *
     * @param s the address; if empty, will use the host name
     */
    protected void setHostAddress(String s) {
        log.trace("setHostAddress({})", s);
        m_HostAddress = s;
        if (s == null || s.equals("")) {
            m_HostAddress = m_HostName;
        }
    }

    protected String getHostAddress() {
        if (m_HostAddress == null) {
            return m_HostName;
        }
        return m_HostAddress;
    }

    /**
     * Remember the associated port number.
     *
     * @param p the port
     */
    @Override
    public void setPort(int p) {
        log.trace("setPort(int {})", p);
        m_port = p;
    }

    @Override
    public void setPort(String p) {
        log.trace("setPort(String {})", p);
        m_port = Integer.parseInt(p);
    }

    @Override
    public int getPort() {
        return m_port;
    }

    /**
     * Return the connection name for the network connection in the format of
     * ip_address:port
     *
     * @return ip_address:port
     */
    @Override
    public String getCurrentPortName() {
        String t;
        if (getMdnsConfigure()) {
            t = getHostAddress();
        } else {
            t = getHostName();
        }
        int p = getPort();
        if (t != null && !t.equals("")) {
            if (p != 0) {
                return t + ":" + p;
            }
            return t;
        } else {
            return JmrixConfigPane.NONE;
        }
    }

    /*
     * Set whether or not this adapter should be
     * configured automatically via MDNS.
     * Note: Default implementation ignores the parameter.
     *
     * @param autoconfig boolean value
     */
    @Override
    public void setMdnsConfigure(boolean autoconfig) {
    }

    /*
     * Get whether or not this adapter is configured
     * to use autoconfiguration via MDNS
     * Default implemntation always returns false.
     *
     * @return true if configured using MDNS
     */
    @Override
    public boolean getMdnsConfigure() {
        return false;
    }

    /*
     * Set the server's host name and port
     * using MDNS autoconfiguration.
     * Default implementation does nothing.
     */
    @Override
    public void autoConfigure() {
    }

    /*
     * Get and set the ZeroConf/mDNS advertisement name.
     * Default implementation does nothing.
     */
    @Override
    public void setAdvertisementName(String AdName) {
    }

    @Override
    public String getAdvertisementName() {
        return null;
    }

    /*
     * Get and set the ZeroConf/mDNS service type.
     * Default implementation does nothing.
     */
    @Override
    public void setServiceType(String ServiceType) {
    }

    @Override
    public String getServiceType() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataInputStream getInputStream() {
        log.trace("getInputStream() starts");
        if (socketConn == null) {
            log.error("getInputStream invoked with null socketConn");
        }
        if (!opened) {
            log.error("getInputStream called before load(), stream not available");
            if (m_port != 0) {
                ConnectionStatus.instance().setConnectionState(
                        getUserName(), m_HostName + ":" + m_port, ConnectionStatus.CONNECTION_DOWN);
            } else {
                ConnectionStatus.instance().setConnectionState(
                        getUserName(), m_HostName, ConnectionStatus.CONNECTION_DOWN);
            }
        }
        try {
            log.trace("getInputStream() returns normally");
            return new DataInputStream(socketConn.getInputStream());
        } catch (java.io.IOException ex1) {
            log.error("Exception getting input stream:", ex1);
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataOutputStream getOutputStream() {
        if (!opened) {
            log.error("getOutputStream called before load(), stream not available");
        }
        try {
            return new DataOutputStream(socketConn.getOutputStream());
        } catch (java.io.IOException e) {
            log.error("getOutputStream exception:", e);
            if (m_port != 0) {
                ConnectionStatus.instance().setConnectionState(
                        getUserName(), m_HostName + ":" + m_port, ConnectionStatus.CONNECTION_DOWN);
            } else {
                ConnectionStatus.instance().setConnectionState(
                        getUserName(), m_HostName, ConnectionStatus.CONNECTION_DOWN);
            }
        }
        return null;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void closeConnection(){
        try {
            socketConn.close();
        } catch (IOException e) {
            log.trace("Unable to close socket", e);
        }
        opened=false;
    }

    /**
     * Customizable method to deal with resetting a system connection after a
     * successful recovery of a connection.
     */
    @Override
    protected void resetupConnection() {
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void reconnectFromLoop(int retryNum){
        try {
            // if the device allows autoConfiguration,
            // we need to run the autoConfigure() call
            // before we try to reconnect.
            if (getMdnsConfigure()) {
                autoConfigure();
            }
            connect();
        } catch (IOException ex) {
            log.trace("restart failed", ex); // main warning to log.error done within connect();
            // if returned on exception stops thread and connection attempts
        }
    }

    /*
     * Set the connection timeout to the specified value.
     * If the socket is not null, set the SO_TIMEOUT option on the
     * socket as well.
     *
     * @param t timeout value in milliseconds
     */
    protected void setConnectionTimeout(int t) {
        connTimeout = t;
        try {
            if (socketConn != null) {
                socketConn.setSoTimeout(getConnectionTimeout());
            }
        } catch (java.net.SocketException se) {
            log.debug("Unable to set socket timeout option on socket");
        }
    }

    /*
     * Get the connection timeout value.
     *
     * @return timeout value in milliseconds
     */
    protected int getConnectionTimeout() {
        return connTimeout;
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractNetworkPortController.class);

}

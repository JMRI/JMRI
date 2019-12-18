package jmri.jmrix;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    protected AbstractNetworkPortController(SystemConnectionMemo connectionMemo) {
        super(connectionMemo);
        setHostName(""); // give the host name a default value of
        // the empty string.
    }

    @Override
    public void connect(String host, int port) throws IOException {
        setHostName(host);
        setPort(port);
        connect();
    }

    @Override
    public void connect() throws IOException {
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
    }

    /**
     * Query the status of this connection, at least as far as is
     * known.
     *
     * @return true if connection is open
     */
    @Override
    public boolean status() {
        return opened;
    }

    /**
     * Remember the associated host name.
     *
     * @param s the host name; if empty will use MDNS to get host name
     */
    @Override
    public void setHostName(String s) {
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
        m_port = p;
    }

    @Override
    public void setPort(String p) {
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

    @Override
    public void dispose() {
        super.dispose();
    }

    //private boolean allowConnectionRecovery = false;
    /**
     * Close the client side socket connection, reset the open flag and attempt
     * a reconnection. Called when a connection is initially lost.
     */
    @Override
    public void recover() {
        if (!allowConnectionRecovery) {
            return;
        }
        opened = false;
        try {
            socketConn.close();
        } catch (IOException e) {
            log.trace("Unable to close socket", e);
        }
        reconnect();
    }

    /**
     * Attempts to reconnect to a failed Server.
     */
    public void reconnect() {

        // If the connection is already open, then we shouldn't try a re-connect.
        if (opened && !allowConnectionRecovery) {
            return;
        }
        ReconnectWait thread = new ReconnectWait();
        thread.setName("Connection Recovery " + getCurrentPortName() );
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            log.error("Unable to join to the reconnection thread");
        }

        if (!opened) {
            log.error("Failed to re-establish connectivity");
        } else {
            resetupConnection();
            log.info("Reconnected to {}", getHostName());
        }
    }

    /**
     * Customizable method to deal with resetting a system connection after a
     * successful recovery of a connection.
     */
    protected void resetupConnection() {
    }

    class ReconnectWait extends Thread {

        public final static int THREADPASS = 0;
        public final static int THREADFAIL = 1;
        int _status;

        public int status() {
            return _status;
        }

        public ReconnectWait() {
            _status = THREADFAIL;
        }

        @Override
        public void run() {
            boolean reply = true;
            int count = 0;
            int secondCount = 0;
            while (reply) {
                safeSleep(reconnectinterval, "Waiting");
                count++;

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
                
                reply = !opened;
                if (count >= retryAttempts) {
                    log.error("Unable to reconnect after {} Attempts, increasing duration of retries", count);
                    //retrying but with twice the retry interval.
                    reconnectinterval = reconnectinterval * 2;
                    count = 0;
                    secondCount++;
                }
                if (secondCount >= 10) {
                    log.error("Giving up on reconnecting after 100 attempts");
                    reply = false;
                }
            }
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

    private final static Logger log = LoggerFactory.getLogger(AbstractNetworkPortController.class);

}

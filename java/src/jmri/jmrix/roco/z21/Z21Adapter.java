package jmri.jmrix.roco.z21;

import java.net.DatagramSocket;
import java.util.ResourceBundle;
import jmri.jmrix.ConnectionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adapter representing a Z21 communication port Note: This connection uses UDP
 * for communication.
 * <p>
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2008
 * @author	Paul Bender Copyright (C) 2004,2010,2011,2014
 */
public class Z21Adapter extends jmri.jmrix.AbstractNetworkPortController {

    protected static ResourceBundle rb;
    protected static int COMMUNICATION_UDP_PORT;
    protected static String DEFAULT_IP_ADDRESS;

    private javax.swing.Timer keepAliveTimer; // Timer used to periodically
    // send a message to both
    // ports to keep the ports
    // open
    private static final int keepAliveTimeoutValue = 30000; // Interval
    // to send a message
    // Must be < 60s.

    private DatagramSocket socket = null;

    public Z21Adapter() {
        super(new Z21SystemConnectionMemo());
        rb = ResourceBundle.getBundle("jmri.jmrix.roco.z21.z21AdapterConfigurationBundle");
        COMMUNICATION_UDP_PORT = java.lang.Integer.parseInt(rb.getString("z21UDPPort1"));
        DEFAULT_IP_ADDRESS = rb.getString("defaultZ21IPAddress");
        setHostName(DEFAULT_IP_ADDRESS);
        setPort(COMMUNICATION_UDP_PORT);
        allowConnectionRecovery = true; // all classes derived from this class
        // can recover from a connection failure

    }

    /**
     * Configure all of the other jmrix widgets needed to work with this adapter
     */
    @Override
    public void configure() {
        if (log.isDebugEnabled()) {
            log.debug("configure called");
        }
        // connect to a packetizing traffic controller
        Z21TrafficController packets = new Z21TrafficController();
        packets.connectPort(this);

        // start operation
        this.getSystemConnectionMemo().setTrafficController(packets);
        this.getSystemConnectionMemo().configureManagers();

        jmri.jmrix.roco.z21.ActiveFlag.setActive();
    }

    @Override
    public void connect() throws Exception {
        opened = false;
        if (getHostAddress() == null || m_port == 0) {
            log.error("No host name or port set : {}:{}", m_HostName, m_port);
            return;
        }
        try {
            socket = new DatagramSocket();
            opened = true;
        } catch (java.net.SocketException se) {
            log.error("Socket Exception creating connection.");
            if (m_port != 0) {
                ConnectionStatus.instance().setConnectionState(
                        m_HostName + ":" + m_port, ConnectionStatus.CONNECTION_DOWN);
            } else {
                ConnectionStatus.instance().setConnectionState(
                        m_HostName, ConnectionStatus.CONNECTION_DOWN);
            }
            throw (se);
        }
        if (opened && m_port != 0) {
            ConnectionStatus.instance().setConnectionState(
                    m_HostName + ":" + m_port, ConnectionStatus.CONNECTION_UP);
        } else if (opened) {
            ConnectionStatus.instance().setConnectionState(
                    m_HostName, ConnectionStatus.CONNECTION_UP);
        }

        keepAliveTimer();

    }

    /*
     * @return the DatagramSocket of this connection.  Returns null
     *         if not connected.
     */
    public DatagramSocket getSocket() {
        return socket;
    }

    /**
     * Check that this object is ready to operate. This is a question of
     * configuration, not transient hardware status.
     */
    public boolean status() {
        return opened;
    }

    @Override
    public Z21SystemConnectionMemo getSystemConnectionMemo() {
        return (Z21SystemConnectionMemo) super.getSystemConnectionMemo();
    }

    /**
     * Customizable method to deal with resetting a system connection after a
     * successful recovery of a connection.
     */
    @Override
    protected void resetupConnection() {
    }

    /*
     * Set up the keepAliveTimer, and start it.
     */
    private void keepAliveTimer() {
        if (keepAliveTimer == null) {
            keepAliveTimer = new javax.swing.Timer(keepAliveTimeoutValue, new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    // If the timer times out, send a request for status
                    Z21Adapter.this.getSystemConnectionMemo().getTrafficController()
                            .sendz21Message(
                                    jmri.jmrix.roco.z21.Z21Message.getSerialNumberRequestMessage(),
                                    null);
                }
            });
        }
        keepAliveTimer.stop();
        keepAliveTimer.setInitialDelay(keepAliveTimeoutValue);
        keepAliveTimer.setRepeats(true);
        keepAliveTimer.start();
    }

    private final static Logger log = LoggerFactory.getLogger(Z21Adapter.class.getName());

}

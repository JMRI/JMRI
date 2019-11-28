package jmri.jmrix.marklin.networkdriver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.DatagramSocket;
import jmri.jmrix.ConnectionStatus;
import jmri.jmrix.marklin.MarklinPortController;
import jmri.jmrix.marklin.MarklinSystemConnectionMemo;
import jmri.jmrix.marklin.MarklinTrafficController;
import jmri.util.com.rbnb.UDPInputStream;
import jmri.util.com.rbnb.UDPOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements NetworkPortAdapter for the Marklin system network connection.
 * <p>
 * This connects a Marklin command station via a UDP connection. Normally
 * controlled by the NetworkDriverFrame class.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002, 2003, 2008
 * @author Kevin Dickerson Copyright (C) 2012
 */
public class NetworkDriverAdapter extends MarklinPortController {

    protected DatagramSocket datagramSocketConn = null;

    public NetworkDriverAdapter() {
        super(new MarklinSystemConnectionMemo());
        allowConnectionRecovery = true;
        manufacturerName = jmri.jmrix.marklin.MarklinConnectionTypeList.MARKLIN;
        m_port = 15731;
    }

    @Override //ports are fixed and not user set
    public void setPort(int p) {
    }

    @Override //ports are fixed and not user set
    public void setPort(String p) {
    }

    @Override
    public void connect() {
        opened = false;

        if (m_HostName == null) {
            log.error("No host name or port set :" + m_HostName + ":" + m_port);
            return;
        }
        try {
            opened = true;
        } catch (Exception e) {
            log.error("a error opening network connection: " + e);
            if (m_port != 0) {
                ConnectionStatus.instance().setConnectionState(
                        null, m_HostName + ":" + m_port, ConnectionStatus.CONNECTION_DOWN);
            } else {
                ConnectionStatus.instance().setConnectionState(
                        null, m_HostName, ConnectionStatus.CONNECTION_DOWN);
            }
            throw (e);
        }
        if (opened && m_port != 0) {
            ConnectionStatus.instance().setConnectionState(
                    null, m_HostName + ":" + m_port, ConnectionStatus.CONNECTION_UP);
        } else if (opened) {
            ConnectionStatus.instance().setConnectionState(
                    null, m_HostName, ConnectionStatus.CONNECTION_UP);
        }
    }

    @Override
    public DataInputStream getInputStream() {
        if (!opened) {
            log.error("getInputStream called before load(), stream not available");
            if (m_port != 0) {
                ConnectionStatus.instance().setConnectionState(
                        null, m_HostName + ":" + m_port, ConnectionStatus.CONNECTION_DOWN);
            } else {
                ConnectionStatus.instance().setConnectionState(
                        null, m_HostName, ConnectionStatus.CONNECTION_DOWN);
            }
        }
        try {
            return new DataInputStream(new UDPInputStream(null, 15730));
        } catch (java.io.IOException ex1) {
            log.error("an Exception getting input stream", ex1);
            return null;
        }
    }

    @Override
    public DataOutputStream getOutputStream() {
        if (!opened) {
            log.error("getOutputStream called before load(), stream not available");
        }
        try {
            return new DataOutputStream(new UDPOutputStream(m_HostName, 15731));
        } catch (java.io.IOException e) {
            log.error("getOutputStream exception", e);
            if (m_port != 0) {
                ConnectionStatus.instance().setConnectionState(
                        null, m_HostName + ":" + m_port, ConnectionStatus.CONNECTION_DOWN);
            } else {
                ConnectionStatus.instance().setConnectionState(
                        null, m_HostName, ConnectionStatus.CONNECTION_DOWN);
            }
        }
        return null;
    }

    /**
     * Set up all of the other objects to operate with a Marklin command station
     * connected to this port.
     */
    @Override
    public void configure() {
        // connect to the traffic controller
        MarklinTrafficController control = new MarklinTrafficController();
        control.connectPort(this);
        control.setAdapterMemo(this.getSystemConnectionMemo());
        this.getSystemConnectionMemo().setMarklinTrafficController(control);
        this.getSystemConnectionMemo().configureManagers();
    }

    @Override
    public boolean status() {
        return opened;
    }

    private final static Logger log = LoggerFactory.getLogger(NetworkDriverAdapter.class);

}

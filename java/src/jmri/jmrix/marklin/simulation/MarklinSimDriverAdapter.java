package jmri.jmrix.marklin.simulation;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import jmri.jmrix.ConnectionStatus;
import jmri.jmrix.marklin.MarklinPortController;
import jmri.jmrix.marklin.MarklinSystemConnectionMemo;

/**
 * Implements NetworkPortAdapter for a simulated Marklin network connection.
 * @author Bob Jacobsen Copyright (C) 2001, 2002, 2003, 2008
 * @author Kevin Dickerson Copyright (C) 2012
 * @author Steve Young Copyright (C) 2024
 */
public class MarklinSimDriverAdapter extends MarklinPortController {

    public MarklinSimDriverAdapter() {
        super(new MarklinSystemConnectionMemo());
        getSystemConnectionMemo().setUserName("Marklin CS2 Simulation");
        allowConnectionRecovery = false;
        manufacturerName = jmri.jmrix.marklin.MarklinConnectionTypeList.MARKLIN;
        m_port = 15731;
    }

    @Override
    public void connect() {
        opened = true;
        ConnectionStatus.instance().setConnectionState(
            null, m_HostName, ConnectionStatus.CONNECTION_UP);
    }

    @Override
    public DataInputStream getInputStream() {
        return null;
    }

    @Override
    public DataOutputStream getOutputStream() {
        return null;
    }

    /**
     * Set up all of the other objects to operate with a Marklin command station
     * connected to this port.
     */
    @Override
    public void configure() {
        // connect to the traffic controller
        MarklinSimTrafficController tc = new MarklinSimTrafficController();
        tc.setAdapterMemo(getSystemConnectionMemo());
        getSystemConnectionMemo().setMarklinTrafficController(tc);
        getSystemConnectionMemo().configureManagers();
    }

    @Override
    public boolean status() {
        return true;
    }

    // private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MarklinSimDriverAdapter.class);

}

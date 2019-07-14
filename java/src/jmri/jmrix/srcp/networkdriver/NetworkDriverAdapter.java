package jmri.jmrix.srcp.networkdriver;

import jmri.jmrix.srcp.SRCPPortController;
import jmri.jmrix.srcp.SRCPTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements SerialPortAdapter for the SRCP system network connection.
 * <p>
 * This connects an SRCP server (daemon) via a telnet connection. Normally
 * controlled by the NetworkDriverFrame class.
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2002, 2003, 2008
 * @author	Paul Bender Copyright (C) 2010
 */
public class NetworkDriverAdapter extends SRCPPortController {

    public NetworkDriverAdapter() {
        super(new jmri.jmrix.srcp.SRCPSystemConnectionMemo());
    }

    /**
     * set up all of the other objects to operate with an SRCP command station
     * connected to this port
     */
    @Override
    public void configure() {
        // connect to the traffic controller
        SRCPTrafficController control = new SRCPTrafficController();
        control.connectPort(this);
        this.getSystemConnectionMemo().setTrafficController(control);
        this.getSystemConnectionMemo().configureManagers();
        this.getSystemConnectionMemo().configureCommandStation();
    }

    @Override
    public boolean status() {
        return opened;
    }

    // private control members
    private boolean opened = false;

    // private final static Logger log = LoggerFactory.getLogger(NetworkDriverAdapter.class);

}

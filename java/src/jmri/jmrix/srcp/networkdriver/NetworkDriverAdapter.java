// NetworkDriverAdapter.java
package jmri.jmrix.srcp.networkdriver;

import jmri.jmrix.srcp.SRCPPortController;
import jmri.jmrix.srcp.SRCPTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements SerialPortAdapter for the SRCP system network connection.
 * <P>
 * This connects an SRCP server (daemon) via a telnet connection. Normally
 * controlled by the NetworkDriverFrame class.
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2002, 2003, 2008
 * @author	Paul Bender Copyright (C) 2010
 * @version	$Revision$
 */
public class NetworkDriverAdapter extends SRCPPortController implements jmri.jmrix.NetworkPortAdapter {

    public NetworkDriverAdapter() {
        super(new jmri.jmrix.srcp.SRCPSystemConnectionMemo());
    }

    /**
     * set up all of the other objects to operate with an SRCP command station
     * connected to this port
     */
    public void configure() {
        // connect to the traffic controller
        SRCPTrafficController control = new SRCPTrafficController();
        control.connectPort(this);
        this.getSystemConnectionMemo().setTrafficController(control);
        this.getSystemConnectionMemo().configureManagers();
        this.getSystemConnectionMemo().configureCommandStation();

         // mark OK for menus
        jmri.jmrix.srcp.ActiveFlag.setActive();
    }

    public boolean status() {
        return opened;
    }

    // private control members
    private boolean opened = false;

    /*
     * @deprecated since 4.3.5
     */
    @Deprecated
    static public NetworkDriverAdapter instance() {
        log.error("Deprecated instance() method called");
        return null;
    }

    private final static Logger log = LoggerFactory.getLogger(NetworkDriverAdapter.class.getName());

}

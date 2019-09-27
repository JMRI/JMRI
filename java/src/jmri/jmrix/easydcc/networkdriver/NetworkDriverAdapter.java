package jmri.jmrix.easydcc.networkdriver;

import java.net.Socket;
import java.util.Arrays;
import java.util.Vector;
import jmri.jmrix.easydcc.EasyDccNetworkPortController;
import jmri.jmrix.easydcc.EasyDccSystemConnectionMemo;
import jmri.jmrix.easydcc.EasyDccTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements NetworkDriverAdapter for the EasyDCC system connection.
 * <p>
 * This connects an EasyDCC command station via a telnet connection.
 * Normally controlled by the NetworkDriverFrame class.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002, 2003
 */
public class NetworkDriverAdapter extends EasyDccNetworkPortController {

    public NetworkDriverAdapter() {
        super(new EasyDccSystemConnectionMemo("E", "EasyDCC via Network")); // pass customized user name
    }

    /**
     * Set up all of the other objects to operate with an EasyDCC command
     * station connected to this port.
     */
    @Override
    public void configure() {
        // connect to the traffic controller, which is provided via the memo
        log.debug("set tc for memo {}", getSystemConnectionMemo().getUserName());

        getSystemConnectionMemo().getTrafficController().connectPort(this);

        // do the common manager config
        getSystemConnectionMemo().configureManagers();
    }

    @Override
    public boolean status() {
        return opened;
    }

    // private control members
    private boolean opened = false;

    Socket socket;

    private final static Logger log = LoggerFactory.getLogger(NetworkDriverAdapter.class);

}

package jmri.jmrix.easydcc.networkdriver;

import java.net.Socket;
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
        // connect to the traffic controller
        log.debug("set tc for memo {}", getSystemConnectionMemo().getUserName());
        EasyDccTrafficController control = new EasyDccTrafficController(getSystemConnectionMemo());
        control.connectPort(this);
        this.getSystemConnectionMemo().setEasyDccTrafficController(control);
        // do the common manager config
        this.getSystemConnectionMemo().configureManagers();
    }

    @Override
    public boolean status() {
        return opened;
    }

    // private control members
    private boolean opened = false;

    /**
     * @deprecated JMRI Since 4.9.5 instance() shouldn't be used, convert to JMRI multi-system support structure
     */
    @Deprecated
    static public NetworkDriverAdapter instance() {
        log.error("Unexpected call to instance()");
        return null;
    }

    Socket socket;

    public Vector<String> getPortNames() {
        log.error("Unexpected call to getPortNames");
        return null;
    }

    public String openPort(String portName, String appName) {
        log.error("Unexpected call to openPort");
        return null;
    }

    public String[] validBaudRates() {
        log.error("Unexpected call to validBaudRates");
        return null;
    }

    private final static Logger log = LoggerFactory.getLogger(NetworkDriverAdapter.class);

}

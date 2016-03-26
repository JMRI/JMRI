// NetworkDriverAdapter.java
package jmri.jmrix.easydcc.networkdriver;

import java.net.Socket;
import java.util.Vector;
import jmri.jmrix.easydcc.EasyDccNetworkPortController;
import jmri.jmrix.easydcc.EasyDccTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements SerialPortAdapter for the EasyDcc system network connection.
 * <P>
 * This connects an EasyDcc command station via a telnet connection. Normally
 * controlled by the NetworkDriverFrame class.
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2002, 2003
 * @version	$Revision$
 */
public class NetworkDriverAdapter extends EasyDccNetworkPortController {

    public NetworkDriverAdapter() {
        super(new jmri.jmrix.easydcc.EasyDccSystemConnectionMemo("E", "EasyDCC via Network"));
        setManufacturer(jmri.jmrix.easydcc.EasyDccConnectionTypeList.EASYDCC);
    }

    /**
     * set up all of the other objects to operate with an EasyDcc command
     * station connected to this port
     */
    public void configure() {
        // connect to the traffic controller
        EasyDccTrafficController control = EasyDccTrafficController.instance();
        control.connectPort(this);
        this.getSystemConnectionMemo().setEasyDccTrafficController(control);
        this.getSystemConnectionMemo().configureManagers();

        jmri.jmrix.easydcc.ActiveFlag.setActive();
    }

    public boolean status() {
        return opened;
    }

    // private control members
    private boolean opened = false;

    static public NetworkDriverAdapter instance() {
        if (mInstance == null) {
            // initialize object, then make instance
            NetworkDriverAdapter m = new NetworkDriverAdapter();
            m.setPort(0);
            mInstance = m;
        }
        return mInstance;
    }
    static NetworkDriverAdapter mInstance = null;

    //The following needs to be enabled once systemconnectionmemo has been correctly implemented
    //public SystemConnectionMemo getSystemConnectionMemo() { return adaptermemo; }
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

    private final static Logger log = LoggerFactory.getLogger(NetworkDriverAdapter.class.getName());

}

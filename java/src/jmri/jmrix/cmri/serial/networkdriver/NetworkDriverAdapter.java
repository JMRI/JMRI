package jmri.jmrix.cmri.serial.networkdriver;

import jmri.jmrix.cmri.CMRISystemConnectionMemo;
import jmri.jmrix.cmri.serial.SerialNetworkPortAdapter;
import jmri.jmrix.cmri.serial.SerialTrafficController;

/**
 * Implements SerialNetworkPortAdapter for a network connection.
 * <p>
 * This connects via a telnet connection. Normally
 * controlled by the NetworkDriverFrame class.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002, 2003, 2015
 */
public class NetworkDriverAdapter extends SerialNetworkPortAdapter {

    public NetworkDriverAdapter() {
        super(new CMRISystemConnectionMemo());
        setManufacturer(jmri.jmrix.cmri.CMRIConnectionTypeList.CMRI);
    }

    /**
     * Set up all of the other objects to operate connected to this port.
     */
    @Override
    public void configure() {
        // connect to the traffic controller
        SerialTrafficController tc = new SerialTrafficController();
        tc.connectPort(this);
        getSystemConnectionMemo().setTrafficController(tc);

        getSystemConnectionMemo().configureManagers();
    }

}

// NetworkDriverAdapter.java
package jmri.jmrix.cmri.serial.networkdriver;

import jmri.jmrix.cmri.CMRISystemConnectionMemo;
import jmri.jmrix.cmri.serial.SerialNetworkPortController;
import jmri.jmrix.cmri.serial.SerialSensorManager;
import jmri.jmrix.cmri.serial.SerialTrafficController;

/**
 * Implements SerialPortAdapter for a network connection.
 * <P>
 * This connects via a telnet connection. Normally
 * controlled by the NetworkDriverFrame class.
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2002, 2003, 2015
 * @version	$Revision: 28746 $
 */
public class NetworkDriverAdapter extends SerialNetworkPortController {

    public NetworkDriverAdapter() {
        super(new CMRISystemConnectionMemo());
        setManufacturer(jmri.jmrix.cmri.CMRIConnectionTypeList.CMRI);
    }

    /**
     * set up all of the other objects to operate connected to this port
     */
    public void configure() {
        // connect to the traffic controller
        SerialTrafficController tc = new SerialTrafficController();
        tc.connectPort(this);
        ((CMRISystemConnectionMemo)getSystemConnectionMemo()).setTrafficController(tc);

        ((CMRISystemConnectionMemo)getSystemConnectionMemo()).configureManagers();
    }

}

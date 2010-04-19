// NetworkDriverAdapter.java

package jmri.jmrix.ecos.networkdriver;

import jmri.jmrix.ConnectionStatus;
import jmri.jmrix.JmrixConfigPane;
import jmri.jmrix.ecos.*;

/*import java.io.*;
import java.net.*;
import java.util.Vector;*/

/**
 * Implements SerialPortAdapter for the ECOS system network connection.
 * <P>This connects
 * an ECOS command station via a telnet connection.
 * Normally controlled by the NetworkDriverFrame class.
 *
 * @author	Bob Jacobsen   Copyright (C) 2001, 2002, 2003, 2008
 * @version	$Revision: 1.10 $
 */
public class NetworkDriverAdapter extends EcosPortController {

    /**
     * set up all of the other objects to operate with an ECOS command
     * station connected to this port
     */
    public void configure() {
        // connect to the traffic controller
        EcosTrafficController control = EcosTrafficController.instance();
        control.connectPort(this);

        EcosSystemConnectionMemo memo 
            = new EcosSystemConnectionMemo(control);
        
        memo.configureManagers();
        
        jmri.jmrix.ecos.ActiveFlag.setActive();

    }

    public boolean status() {return opened;}

    // private control members
    private boolean opened = false;

    static public NetworkDriverAdapter instance() {
        if (mInstance == null) {
            mInstance = new NetworkDriverAdapter();
            mInstance.setPort(15471);
            mInstance.setManufacturer(jmri.jmrix.DCCManufacturerList.ESU);
        }
        return mInstance;
    }
    static NetworkDriverAdapter mInstance = null;

    //To be completed
    public void dispose(){

    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(NetworkDriverAdapter.class.getName());

}

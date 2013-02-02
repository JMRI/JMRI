// NetworkDriverAdapter.java

package jmri.jmrix.ecos.networkdriver;

import org.apache.log4j.Logger;
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
 * @version	$Revision$
 */
public class NetworkDriverAdapter extends EcosPortController implements jmri.jmrix.NetworkPortAdapter{

    public NetworkDriverAdapter() {
        super();
        allowConnectionRecovery = true;
        mManufacturer = jmri.jmrix.DCCManufacturerList.ESU;
        adaptermemo = new jmri.jmrix.ecos.EcosSystemConnectionMemo();
    }

    public EcosSystemConnectionMemo getSystemConnectionMemo() {return adaptermemo; }
    
    /**
     * set up all of the other objects to operate with an ECOS command
     * station connected to this port
     */
    public void configure() {
        // connect to the traffic controller
        EcosTrafficController control = new EcosTrafficController();
        control.connectPort(this);
        control.setAdapterMemo(adaptermemo);
        adaptermemo.setEcosTrafficController(control);
        adaptermemo.configureManagers();
        jmri.jmrix.ecos.ActiveFlag.setActive();
    }


    @Override
    public boolean status() {return opened;}
    
    //To be completed
    @Override
    public void dispose(){
        if (adaptermemo!=null)
            adaptermemo.dispose();
        adaptermemo = null;
    }
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="DE_MIGHT_IGNORE", 
        justification="We are closing the connection and not worried if it throws an exception as this stage")
    protected void closeConnection(){
        try {
            socketConn.close();
        } catch (Exception e) { }
        opened = false;
    }
    
    protected void resetupConnection() {
        log.info("reconnected to ECOS after lost connection");
        if(opened){
            adaptermemo.getTrafficController().connectPort(this);
            adaptermemo.getTurnoutManager().refreshItems();
            adaptermemo.getSensorManager().refreshItems();
            adaptermemo.getLocoAddressManager().refreshItems();
        }
    }

    static Logger log = Logger.getLogger(NetworkDriverAdapter.class.getName());

}

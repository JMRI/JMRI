// NetworkDriverAdapter.java

package jmri.jmrix.can.adapters.gridconnect.net;

import jmri.jmrix.can.adapters.gridconnect.GcTrafficController;
import jmri.jmrix.can.TrafficController;
import jmri.jmrix.SystemConnectionMemo;

import java.util.Vector;

/**
 * Implements SerialPortAdapter for the OpenLCB system network connection.
 * <P>This connects via a telnet connection.
 * Normally controlled by the NetworkDriverFrame class.
 *
 * @author	Bob Jacobsen   Copyright (C) 2010
 * @version	$Revision$
 */
public class NetworkDriverAdapter extends jmri.jmrix.AbstractNetworkPortController {

    //This should all probably be updated to use the AbstractNetworkPortContoller
    protected jmri.jmrix.can.CanSystemConnectionMemo adaptermemo;
    
    public NetworkDriverAdapter() {
        super();
        option1Name = "Gateway";
        options.put(option1Name, new Option("Gateway", new String[]{"Pass All", "Filtering"}));
        option2Name = "Protocol";
        options.put(option2Name, new Option("Connection Protocol", jmri.jmrix.can.ConfigurationManager.getSystemOptions(), false));
        adaptermemo = new jmri.jmrix.can.CanSystemConnectionMemo();
        adaptermemo.setUserName("OpenLCB");
        setManufacturer(jmri.jmrix.DCCManufacturerList.OPENLCB);
    }
    
    /**
     * set up all of the other objects to operate with an NCE command
     * station connected to this port
     */
    public void configure() {

        // Register the CAN traffic controller being used for this connection
        TrafficController tc = new GcTrafficController();
        adaptermemo.setTrafficController(tc);
        
        
        // Now connect to the traffic controller
        log.debug("Connecting port");
        tc.connectPort(this);

        adaptermemo.setProtocol(getOptionState(option2Name));

        // do central protocol-specific configuration    
        adaptermemo.configureManagers();
    }

    public boolean status() {return opened;}

    // private control members
    private boolean opened = false;
    
    public Vector<String> getPortNames() {
        log.error("Unexpected call to getPortNames");
        return null;
    }
    public String openPort(String portName, String appName)  {
        log.error("Unexpected call to openPort");
        return null;
    }
    public String[] validBaudRates() {
        log.error("Unexpected call to validBaudRates");
        return null;
    }
    
    public void dispose(){
        if (adaptermemo!=null)
            adaptermemo.dispose();
        adaptermemo = null;
    }
    
    public SystemConnectionMemo getSystemConnectionMemo() { return adaptermemo; }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(NetworkDriverAdapter.class.getName());

}

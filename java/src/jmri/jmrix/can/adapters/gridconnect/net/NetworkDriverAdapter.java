// NetworkDriverAdapter.java

package jmri.jmrix.can.adapters.gridconnect.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.can.adapters.gridconnect.GcTrafficController;
import jmri.jmrix.can.TrafficController;
import jmri.jmrix.SystemConnectionMemo;
import jmri.jmrix.can.ConfigurationManager;
import jmri.jmrix.can.adapters.gridconnect.canrs.MergTrafficController;

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
     * set up all of the other objects to operate with the CAN bus
     * connected via this TCP/IP link
     */
    public void configure() {
        TrafficController tc;
        if(getOptionState(option2Name).equals(ConfigurationManager.MERGCBUS)){
        // Register the CAN traffic controller being used for this connection
            tc = new MergTrafficController();
            try {
                tc.setCanId(Integer.parseInt(getOptionState("CANID")));
            } catch (Exception e) {
                log.error("Cannot parse CAN ID - check your preference settings "+e);
                log.error("Now using default CAN ID");
            }
        } else {
            tc = new GcTrafficController();
        }
        adaptermemo.setTrafficController(tc);
        
        
        // Now connect to the traffic controller
        log.debug("Connecting port");
        tc.connectPort(this);
        adaptermemo.setProtocol(getOptionState(option2Name));

        // do central protocol-specific configuration    
        adaptermemo.configureManagers();
        if (socketConn!=null) log.info("Connection complete with "+socketConn.getInetAddress());
    }
    @Override
    public boolean status() {return opened;}

    @Override
    public void dispose(){
        if (adaptermemo!=null)
            adaptermemo.dispose();
        adaptermemo = null;
    }
    
    public SystemConnectionMemo getSystemConnectionMemo() { return adaptermemo; }
    
    static Logger log = LoggerFactory.getLogger(NetworkDriverAdapter.class.getName());

}

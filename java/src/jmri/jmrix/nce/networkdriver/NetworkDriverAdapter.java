// NetworkDriverAdapter.java

package jmri.jmrix.nce.networkdriver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.nce.NceNetworkPortController;
import jmri.jmrix.nce.NceTrafficController;
import jmri.jmrix.nce.NceSystemConnectionMemo;

/**
 * Implements SerialPortAdapter for the NCE system network connection.
 * <P>This connects
 * an NCE command station via a telnet connection.
 * Normally controlled by the NetworkDriverFrame class.
 *
 * @author	Bob Jacobsen   Copyright (C) 2001, 2002, 2003
 * @version	$Revision$
 */
public class NetworkDriverAdapter extends NceNetworkPortController {

    public NetworkDriverAdapter() {
        super();
        option2Name = "Eprom";
        // the default is 2006 or later
        options.put(option2Name, new Option("Command Station EPROM", new String[]{"2006 or later", "2004 or earlier"}));
        adaptermemo = new NceSystemConnectionMemo();
        setManufacturer(jmri.jmrix.DCCManufacturerList.NCE);
    }

    @Override
    public NceSystemConnectionMemo getSystemConnectionMemo() {
    	return adaptermemo;
	}

    /**
     * set up all of the other objects to operate with an NCE command
     * station connected to this port
     */
    public void configure() {
        NceTrafficController tc = new NceTrafficController();
        adaptermemo.setNceTrafficController(tc);
        tc.setAdapterMemo(adaptermemo);           
               
    	// set the command options, Note that the NetworkDriver uses
    	// the second option for EPROM revision
        if (getOptionState(option2Name).equals(getOptionChoices(option2Name)[0])) {
            // setting binary mode
            adaptermemo.configureCommandStation(NceTrafficController.OPTION_2006);
        } else {
            adaptermemo.configureCommandStation(NceTrafficController.OPTION_2004);
        }
        
        tc.connectPort(this); 
        
        adaptermemo.configureManagers();
        
        jmri.jmrix.nce.ActiveFlag.setActive();
    }

    
    public void dispose(){
        if (adaptermemo!=null)
            adaptermemo.dispose();
        adaptermemo = null;
    }

    static Logger log = LoggerFactory.getLogger(NetworkDriverAdapter.class.getName());

}

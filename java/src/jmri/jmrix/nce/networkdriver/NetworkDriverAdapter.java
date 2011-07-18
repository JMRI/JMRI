// NetworkDriverAdapter.java

package jmri.jmrix.nce.networkdriver;

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
        if (getCurrentOption2Setting().equals(validOption2()[0])) {
        	adaptermemo.configureCommandStation(NceTrafficController.OPTION_2004);
        } else {
            // setting binary mode
            adaptermemo.configureCommandStation(NceTrafficController.OPTION_2006);
        }
        
        tc.connectPort(this); 
        
        adaptermemo.configureManagers();
        
        jmri.jmrix.nce.ActiveFlag.setActive();

    }

    /**
     * Option 2 is binary vs ASCII command set.
     */
    public String[] validOption2() { return new String[]{"2004 or earlier", "2006 or later"}; }

    /**
     * Get a String that says what Option 2 represents
     * May be an empty string, but will not be null
     */
    public String option2Name() { return "Command Station EPROM"; }

    /**
     * Set the binary vs ASCII command set option.
     */
    public void configureOption2(String value) { mOpt2 = value; }
    private String mOpt2 = null;
    public String getCurrentOption2Setting() {
        if (mOpt2 == null) return validOption2()[1];
        return mOpt2;
    }

//    static public NetworkDriverAdapter instance() {
//        if (mInstance == null) {
//            mInstance = new NetworkDriverAdapter();
//        }
//        return mInstance;
//    }
//    static NetworkDriverAdapter mInstance = null;
    
    public void dispose(){
        if (adaptermemo!=null)
            adaptermemo.dispose();
        adaptermemo = null;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(NetworkDriverAdapter.class.getName());

}

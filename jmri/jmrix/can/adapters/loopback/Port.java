// Port.java

package jmri.jmrix.can.adapters.loopback;

import jmri.jmrix.AbstractPortController;

import java.io.DataInputStream;
import java.io.DataOutputStream;


/**
 * LnHexFilePort implements a LnPortController via a
 * ASCII-hex input file. See below for the file format
 * There are user-level controls for
 *      send next message
 *	how long to wait between messages
 *
 * An object of this class should run in a thread
 * of its own so that it can fill the output pipe as
 * needed.
 *
 *	The input file is expected to have one message per line. Each line
 *	can contain as many bytes as needed, each represented by two Hex characters
 *	and separated by a space. Variable whitespace is not (yet) supported
 *
 * @author			Bob Jacobsen    Copyright (C) 2008
 * @version			$Revision: 1.6 $
 */
public class Port extends AbstractPortController {


    public Port() {
    }

    public void configure() {

        // Register the CAN traffic controller being used for this connection
        TrafficController.instance();

        // do central protocol-specific configuration    
        jmri.jmrix.can.ConfigurationManager.configure(mOpt1);

    }

    /**
     * Option 1 is CAN-based protocol
     */
    public String[] validOption1() { return jmri.jmrix.can.ConfigurationManager.getSystemOptions(); }
        
    /**
     * Get a String that says what Option 1 represents
     * May be an empty string, but will not be null
     */
    public String option1Name() { return "Connection Protocol"; }

    /**
     * Set the CAN protocol option.
     */
    public void configureOption1(String value) { mOpt1 = value; }
    protected String mOpt1 = null;
    public String getCurrentOption1Setting() {
        System.err.println("getCurrentOption1Setting "+mOpt1+" "+ this);
        if (mOpt1 == null) return validOption1()[0];
        return mOpt1;
    }

    // check that this object is ready to operate
    public boolean status() { return true; }
    
    static public Port instance() {
        if (mInstance == null) mInstance = new Port();
        return mInstance;
    }
    static Port mInstance = null;

    //////////////
    // not used //
    //////////////
    
    // Streams not used in simulations
    public DataInputStream getInputStream() {
        return null;
    }
    public DataOutputStream getOutputStream() {
        return null;
    }

	public String[] validBaudRates() { return new String[]{"None"}; }
	public String openPort(String portName, String appName) { return "invalid request"; }
	public java.util.Vector<String> getPortNames() { 
	    java.util.Vector<String> v = new java.util.Vector<String>();
	    v.addElement("(None)");
	    return v;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Port.class.getName());
}

/* @(#)Port.java */

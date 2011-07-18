// Port.java

package jmri.jmrix.can.adapters.loopback;

import jmri.jmrix.AbstractSerialPortController;

import java.io.DataInputStream;
import java.io.DataOutputStream;


/**
 * Loopback connection to simulate a CAN link
 *
 * @author			Bob Jacobsen    Copyright (C) 2008, 2010
 * @version			$Revision$
 */
public class Port extends AbstractSerialPortController {

    public Port() {
    }

    public void configure() {

        // Register the CAN traffic controller being used for this connection
        LoopbackTrafficController.instance();

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

    public String getCurrentOption1Setting() {
        log.debug("getCurrentOption1Setting "+mOpt1+" "+ this);
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

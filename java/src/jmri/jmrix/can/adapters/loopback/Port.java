// Port.java

package jmri.jmrix.can.adapters.loopback;

import jmri.jmrix.AbstractSerialPortController;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import jmri.jmrix.SystemConnectionMemo;

/**
 * Loopback connection to simulate a CAN link
 *
 * @author			Bob Jacobsen    Copyright (C) 2008, 2010
 * @version			$Revision$
 */
public class Port extends AbstractSerialPortController {

    public Port() {
        mPort="(None)";
        adaptermemo = new jmri.jmrix.can.CanSystemConnectionMemo();
    }

    public void configure() {

        // Register the CAN traffic controller being used for this connection
        adaptermemo.setTrafficController(new LoopbackTrafficController());

        // do central protocol-specific configuration    
        adaptermemo.setProtocol(mOpt1);
        
        adaptermemo.configureManagers();

    }
    
    protected jmri.jmrix.can.CanSystemConnectionMemo adaptermemo;

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
    
    public void dispose(){
        if (adaptermemo!=null)
            adaptermemo.dispose();
        adaptermemo = null;
    }
    
    public SystemConnectionMemo getSystemConnectionMemo() { return adaptermemo; }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Port.class.getName());
}

/* @(#)Port.java */

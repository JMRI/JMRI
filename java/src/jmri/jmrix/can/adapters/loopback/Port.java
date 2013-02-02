// Port.java

package jmri.jmrix.can.adapters.loopback;

import org.apache.log4j.Logger;
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
        option1Name = "Protocol";
        options.put(option1Name, new Option("Connection Protocol", jmri.jmrix.can.ConfigurationManager.getSystemOptions()));
        mPort="(None)";
        adaptermemo = new jmri.jmrix.can.CanSystemConnectionMemo();
    }

    public void configure() {

        // Register the CAN traffic controller being used for this connection
        adaptermemo.setTrafficController(new LoopbackTrafficController());

        // do central protocol-specific configuration    
        adaptermemo.setProtocol(getOptionState(option1Name));
        
        adaptermemo.configureManagers();

    }
    
    protected jmri.jmrix.can.CanSystemConnectionMemo adaptermemo;
    
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
    
    static Logger log = Logger.getLogger(Port.class.getName());
}

/* @(#)Port.java */

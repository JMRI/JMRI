// Port.java

package jmri.jmrix.can.adapters.loopback;

import jmri.jmrix.AbstractPortController;

import jmri.jmrix.can.CanConstants;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;

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
 * @version			$Revision: 1.1 $
 */
public class Port extends AbstractPortController {


    public Port() {
        configure();
    }

    public void configure() {
        // Set the CAN protocol being used
        int p = validOption1Values[0];  // default, but also defaulted in the initial value of selectedSpeed
        for (int i = 0; i<validForOption1.length; i++ ) {
            if (validForOption1[i].equals(mOpt1)) {
                p = validOption1Values[i];
            }
        }
        CanMessage.setProtocol(p);
//        CanReply.setProtocol(p);

        // Register the CAN traffic controller being used for this connection
        TrafficController.instance();
    

//        jmri.InstanceManager.setProgrammerManager(
//                new NceProgrammerManager(
//                    new NceProgrammer()));

//        jmri.InstanceManager.setPowerManager(new jmri.jmrix.nce.NcePowerManager());

//        jmri.InstanceManager.setTurnoutManager(new jmri.jmrix.nce.NceTurnoutManager());

        jmri.InstanceManager.setSensorManager(new jmri.jmrix.can.cbus.CbusSensorManager());

//        jmri.InstanceManager.setThrottleManager(new jmri.jmrix.nce.NceThrottleManager());

//        jmri.InstanceManager.addClockControl(new jmri.jmrix.nce.NceClockControl());
    }

    /**
     * Option 1 is binary vs ASCII command set.
     */
    public String[] validOption1() { return validForOption1; }
    
    protected String [] validForOption1 = new String[]{"MERG CBUS", "Test - do not use"};
    protected int [] validOption1Values = new int[]{CanConstants.CBUS, CanConstants.FOR_TESTING};
    
    /**
     * Get a String that says what Option 1 represents
     * May be an empty string, but will not be null
     */
    public String option1Name() { return "CAN Protocol"; }

    /**
     * Set the CAN protocol option.
     */
    public void configureOption1(String value) { mOpt1 = value; }
    protected String mOpt1 = null;
    public String getCurrentOption1Setting() {
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
	public java.util.Vector getPortNames() { 
	    java.util.Vector v = new java.util.Vector();
	    v.addElement("(None)");
	    return v;
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(Port.class.getName());
}

/* @(#)Port.java */

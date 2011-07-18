// NetworkDriverAdapter.java

package jmri.jmrix.can.adapters.gridconnect.net;

import jmri.jmrix.ConnectionStatus;
import jmri.jmrix.JmrixConfigPane;

import jmri.jmrix.can.adapters.gridconnect.GcTrafficController;

import java.io.*;
import java.net.*;
import java.util.Vector;

/**
 * Implements SerialPortAdapter for the OpenLCB system network connection.
 * <P>This connects via a telnet connection.
 * Normally controlled by the NetworkDriverFrame class.
 *
 * @author	Bob Jacobsen   Copyright (C) 2010
 * @version	$Revision$
 */
public class NetworkDriverAdapter extends jmri.jmrix.AbstractSerialPortController {

    /**
     * set up all of the other objects to operate with an NCE command
     * station connected to this port
     */
    public void configure() {
    	// set the command options, Note that the NetworkDriver uses
    	// the second option for EPROM revision
        if (getCurrentOption2Setting().equals(validOption2()[0])) {
        	//
        } else {
            // setting binary mode
            //
        }
        
        // start of code duplicated from net.ConnectionConfig
        log.error("This code comes from ConnectionConfig, and needs to be refactored");
        // Register the CAN traffic controller being used for this connection
        GcTrafficController.instance();
        
        // Now connect to the traffic controller
        log.debug("Connecting port");
        GcTrafficController.instance().connectPort(this);

        // do central protocol-specific configuration    
        jmri.jmrix.openlcb.ConfigurationManager.configure("OpenLCB CAN");  // MUST CHANGE
        
        // end of code duplicated from net.ConnectionConfig
        
        jmri.jmrix.openlcb.ActiveFlag.setActive();

    }

    // base class methods for the NcePortController interface
    public DataInputStream getInputStream() {
        if (!opened) {
            log.error("getInputStream called before load(), stream not available");
            ConnectionStatus.instance().setConnectionState(
            		hostName, ConnectionStatus.CONNECTION_DOWN);
        }
        try {
            log.debug("attempt create input stream");
            return new DataInputStream(socket.getInputStream());
        } catch (java.io.IOException ex1) {
            log.error("Exception getting input stream: "+ex1);
            return null;
        }
    }

    public void connect(String host, int port) {
        try {
            log.debug("attempt connect");
            socket = new Socket(host, port);
            opened = true;
        } catch (Exception e) {
            log.error("error opening NCE network connection: "+e);
            ConnectionStatus.instance().setConnectionState(
            		hostName, ConnectionStatus.CONNECTION_DOWN);
        }
    }

    public DataOutputStream getOutputStream() {
        if (!opened) log.error("getOutputStream called before load(), stream not available");
        try {
            return new DataOutputStream(socket.getOutputStream());
        }
     	catch (java.io.IOException e) {
            log.error("getOutputStream exception: "+e);
            ConnectionStatus.instance().setConnectionState(
            		hostName, ConnectionStatus.CONNECTION_DOWN);
     	}
     	return null;
    }

    public boolean status() {return opened;}

    /**
     * Option 2 is various filters
     */
    public String[] validOption2() { return new String[]{"Pass All", "Filtering"}; }

    /**
     * Get a String that says what Option 2 represents
     * May be an empty string, but will not be null
     */
    public String option2Name() { return "Gateway: "; }

    /**
     * Set the binary vs ASCII command set option.
     */
    public void configureOption2(String value) { mOpt2 = value; }

    public String getCurrentOption2Setting() {
        if (mOpt2 == null) return validOption2()[1];
        return mOpt2;
    }

    // private control members
    private boolean opened = false;

    static public NetworkDriverAdapter instance() {
        if (mInstance == null) mInstance = new NetworkDriverAdapter();
        return mInstance;
    }
    static NetworkDriverAdapter mInstance = null;

    Socket socket;
    
    String hostName = null;
    public void setHostName (String hostName){
    	this.hostName = hostName;
        if (this.hostName.equals("")) this.hostName = JmrixConfigPane.NONE;
     }
    
    Vector<String> portNameVector = null;
    public Vector<String> getPortNames() {
    	portNameVector = new Vector<String>();
    	portNameVector.addElement(hostName);
        return portNameVector;
    }
    public String openPort(String portName, String appName)  {
        log.error("Unexpected call to openPort");
        return null;
    }
    public String[] validBaudRates() {
        log.error("Unexpected call to validBaudRates");
        return null;
    }

    String manufacturerName = jmri.jmrix.DCCManufacturerList.OPENLCB;
    
    public String getManufacturer() { return manufacturerName; }
    public void setManufacturer(String manu) { manufacturerName=manu; }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(NetworkDriverAdapter.class.getName());

}

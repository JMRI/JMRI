/** 
 * SerialDriverAdapter.java
 *
 * Title:			SerialDriverAdapter
 * Description:		Provide access to an NCE command station via a SerialDriver attached to a serial comm port.
 *					Normally controlled by the SerialDriverFrame class.
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			
 */

package jmri.jmrix.nce.serialdriver;

import javax.comm.CommPortIdentifier;
import javax.comm.SerialPort;
import java.util.Enumeration;
import java.util.Vector;
import java.io.DataOutputStream;
import java.io.DataInputStream;

import jmri.jmrix.nce.*;

public class SerialDriverAdapter extends NcePortController  implements jmri.jmrix.SerialPortAdapter {

	Vector portNameVector = null;
	SerialPort activeSerialPort = null;
	
	public Vector getPortNames() {
		// first, check that the comm package can be opened and ports seen
		portNameVector = new Vector();
		Enumeration portIDs = CommPortIdentifier.getPortIdentifiers();
		// find the names of suitable ports
		while (portIDs.hasMoreElements()) {
		  CommPortIdentifier id = (CommPortIdentifier) portIDs.nextElement();
		  // accumulate the names in a vector
		  portNameVector.addElement(id.getName());
		  }
		return portNameVector;
	}
	
	public void openPort(String portName, String appName)  {
		// open the port, check ability to set moderators
		try {
			// get and open the primary port
			CommPortIdentifier portID = CommPortIdentifier.getPortIdentifier(portName);
  			activeSerialPort = (SerialPort) portID.open(appName, 0);  // name of program, msec to wait

			// try to set it for comunication via SerialDriver
			try {
				activeSerialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
			} catch (javax.comm.UnsupportedCommOperationException e) {
				log.error("Cannot open serial port: "+e);	
			}
			
			// disable flow control; hardware lines used for signalling, XON/XOFF might appear in data
			activeSerialPort.setFlowControlMode(0);
			// activeSerialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN | SerialPort.FLOWCONTROL_RTSCTS_OUT);

			// set RTS high, DTR high
			activeSerialPort.setRTS(true);		// not connected in some serial ports and adapters
			activeSerialPort.setDTR(true);		// pin 1 in DIN8; on main connector, this is DTR
						
			opened = true;
			
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		
		
	}

	/**
	 * set up all of the other objects to operate with an NCE command 
	 * station connected to this port
	 */
	public void configure() {
			// connect to the traffic controller
			NceTrafficController.instance().connectPort(this);
		
			// If a jmri.Programmer instance doesn't exist, create a 
			// nce.NceProgrammer to do that
			if (jmri.InstanceManager.programmerInstance() == null) 
				jmri.jmrix.nce.NceProgrammer.instance();

			// If a jmri.PowerManager instance doesn't exist, create a 
			// nce.NcePowerManager to do that
			if (jmri.InstanceManager.powerManagerInstance() == null) 
				jmri.InstanceManager.setPowerManager(new jmri.jmrix.nce.NcePowerManager());
				
			// If a jmri.TurnoutManager instance doesn't exist, create a 
			// nce.NcePowerManager to do that
			if (jmri.InstanceManager.turnoutManagerInstance() == null) 
				jmri.InstanceManager.setTurnoutManager(new jmri.jmrix.nce.NceTurnoutManager());
				
			// start operation
			// sourceThread = new Thread(p);
			// sourceThread.start();
			sinkThread = new Thread(NceTrafficController.instance());
			sinkThread.start();
	}
	
	private Thread sinkThread;

// base class methods for the NcePortController interface
	public DataInputStream getInputStream() {
		if (!opened) log.error("getInputStream called before load(), stream not available");
		try {
			return new DataInputStream(activeSerialPort.getInputStream());
     		}
     	catch (java.io.IOException e) {
     		log.error("getInputStream exception: "+e);
     	}
     	return null;
	}
	
	public DataOutputStream getOutputStream() {
		if (!opened) log.error("getOutputStream called before load(), stream not available");
		try {
     		return new DataOutputStream(activeSerialPort.getOutputStream());
     		}
     	catch (java.io.IOException e) {
     		log.error("getOutputStream exception: "+e);
     	}
     	return null;
	}
	
	public boolean status() {return opened;}
	
// private control members
	private boolean opened = false;
	
   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SerialDriverAdapter.class.getName());

}

/** 
 * MS100Adapter.java
 *
 * Title:			MS100Adapter
 * Description:		Provide access to LocoNet via a MS100 attached to a serial comm port.
 *					Normally controlled by the MS100Frame class.
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			
 */

package jmri.jmrix.loconet.ms100;

import javax.comm.CommPortIdentifier;
import javax.comm.SerialPort;
import java.util.Enumeration;
import java.util.Vector;
import java.io.DataOutputStream;
import java.io.DataInputStream;

import jmri.jmrix.loconet.LnPortController;

public class MS100Adapter extends LnPortController  {

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
		// open the primary and secondary ports in LocoNet mode, check ability to set moderators
		try {
			// get and open the primary port
			CommPortIdentifier portID = CommPortIdentifier.getPortIdentifier(portName);
  			activeSerialPort = (SerialPort) portID.open(appName, 0);  // name of program, msec to wait

			// try to set it for LocoNet direct (e.g. via MS100)
			// spec is 16600, says 16457 is OK also. Try that as a second choice
			try {
				activeSerialPort.setSerialPortParams(16600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
			} catch (javax.comm.UnsupportedCommOperationException e) {
				// assume that's a baudrate problem, fall back. Error here goes out to terminate function
				log.warn("attempting to fall back to 16457 baud after 16600 failed");
				activeSerialPort.setSerialPortParams(16457, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
			}
			
			// disable flow control; hardware lines used for signalling, XON/XOFF might appear in data
			activeSerialPort.setFlowControlMode(0);
			// activeSerialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN | SerialPort.FLOWCONTROL_RTSCTS_OUT);

			// set RTS high, DTR low to power the MS100
			activeSerialPort.setRTS(true);		// not connected in some serial ports and adapters
			activeSerialPort.setDTR(false);		// pin 1 in DIN8; on main connector, this is DTR
						
			opened = true;
			
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		
		
	}

// base class methods for the LnPortController interface
	public DataInputStream getInputStream() {
		if (!opened) log.error("called before load(), stream not available");
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

   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(MS100Frame.class.getName());
	
}

// MS100Adapter.java

package jmri.jmrix.loconet.ms100;

import javax.comm.CommPortIdentifier;
import javax.comm.PortInUseException;
import javax.comm.SerialPort;
import java.util.Enumeration;
import java.util.Vector;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.InputStream;

import jmri.jmrix.loconet.*;

/** 
 * Provide access to LocoNet via a MS100 attached to a serial comm port.
 *					Normally controlled by the MS100Frame class.
 *<P>
 * By default, this attempts to use 16600 baud. If that fails, it falls back to 16457 baud.
 * Neither the baud rate configuration nor the "option 1" option are used.
 *
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Id: MS100Adapter.java,v 1.10 2002-02-20 15:57:25 jacobsen Exp $
 */
public class MS100Adapter extends LnPortController implements jmri.jmrix.SerialPortAdapter {

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
	
	public String openPort(String portName, String appName)  {
		// open the primary and secondary ports in LocoNet mode, check ability to set moderators
		try {
			// get and open the primary port
			CommPortIdentifier portID = CommPortIdentifier.getPortIdentifier(portName);
 			try {
	  			activeSerialPort = (SerialPort) portID.open(appName, 100);  // name of program, msec to wait
	  			}
			catch (PortInUseException p) {
				log.error(portName+" port is in use: "+p.getMessage());
				return portName+" port is in use";
			}

			// try to set it for LocoNet direct (e.g. via MS100)
			// spec is 16600, says 16457 is OK also. Try that as a second choice
			try {
				activeSerialPort.setSerialPortParams(16600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
			} catch (javax.comm.UnsupportedCommOperationException e) {
				// assume that's a baudrate problem, fall back.
				log.warn("attempting to fall back to 16457 baud after 16600 failed");
				try {
					activeSerialPort.setSerialPortParams(16457, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
				} catch (javax.comm.UnsupportedCommOperationException e2) {
					log.warn("trouble setting 16600 baud");
					javax.swing.JOptionPane.showMessageDialog(null, 
		   				"Failed to set the correct baud rate for the MS100. Port is set to "
		   				+activeSerialPort.getBaudRate()+
		   				" baud. See the README file for more info.", 
		   				 "Connection failed", javax.swing.JOptionPane.ERROR_MESSAGE);
				}
			}
			
			// set RTS high, DTR low to power the MS100
			activeSerialPort.setRTS(true);		// not connected in some serial ports and adapters
			activeSerialPort.setDTR(false);		// pin 1 in DIN8; on main connector, this is DTR

			// disable flow control; hardware lines used for signalling, XON/XOFF might appear in data
			activeSerialPort.setFlowControlMode(0);
						
			// activeSerialPort.enableReceiveTimeout(1000);
			log.debug("Serial timeout was observed as: "+activeSerialPort.getReceiveTimeout()
						+" "+activeSerialPort.isReceiveTimeoutEnabled());
			
			// get and save stream
			serialStream = activeSerialPort.getInputStream();
			
			// purge contents, if any
			int count = serialStream.available();
			log.debug("input stream shows "+count+" bytes available");
			while ( count > 0) {
				serialStream.skip(count);
				count = serialStream.available();
			}
				
			// report status?
			if (log.isInfoEnabled()) {
				log.info(portName+" port opened at "
						+activeSerialPort.getBaudRate()+" baud, sees "
						+" DTR: "+activeSerialPort.isDTR()
						+" RTS: "+activeSerialPort.isRTS()
						+" DSR: "+activeSerialPort.isDSR()
						+" CTS: "+activeSerialPort.isCTS()
						+"  CD: "+activeSerialPort.isCD()
					);
			}
						
			opened = true;
			
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return null; // normal termination
	}

	/**
	 * Can the port accept additional characters?  
	 * For an MS100, this is _always_ true, as we rely on the
	 * queueing in the port itself.  But if a lot is being
	 * send, this might result in the main thread getting stalled...
	 */	
	public boolean okToSend() {
		return true;
	}

	/**
	 * set up all of the other objects to operate with a MS100
	 * connected to this port
	 */
	public void configure() {
			// connect to the traffic controller
			LnTrafficController.instance().connectPort(this);
		
			// If a jmri.Programmer instance doesn't exist, create a 
			// loconet.SlotManager to do that
			if (jmri.InstanceManager.programmerInstance() == null) 
				jmri.jmrix.loconet.SlotManager.instance();
				
			// If a jmri.PowerManager instance doesn't exist, create a 
			// loconet.LnPowerManager to do that
			if (jmri.InstanceManager.powerManagerInstance() == null) 
				jmri.InstanceManager.setPowerManager(new jmri.jmrix.loconet.LnPowerManager());

			// If a jmri.TurnoutManager instance doesn't exist, create a 
			// loconet.LnTurnoutManager to do that
			if (jmri.InstanceManager.turnoutManagerInstance() == null) 
				jmri.InstanceManager.setTurnoutManager(new jmri.jmrix.loconet.LnTurnoutManager());

			// start operation
			LnTrafficController.instance().startThreads();
	}
	
	private Thread sinkThread;

// base class methods for the LnPortController interface
	public DataInputStream getInputStream() {
		if (!opened) {
			log.error("called before load(), stream not available");
			return null;
		}
		return new DataInputStream(serialStream);
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
	
	/**
	 * Get an array of valid baud rates. This is currently just a message
	 * saying its fixed
	 */
	public String[] validBaudRates() {
		return new String[]{"fixed at 16600 baud"};
	}
	
	/**
	 * Set the baud rate.  This currently does nothing, as there's
	 * only one possible value
	 */
	public void configureBaudRate(String rate) {}
	
	/**
	 * Since option 1 is not used for this, return an array with one empty element
	 */
	public String[] validOption1() { return new String[]{""}; }
	
	/**
	 * Option 1 not used, so return a null string.
	 */
	public String option1Name() { return ""; }
	
	/**
	 * The first port option isn't used, so just ignore this call.
	 */
	public void configureOption1(String value) {}

// private control members
	private boolean opened = false;
	InputStream serialStream = null;

   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(MS100Frame.class.getName());
	
}

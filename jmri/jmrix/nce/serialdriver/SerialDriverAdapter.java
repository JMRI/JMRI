// SerialDriverAdapter.java

package jmri.jmrix.nce.serialdriver;

import javax.comm.CommPortIdentifier;
import javax.comm.PortInUseException;
import javax.comm.SerialPort;
import java.util.Enumeration;
import java.util.Vector;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.InputStream;

import jmri.jmrix.nce.*;

/**
 * Implements SerialPortAdapter for the NCE system.  This connects
 * an NCE command station via a serial com port.
 * Normally controlled by the SerialDriverFrame class.
 * <P>
 * The current implementation only handles the 19,200 baud rate, and does
 * not use any other options at configuration time.
 *
 * @author			Bob Jacobsen   Copyright (C) 2001, 2002
 * @version			$Revision: 1.4 $
 */
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

	public String openPort(String portName, String appName)  {
		// open the port, check ability to set moderators
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

			// try to set it for comunication via SerialDriver
			try {
				activeSerialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
			} catch (javax.comm.UnsupportedCommOperationException e) {
				log.error("Cannot set serial parameters on port "+portName+": "+e.getMessage());
				return "Cannot set serial parameters on port "+portName+": "+e.getMessage();
			}

			// set RTS high, DTR high
			activeSerialPort.setRTS(true);		// not connected in some serial ports and adapters
			activeSerialPort.setDTR(true);		// pin 1 in DIN8; on main connector, this is DTR

			// disable flow control; hardware lines used for signalling, XON/XOFF might appear in data
			activeSerialPort.setFlowControlMode(0);

			// set timeout
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
			log.error("Unexpected exception while opening port "+portName+" trace follows: "+ex);
			ex.printStackTrace();
			return "Unexpected error while opening port "+portName+": "+ex;
		}

		return null; // indicates OK return

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
		if (!opened) {
			log.error("getInputStream called before load(), stream not available");
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
	 * Get an array of valid baud rates. This is currently only 19,200 bps
	 */
	public String[] validBaudRates() {
		return new String[]{"9,600 bps"};
	}

	/**
	 * Set the baud rate.  This currently does nothing, as there's
	 * only one possible value
	 */
	public void configureBaudRate(String rate) {}

	/**
	 * Since option 1 is not used for this, return an array with just a single string
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

        /**
	 * Get an array of valid values for "option 2"; used to display valid options.
	 * May not be null, but may have zero entries
	 */
	public String[] validOption2() { return new String[]{""}; }

	/**
	 * Get a String that says what Option 2 represents
	 * May be an empty string, but will not be null
	 */
	public String option2Name() { return ""; }

	/**
	 * Set the second port option.  Only to be used after construction, but
	 * before the openPort call
	 */
	public void configureOption2(String value) throws jmri.jmrix.SerialConfigException {}


// private control members
	private boolean opened = false;
	InputStream serialStream = null;

   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SerialDriverAdapter.class.getName());

}

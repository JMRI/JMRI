/**
 * MX1Adapter.java
 *
 * Title:		MX1Adapter
 * Description:		Provide access to Zimo's MX-1 on an attached serial comm port.
 *			Normally controlled by the zimo.mx1.Mx1Frame class.
 * @author		Bob Jacobsen   Copyright (C) 2002
 * @version		$Revision: 1.2 $
 *
 * Adapted for use with Zimo MX-1 by Sip Bosch
 *
 */

package jmri.jmrix.zimo.mx1;

import jmri.jmrix.zimo.Mx1CommandStation;
import jmri.jmrix.zimo.Mx1Packetizer;
import jmri.jmrix.zimo.Mx1PortController;

import javax.comm.CommPortIdentifier;
import javax.comm.PortInUseException;
import javax.comm.SerialPortEventListener;
import javax.comm.SerialPortEvent;
import javax.comm.SerialPort;

import java.util.Enumeration;
import java.util.Vector;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.InputStream;

import jmri.jmrix.zimo.*;

public class Mx1Adapter extends Mx1PortController implements jmri.jmrix.SerialPortAdapter {

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
		// open the port in MX-1 mode, check ability to set moderators
		try {
			// get and open the primary port
			CommPortIdentifier portID = CommPortIdentifier.getPortIdentifier(portName);
			try {
	  			activeSerialPort = (SerialPort) portID.open(appName, 100);  // name of program, msec to wait
	  			}
			catch (PortInUseException p) {
				return handlePortBusy(p, portName, log);
			}
			// try to set it for Can Net
			try {
				setSerialPort();
			} catch (javax.comm.UnsupportedCommOperationException e) {
				log.error("Cannot set serial parameters on port "+portName+": "+e.getMessage());
				return "Cannot set serial parameters on port "+portName+": "+e.getMessage();
			}

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
				// report now
				log.info(portName+" port opened at "
						+activeSerialPort.getBaudRate()+" baud with"
						+" DTR: "+activeSerialPort.isDTR()
						+" RTS: "+activeSerialPort.isRTS()
						+" DSR: "+activeSerialPort.isDSR()
						+" CTS: "+activeSerialPort.isCTS()
						+"  CD: "+activeSerialPort.isCD()
					);
			}
			if (log.isDebugEnabled()) {
				// report additional status
				log.debug(" port flow control shows "+
							(activeSerialPort.getFlowControlMode()==SerialPort.FLOWCONTROL_RTSCTS_OUT?"hardware flow control":"no flow control"));
			}
			if (log.isDebugEnabled()) {
				// arrange to notify later
				activeSerialPort.addEventListener(new SerialPortEventListener(){
						public void serialEvent(SerialPortEvent e) {
							int type = e.getEventType();
							switch (type) {
								case SerialPortEvent.DATA_AVAILABLE:
									log.info("SerialEvent: DATA_AVAILABLE is "+e.getNewValue());
									return;
								case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
									log.info("SerialEvent: OUTPUT_BUFFER_EMPTY is "+e.getNewValue());
									return;
								case SerialPortEvent.CTS:
									log.info("SerialEvent: CTS is "+e.getNewValue());
									return;
								case SerialPortEvent.DSR:
									log.info("SerialEvent: DSR is "+e.getNewValue());
									return;
								case SerialPortEvent.RI:
									log.info("SerialEvent: RI is "+e.getNewValue());
									return;
								case SerialPortEvent.CD:
									log.info("SerialEvent: CD is "+e.getNewValue());
									return;
								case SerialPortEvent.OE:
									log.info("SerialEvent: OE (overrun error) is "+e.getNewValue());
									return;
								case SerialPortEvent.PE:
									log.info("SerialEvent: PE (parity error) is "+e.getNewValue());
									return;
								case SerialPortEvent.FE:
									log.info("SerialEvent: FE (framing error) is "+e.getNewValue());
									return;
								case SerialPortEvent.BI:
									log.info("SerialEvent: BI (break interrupt) is "+e.getNewValue());
									return;
								default:
									log.info("SerialEvent of unknown type: "+type+" value: "+e.getNewValue());
									return;
							}
						}
					}
				);
				try { activeSerialPort.notifyOnFramingError(true); }
					catch (Exception e) { log.debug("Could not notifyOnFramingError: "+e); }

				try { activeSerialPort.notifyOnBreakInterrupt(true); }
					catch (Exception e) { log.debug("Could not notifyOnBreakInterrupt: "+e); }

				try { activeSerialPort.notifyOnParityError(true); }
					catch (Exception e) { log.debug("Could not notifyOnParityError: "+e); }

				try { activeSerialPort.notifyOnOverrunError(true); }
					catch (Exception e) { log.debug("Could not notifyOnOverrunError: "+e); }

			}

			opened = true;

                }  catch (javax.comm.NoSuchPortException p) {
                     return handlePortNotFound(p, portName, log);
                }  catch (Exception ex) {
			log.error("Unexpected exception while opening port "+portName+" trace follows: "+ex);
			ex.printStackTrace();
			return "Unexpected error while opening port "+portName+": "+ex;
		}

		return null; // normal operation
	}

	/**
	 * Can the port accept additional characters?
	 * The state of CTS determines this, as there seems to
	 * be no way to check the number of queued bytes and buffer length.
	 * This might
	 * go false for short intervals, but it might also stick
	 * off if something goes wrong.
	 */
	public boolean okToSend() {
		return activeSerialPort.isCTS();
	}

	/**
	 * set up all of the other objects to operate with a MX-1
	 * connected to this port
	 */
	public void configure() {
          // connect to a packetizing traffic controller
            Mx1Packetizer packets = new Mx1Packetizer(new Mx1CommandStation());
            packets.connectPort(this);

            jmri.jmrix.zimo.Mx1Programmer.instance();
            jmri.InstanceManager.setPowerManager(new jmri.jmrix.zimo.Mx1PowerManager());
            //jmri.InstanceManager.setTurnoutManager(new jmri.jmrix.zimo.Mx1TurnoutManager());
            //jmri.InstanceManager.setThrottleManager(new jmri.jmrix.zimo.Mx1ThrottleManager());

             // start operation
             packets.startThreads();

             jmri.jmrix.zimo.ActiveFlag.setActive();

	}

	private Thread sinkThread;

// base class methods for the ZimoPortController interface
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
     		log.error("getOutputStream exception: "+e.getMessage());
     	}
     	return null;
	}

	public boolean status() {return opened;}

	/**
	 * Local method to do specific configuration
	 */
	protected void setSerialPort() throws javax.comm.UnsupportedCommOperationException {
		// find the baud rate value, configure comm options
		int baud = validSpeedValues[0];  // default, but also defaulted in the initial value of selectedSpeed
		for (int i = 0; i<validSpeeds.length; i++ )
			if (validSpeeds[i].equals(mBaudRate))
				baud = validSpeedValues[i];
		activeSerialPort.setSerialPortParams(baud, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

		// set RTS high, DTR high - done early, so flow control can be configured after
		activeSerialPort.setRTS(true);		// not connected in some serial ports and adapters
		activeSerialPort.setDTR(true);		// pin 1 in DIN8; on main connector, this is DTR

		// find and configure flow control
		int flow = SerialPort.FLOWCONTROL_RTSCTS_OUT; // default, but also defaults in selectedOption1
		if (selectedOption1.equals(validOption1[1]))
			flow = 0;
		activeSerialPort.setFlowControlMode(flow);
	}

	/**
	 * Get an array of valid baud rates. This is currently just a message
	 * saying its fixed
	 */
	public String[] validBaudRates() {
		return validSpeeds;
	}

        /**
         * Option 1 controls flow control option
         */
        public String option1Name() { return "MX-1 connections uses "; }
        public String[] validOption1() { return validOption1; }

        protected String [] validSpeeds = new String[]{"1,200 baud", "2,400 baud", "4,800 baud",
        "9,600 baud", "19,200 baud", "38,400 baud"};
        protected int [] validSpeedValues = new int[]{1200, 2400, 4800, 9600, 19200, 38400};

        // meanings are assigned to these above, so make sure the order is consistent
        protected String [] validOption1 = new String[]{"hardware flow control (recommended)", "no flow control"};
        protected String selectedOption1=validOption1[0];

        private boolean opened = false;
        InputStream serialStream = null;

        static public Mx1Adapter instance() {
            if (mInstance == null) mInstance = new Mx1Adapter();
            return mInstance;
        }
        static Mx1Adapter mInstance = null;

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(Mx1Adapter.class.getName());

}

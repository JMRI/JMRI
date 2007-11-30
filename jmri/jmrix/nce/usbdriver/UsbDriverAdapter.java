// UsbDriverAdapter.java

package jmri.jmrix.nce.usbdriver;

import jmri.jmrix.nce.NceMessage;
import jmri.jmrix.nce.NcePortController;
import jmri.jmrix.nce.NceProgrammer;
import jmri.jmrix.nce.NceProgrammerManager;
import jmri.jmrix.nce.NceSensorManager;
import jmri.jmrix.nce.NceTrafficController;
import jmri.jmrix.nce.NceUSB;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Vector;

import javax.comm.CommPortIdentifier;
import javax.comm.PortInUseException;
import javax.comm.SerialPort;

/**
 * Implements UsbPortAdapter for the NCE system.
 * <P>
 * This connects an NCE PowerCab or PowerHouse via a USB port. Normally
 * controlled by the UsbDriverFrame class.
 * <P>
 * 
 * 
 * @author Bob Jacobsen Copyright (C) 2001, 2002
 * @author Daniel Boudreau Copyright (C) 2007
 * @version $Revision: 1.2 $
 */
public class UsbDriverAdapter extends NcePortController  implements jmri.jmrix.SerialPortAdapter {

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
            } catch (PortInUseException p) {
                return handlePortBusy(p, portName, log);
            }

            // try to set it for comunication via SerialDriver
            try {
                // find the baud rate value, configure comm options
                int baud = validSpeedValues[0];  // default, but also defaulted in the initial value of selectedSpeed
                for (int i = 0; i<validSpeeds.length; i++ )
                    if (validSpeeds[i].equals(mBaudRate))
                        baud = validSpeedValues[i];
                activeSerialPort.setSerialPortParams(baud, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            } catch (javax.comm.UnsupportedCommOperationException e) {
                log.error("Cannot set serial parameters on port "+portName+": "+e.getMessage());
                return "Cannot set serial parameters on port "+portName+": "+e.getMessage();
            }

            // set RTS high, DTR high
            activeSerialPort.setRTS(true);		// not connected in some serial ports and adapters
            activeSerialPort.setDTR(true);		// pin 1 in DIN8; on main connector, this is DTR

            // disable flow control; hardware lines used for signaling, XON/XOFF might appear in data
            activeSerialPort.setFlowControlMode(0);
            activeSerialPort.enableReceiveTimeout(50);  // 50 mSec timeout before sending chars

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
                log.info(portName+" USB port opened at "
                         +activeSerialPort.getBaudRate()+" baud, sees "
                         +" DTR: "+activeSerialPort.isDTR()
                         +" RTS: "+activeSerialPort.isRTS()
						 +" DSR: "+activeSerialPort.isDSR()
                         +" CTS: "+activeSerialPort.isCTS()
                         +"  CD: "+activeSerialPort.isCD()
                         );
            }

            opened = true;

        } catch (javax.comm.NoSuchPortException p) {
            return handlePortNotFound(p, portName, log);
        } catch (Exception ex) {
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

        jmri.InstanceManager.setProgrammerManager(
                new NceProgrammerManager(
                    new NceProgrammer()));

        jmri.InstanceManager.setPowerManager(new jmri.jmrix.nce.NcePowerManager());

        jmri.InstanceManager.setTurnoutManager(new jmri.jmrix.nce.NceTurnoutManager());

        NceSensorManager s;
        jmri.InstanceManager.setSensorManager(s = new jmri.jmrix.nce.NceSensorManager());
        NceTrafficController.instance().setSensorManager(s);

        jmri.InstanceManager.setThrottleManager(new jmri.jmrix.nce.NceThrottleManager());

        jmri.jmrix.nce.ActiveFlag.setActive();

        // set binary mode
		NceMessage.setCommandOptions(NceMessage.OPTION_2006);

		// set the system the USB is connected to
		if (getCurrentOption1Setting().equals(validOption1()[0])) {
			NceUSB.setUsbSystem(NceUSB.USB_SYSTEM_POWERCAB);
		} else if (getCurrentOption1Setting().equals(validOption1()[1])) {
			NceUSB.setUsbSystem(NceUSB.USB_SYSTEM_SB3);
		} else{
			NceUSB.setUsbSystem(NceUSB.USB_SYSTEM_POWERHOUSE);
		}

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
     * Get an array of valid baud rates.
     */
	public String[] validBaudRates() {
		return validSpeeds;
	}

	protected String [] validSpeeds = new String[]{"9,600 baud", "19,200 baud"};
	protected int [] validSpeedValues = new int[]{9600, 19200};

    /**
     * Option 1 system type.
     */
    public String[] validOption1() { return new String[]{"PowerCab", "Smart Booster SB3", "PowerHouse"}; }

    /**
     * Get a String that says what Option 1 represents
     * May be an empty string, but will not be null
     */
    public String option1Name() { return "System:"; }

    /**
     * Set the system type.
     */
    public void configureOption1(String value) { mOpt1 = value; }
    protected String mOpt1 = null;
    public String getCurrentOption1Setting() {
        if (mOpt1 == null) return validOption1()[0];
        return mOpt1;
    }

    // private control members
    private boolean opened = false;
    InputStream serialStream = null;

    static public UsbDriverAdapter instance() {
        if (mInstance == null) mInstance = new UsbDriverAdapter();
        return mInstance;
    }
    static UsbDriverAdapter mInstance = null;

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(UsbDriverAdapter.class.getName());

}

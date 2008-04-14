// SerialDriverAdapter.java

package jmri.jmrix.acela.serialdriver;

import jmri.jmrix.acela.AcelaPortController;
import jmri.jmrix.acela.AcelaSensorManager;
import jmri.jmrix.acela.AcelaTrafficController;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Vector;

import javax.comm.CommPortIdentifier;
import javax.comm.PortInUseException;
import javax.comm.SerialPort;

/**
 * Implements SerialPortAdapter for the Acela system.  This connects
 * an Acela interface to the CTI network via a serial com port.
 * Normally controlled by the SerialDriverFrame class.
 * <P>
 * The current implementation only handles the 9,600 baud rate, and does
 * not use any other options at configuration time.
 *
 * @author	Bob Jacobsen   Copyright (C) 2001, 2002
 * @version	$Revision: 1.2 $
 *
 * @author	Bob Coleman, Copyright (C) 2007, 2008
 *              Based on Mrc example, modified to establish Acela support. 
 */

public class SerialDriverAdapter extends AcelaPortController  implements jmri.jmrix.SerialPortAdapter {

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
                activeSerialPort = (SerialPort) portID.open(appName, 2000);  // name of program, msec to wait
            } catch (PortInUseException p) {
                return handlePortBusy(p, portName, log);
            }

            // try to set it for communication via SerialDriver
            try {
                activeSerialPort.setSerialPortParams(currentBaudNumber(getCurrentBaudRate()), SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            } catch (javax.comm.UnsupportedCommOperationException e) {
                log.error("Cannot set serial parameters on port "+portName+": "+e.getMessage());
                return "Cannot set serial parameters on port "+portName+": "+e.getMessage();
            }

            // set RTS high, DTR high
            activeSerialPort.setRTS(false);		// not connected in some serial ports and adapters
            activeSerialPort.setDTR(false);		// pin 1 in DIN8; on main connector, this is DTR

            // disable flow control; hardware lines used for signaling, XON/XOFF might appear in data
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
     * set up all of the other objects to operate with an serial command
     * station connected to this port
     */
    public void configure() {
        // connect to the traffic controller
        AcelaTrafficController.instance().connectPort(this);

        // connect to a packetizing traffic controller
        // LnPacketizer packets = new LnPacketizer();
        // packets.connectPort(this);

        // do the common manager config
        // configureManagers();
   	jmri.InstanceManager.setLightManager(new jmri.jmrix.acela.AcelaLightManager());

        AcelaSensorManager s;
        jmri.InstanceManager.setSensorManager(s = new jmri.jmrix.acela.AcelaSensorManager());
        AcelaTrafficController.instance().setSensorManager(s);	

        // start operation
        // packets.startThreads();
        jmri.jmrix.acela.ActiveFlag.setActive();
    }

    private Thread sinkThread;

    // base class methods for the AcelaPortController interface
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
//	Really just want 9600 Baud for Acela
//      return new String[]{"9,600 bps", "19,200 bps", "38,400 bps", "57,600 bps"};
        return new String[]{"9,600 bps"};
    }

    /**
     * Return array of valid baud rates as integers.
     */
    public int[] validBaudNumber() {
//	Really just want 9600 Baud for Acela
//      return new int[]{9600, 19200, 38400, 57600};
        return new int[]{9600};
    }

    // private control members
    private boolean opened = false;
    InputStream serialStream = null;

    static public SerialDriverAdapter instance() {
        if (mInstance == null) mInstance = new SerialDriverAdapter();
        return mInstance;
    }
    static SerialDriverAdapter mInstance = null;

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SerialDriverAdapter.class.getName());
}

/* @(#)SerialDriverAdapter.java */
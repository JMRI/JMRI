// SerialDriverAdapter.java

package jmri.jmrix.easydcc.serialdriver;

import jmri.jmrix.easydcc.EasyDccPortController;
import jmri.jmrix.easydcc.EasyDccProgrammer;
import jmri.jmrix.easydcc.EasyDccProgrammerManager;
import jmri.jmrix.easydcc.EasyDccTrafficController;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Vector;

import javax.comm.CommPortIdentifier;
import javax.comm.PortInUseException;
import javax.comm.SerialPort;

/**
 * Implements SerialPortAdapter for the EasyDcc system.  This connects
 * an EasyDcc command station via a serial com port.
 * Normally controlled by the SerialDriverFrame class.
 * <P>
 * The current implementation only handles the 19,200 baud rate, and does
 * not use any other options at configuration time.
 *
 * @author	Bob Jacobsen   Copyright (C) 2001, 2002
 * @version	$Revision: 1.10 $
 */
public class SerialDriverAdapter extends EasyDccPortController  implements jmri.jmrix.SerialPortAdapter {

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
                return handlePortBusy(p, portName, log);
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
     * set up all of the other objects to operate with an EasyDcc command
     * station connected to this port
     */
    public void configure() {
        // connect to the traffic controller
        EasyDccTrafficController.instance().connectPort(this);

        jmri.InstanceManager.setProgrammerManager(
                new EasyDccProgrammerManager(
                    new EasyDccProgrammer()));

        jmri.InstanceManager.setPowerManager(new jmri.jmrix.easydcc.EasyDccPowerManager());

        jmri.InstanceManager.setTurnoutManager(new jmri.jmrix.easydcc.EasyDccTurnoutManager());

        // start operation
        // sourceThread = new Thread(p);
        // sourceThread.start();
        sinkThread = new Thread(EasyDccTrafficController.instance());
        sinkThread.start();
    }

    private Thread sinkThread;

    // base class methods for the EasyDccPortController interface
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

    // private control members
    private boolean opened = false;
    InputStream serialStream = null;

    static public SerialDriverAdapter instance() {
        if (mInstance == null) mInstance = new SerialDriverAdapter();
        return mInstance;
    }
    static SerialDriverAdapter mInstance = null;
    static public boolean hasInstance() {
        return (mInstance != null);
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SerialDriverAdapter.class.getName());

}

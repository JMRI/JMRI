// SerialDriverAdapter.java
package jmri.jmrix.direct.serial;

import Serialio.SerInputStream;
import Serialio.SerOutputStream;
import Serialio.SerialConfig;
import Serialio.SerialPortLocal;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Vector;
import jmri.jmrix.direct.PortController;
import jmri.jmrix.direct.TrafficController;
import jmri.util.SystemType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements SerialPortAdapter for direct serial drive
 * <P>
 * Normally controlled by the SerialDriverFrame class.
 * <P>
 * The current implementation only handles the 19,200 baud rate, and does not
 * use any other options at configuration time.
 *
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2002, 2004
 * @version	$Revision$
 */
public class SerialDriverAdapter extends PortController implements jmri.jmrix.SerialPortAdapter {

    Vector<String> portNameVector = null;
    SerialPort activeSerialPort = null;

    public Vector<String> getPortNames() {
        portNameVector = null;
        try {
            // this has to work through one of two sets of class. If
            // Serialio.SerialConfig exists on this machine, we use that
            // else we revert to gnu.io
            try {
                if (SystemType.isWindows() && Double.valueOf(System.getProperty("os.version")) >= 6) {
                    throw new Exception("Direct interface not compatible.");
                }
                Class.forName("Serialio.SerialConfig");
                log.debug("openPort using SerialIO");
                InnerSerial inner = new InnerSerial();
                inner.getPortNames();
            } catch (ClassNotFoundException e) {
                log.debug("openPort using gnu.io");
                InnerJavaComm inner = new InnerJavaComm();
                inner.getPortNames();
            } catch (java.lang.UnsatisfiedLinkError e) {
                log.debug("openPort using gnu.io");
                InnerJavaComm inner = new InnerJavaComm();
                inner.getPortNames();
            }
        } catch (Exception ex) {
            log.error("error listing port names");
            ex.printStackTrace();
        }

        return portNameVector;
    }

    class InnerSerial {

        public Vector<String> getPortNames() {
            // first, check that the comm package can be opened and ports seen
            portNameVector = new Vector<String>();
            try {
                String[] names = SerialPortLocal.getPortList();
                // accumulate the names in a vector
                for (int i = 0; i < names.length; i++) {
                    portNameVector.addElement(names[i]);
                }
            } catch (java.io.IOException e) {
                log.error("IO exception listing ports: " + e);
            }
            return portNameVector;
        }

        public String openPort(String portName, String appName) throws java.io.IOException {
            // get and open the primary port
            SerialConfig config = new SerialConfig(portName);

            // try to set it for 16457 baud, then fall back if needed
            config.setBitRate(16457);
            config.setDataBits(SerialConfig.LN_8BITS);
            config.setStopBits(SerialConfig.ST_1BITS);
            config.setParity(SerialConfig.PY_NONE);
            config.setHandshake(SerialConfig.HS_NONE);
            Serialio.SerialPort activeSerialPort = new SerialPortLocal(config);

            // set RTS high, DTR low to power the MS100
            activeSerialPort.setRTS(true);		// not connected in some serial ports and adapters
            activeSerialPort.setDTR(false);		// pin 1 in DIN8; on main connector, this is DTR

            // get and save stream
            serialInStream = new SerInputStream(activeSerialPort);
            serialOutStream = new SerOutputStream(activeSerialPort);

            // report status
            if (log.isInfoEnabled()) {
                log.info(portName + " port opened, sees "
                        + " DSR: " + activeSerialPort.sigDSR()
                        + " CTS: " + activeSerialPort.sigCTS()
                        + "  CD: " + activeSerialPort.sigCD()
                );
            }
            return null;
        }
    }

    class InnerJavaComm {

        @SuppressWarnings("unchecked")
        public Vector<String> getPortNames() {
            // first, check that the comm package can be opened and ports seen
            portNameVector = new Vector<String>();
            Enumeration<CommPortIdentifier> portIDs = CommPortIdentifier.getPortIdentifiers();
            // find the names of suitable ports
            while (portIDs.hasMoreElements()) {
                CommPortIdentifier id = portIDs.nextElement();
                // accumulate the names in a vector
                portNameVector.addElement(id.getName());
            }
            return portNameVector;
        }

        public String openPort(String portName, String appName) throws gnu.io.NoSuchPortException, gnu.io.UnsupportedCommOperationException,
                java.io.IOException {
            // get and open the primary port
            CommPortIdentifier portID = CommPortIdentifier.getPortIdentifier(portName);
            gnu.io.SerialPort activeSerialPort = null;
            try {
                activeSerialPort = (SerialPort) portID.open(appName, 2000);  // name of program, msec to wait
            } catch (PortInUseException p) {
                return handlePortBusy(p, portName, log);
            }

            // try to set it for 17240, then 16457 baud, then 19200 if needed
            try {
                activeSerialPort.setSerialPortParams(17240, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            } catch (gnu.io.UnsupportedCommOperationException e) {
                // assume that's a baudrate problem, fall back.
                log.warn("attempting to fall back to 16457 baud after 17240 failed");
                try {
                    activeSerialPort.setSerialPortParams(16457, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
                } catch (gnu.io.UnsupportedCommOperationException e2) {
                    log.warn("trouble setting 16457 baud");
                    activeSerialPort.setSerialPortParams(19200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
                    javax.swing.JOptionPane.showMessageDialog(null,
                            "Failed to set the correct baud rate. Port is set to "
                            + activeSerialPort.getBaudRate()
                            + " baud. See the README file for more info.",
                            "Connection failed", javax.swing.JOptionPane.ERROR_MESSAGE);
                }
            }

            // set RTS high, DTR low in case power is needed
            activeSerialPort.setRTS(true);          // not connected in some serial ports and adapters
            activeSerialPort.setDTR(false);         // pin 1 in DIN8; on main connector, this is DTR

            // disable flow control; hardware lines used for signaling, XON/XOFF might appear in data
            activeSerialPort.setFlowControlMode(0);

            // activeSerialPort.enableReceiveTimeout(1000);
            log.debug("Serial timeout was observed as: " + activeSerialPort.getReceiveTimeout()
                    + " " + activeSerialPort.isReceiveTimeoutEnabled());

            // get and save stream
            serialInStream = activeSerialPort.getInputStream();
            serialOutStream = activeSerialPort.getOutputStream();

            // report status?
            if (log.isInfoEnabled()) {
                log.info(portName + " port opened at "
                        + activeSerialPort.getBaudRate() + " baud, sees "
                        + " DTR: " + activeSerialPort.isDTR()
                        + " RTS: " + activeSerialPort.isRTS()
                        + " DSR: " + activeSerialPort.isDSR()
                        + " CTS: " + activeSerialPort.isCTS()
                        + "  CD: " + activeSerialPort.isCD()
                );
            }
            return null;
        }
    }

    public String openPort(String portName, String appName) {
        try {
            // this has to work through one of two sets of class. If
            // Serialio.SerialConfig exists on this machine, we use that
            // else we revert to gnu.io
            try {
                Class.forName("Serialio.SerialConfig");
                log.debug("openPort using SerialIO");
                InnerSerial inner = new InnerSerial();
                String result = inner.openPort(portName, appName);
                if (result != null) {
                    return result;
                }
            } catch (ClassNotFoundException e) {
                log.debug("openPort using gnu.io");
                InnerJavaComm inner = new InnerJavaComm();
                String result = inner.openPort(portName, appName);
                if (result != null) {
                    return result;
                }
            } catch (java.lang.UnsatisfiedLinkError e) {
                log.debug("openPort using gnu.io");
                InnerJavaComm inner = new InnerJavaComm();
                String result = inner.openPort(portName, appName);
                if (result != null) {
                    return result;
                }
            }

            // port is open, regardless of method, start work on the stream
            // purge contents, if any
            int count = serialInStream.available();
            log.debug("input stream shows " + count + " bytes available");
            while (count > 0) {
                serialInStream.skip(count);
                count = serialInStream.available();
            }

            opened = true;

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null; // normal termination
    }

    /**
     * set up all of the other objects to operate with direct drive on this port
     */
    public void configure() {
        // connect to the traffic controller
        TrafficController.instance().connectPort(this);

        // initialize any managers this protocol provides
        jmri.InstanceManager.setCommandStation(TrafficController.instance());

        // mention as available
        jmri.jmrix.direct.ActiveFlag.setActive();

    }

    // base class methods for the PortController interface
    public DataInputStream getInputStream() {
        if (!opened) {
            log.error("getInputStream called before load(), stream not available");
            return null;
        }
        return new DataInputStream(serialInStream);
    }

    public DataOutputStream getOutputStream() {
        if (!opened) {
            log.error("getOutputStream called before load(), stream not available");
        }
        return new DataOutputStream(serialOutStream);
    }

    public boolean status() {
        return opened;
    }

    /**
     * Get an array of valid baud rates. This is currently only 19,200 bps
     */
    public String[] validBaudRates() {
        return new String[]{"19,200 bps"};
    }

    // private control members
    private boolean opened = false;
    InputStream serialInStream = null;
    OutputStream serialOutStream = null;

    static public SerialDriverAdapter instance() {
        if (mInstance == null) {
            mInstance = new SerialDriverAdapter();
        }
        return mInstance;
    }
    static SerialDriverAdapter mInstance = null;

    private final static Logger log = LoggerFactory.getLogger(SerialDriverAdapter.class.getName());

}

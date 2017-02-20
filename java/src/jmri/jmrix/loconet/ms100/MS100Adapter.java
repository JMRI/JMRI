package jmri.jmrix.loconet.ms100;

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
import jmri.jmrix.loconet.LnPacketizer;
import jmri.jmrix.loconet.LnPortController;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.util.SystemType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide access to LocoNet via a MS100 attached to a serial comm port.
 * Normally controlled by the MS100Frame class.
 * <P>
 * By default, this attempts to use 16600 baud. If that fails, it falls back to
 * 16457 baud. Neither the baud rate configuration nor the "option 1" option are
 * used.
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 */
public class MS100Adapter extends LnPortController implements jmri.jmrix.SerialPortAdapter {

    public MS100Adapter() {
        super(new LocoNetSystemConnectionMemo());
        option2Name = "CommandStation"; // NOI18N
        option3Name = "TurnoutHandle"; // NOI18N
        options.put(option2Name, new Option("Command station type:", commandStationNames, false));
        options.put(option3Name, new Option("Turnout command handling:", new String[]{"Normal", "Spread", "One Only", "Both"}));
    }

    Vector<String> portNameVector = null;

    @Override
    public Vector<String> getPortNames() {
        portNameVector = null;
        try {
            // this has to work through one of two sets of class. If
            // Serialio.SerialConfig exists on this machine and we're
            // running on Windows XP or earlier, we use that
            // else we revert to gnu.io
            try {
                if (SystemType.isWindows() && Double.valueOf(System.getProperty("os.version")) >= 6) { // NOI18N
                    throw new Exception("MS100 interface not compatible."); // NOI18N
                }
                Class.forName("Serialio.SerialConfig"); // NOI18N
                log.debug("openPort using SerialIO"); // NOI18N
                InnerSerial inner = new InnerSerial();
                inner.getPortNames();
            } catch (ClassNotFoundException e) {
                log.debug("openPort using gnu.io"); // NOI18N
                InnerJavaComm inner = new InnerJavaComm();
                inner.getPortNames();
            }
        } catch (Exception ex) {
            log.error("error listing port names"); // NOI18N
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
                log.error("IO exception listing ports: " + e); // NOI18N
            } catch (java.lang.UnsatisfiedLinkError e) {
                log.error("Exception listing ports: " + e); // NOI18N
            }
            return portNameVector;
        }

        public String openPort(String portName, String appName) throws java.io.IOException {
            // get and open the primary port
            SerialConfig config = new SerialConfig(portName);

            // try to set it for LocoNet direct (e.g. via MS100)
            // spec is 16600, says 16457 is OK also. We start with 16600,
            // attempting to make that work.
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
                log.info(portName + " port opened, sees " // NOI18N
                        + " DSR: " + activeSerialPort.sigDSR() // NOI18N
                        + " CTS: " + activeSerialPort.sigCTS() // NOI18N
                        + "  CD: " + activeSerialPort.sigCD() // NOI18N
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
                // filter out line printers 
                if (id.getPortType() != CommPortIdentifier.PORT_PARALLEL) // accumulate the names in a vector
                {
                    portNameVector.addElement(id.getName());
                }
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

            // try to set it for LocoNet direct (e.g. via MS100)
            // spec is 16600, says 16457 is OK also. Try that as a second choice
            try {
                activeSerialPort.setSerialPortParams(16600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            } catch (gnu.io.UnsupportedCommOperationException e) {
                // assume that's a baudrate problem, fall back.
                log.warn("attempting to fall back to 16457 baud after 16600 failed"); // NOI18N
                try {
                    activeSerialPort.setSerialPortParams(16457, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
                } catch (gnu.io.UnsupportedCommOperationException e2) {
                    log.warn("trouble setting 16600 baud"); // NOI18N
                    javax.swing.JOptionPane.showMessageDialog(null,
                            "Failed to set the correct baud rate for the MS100. Port is set to "
                            + activeSerialPort.getBaudRate()
                            + " baud. See the README file for more info.",
                            "Connection failed", javax.swing.JOptionPane.ERROR_MESSAGE);
                }
            }

            // set RTS high, DTR low to power the MS100
            activeSerialPort.setRTS(true);          // not connected in some serial ports and adapters
            activeSerialPort.setDTR(false);         // pin 1 in DIN8; on main connector, this is DTR

            // disable flow control; hardware lines used for signaling, XON/XOFF might appear in data
            activeSerialPort.setFlowControlMode(0);

            // set timeout
            try {
                activeSerialPort.enableReceiveTimeout(10);
                log.debug("Serial timeout was observed as: " + activeSerialPort.getReceiveTimeout() // NOI18N
                        + " " + activeSerialPort.isReceiveTimeoutEnabled());
            } catch (Exception et) {
                log.info("failed to set serial timeout: " + et); // NOI18N
            }

            // get and save stream
            serialInStream = activeSerialPort.getInputStream();
            serialOutStream = activeSerialPort.getOutputStream();

            // report status?
            if (log.isInfoEnabled()) {
                log.info(portName + " port opened at " // NOI18N
                        + activeSerialPort.getBaudRate() + " baud, sees " // NOI18N
                        + " DTR: " + activeSerialPort.isDTR() // NOI18N
                        + " RTS: " + activeSerialPort.isRTS() // NOI18N
                        + " DSR: " + activeSerialPort.isDSR() // NOI18N
                        + " CTS: " + activeSerialPort.isCTS() // NOI18N
                        + "  CD: " + activeSerialPort.isCD() // NOI18N
                );
            }
            return null;
        }
    }

    @Override
    public String openPort(String portName, String appName) {
        try {
            // this has to work through one of two sets of class. If
            // Serialio.SerialConfig exists on this machine, we use that
            // else we revert to gnu.io
            try {
                Class.forName("Serialio.SerialConfig"); // NOI18N
                log.debug("openPort using SerialIO"); // NOI18N
                InnerSerial inner = new InnerSerial();
                String result = inner.openPort(portName, appName);
                if (result != null) {
                    return result;
                }
            } catch (ClassNotFoundException e) {
                log.debug("openPort using gnu.io"); // NOI18N
                InnerJavaComm inner = new InnerJavaComm();
                String result = inner.openPort(portName, appName);
                if (result != null) {
                    return result;
                }
            }

            // port is open, regardless of method, start work on the stream

            // purge contents, if any
            purgeStream(serialInStream);

            opened = true;

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null; // normal termination
    }

    /**
     * set up all of the other objects to operate with a MS100 connected to this
     * port
     */
    @Override
    public void configure() {

        setCommandStationType(getOptionState(option2Name));
        setTurnoutHandling(getOptionState(option3Name));
        // connect to a packetizing traffic controller
        LnPacketizer packets = new LnPacketizer();
        packets.connectPort(this);

        // create memo
        this.getSystemConnectionMemo().setLnTrafficController(packets);
        // do the common manager config
        this.getSystemConnectionMemo().configureCommandStation(commandStationType,
                mTurnoutNoRetry, mTurnoutExtraSpace);
        this.getSystemConnectionMemo().configureManagers();

        // start operation
        packets.startThreads();
    }

    // base class methods for the LnPortController interface
    @Override
    public DataInputStream getInputStream() {
        if (!opened) {
            log.error("called before load(), stream not available"); // NOI18N
            return null;
        }
        return new DataInputStream(serialInStream);
    }

    @Override
    public DataOutputStream getOutputStream() {
        if (!opened) {
            log.error("getOutputStream called before load(), stream not available"); // NOI18N
            return null;
        }
        return new DataOutputStream(serialOutStream);
    }

    @Override
    public boolean status() {
        return opened;
    }

    /**
     * Get an array of valid baud rates. This is currently just a message saying
     * its fixed
     */
    @Override
    public String[] validBaudRates() {
        return new String[]{"fixed at 16600 baud"};
    }

    /**
     * Set the second port option. Only to be used after construction, but
     * before the openPort call
     */
    @Override
    public void configureOption2(String value) {
        super.configureOption2(value);
        log.debug("configureOption2: " + value); // NOI18N
        setCommandStationType(value);
    }

    // private control members
    private boolean opened = false;
    InputStream serialInStream = null;
    OutputStream serialOutStream = null;

    private final static Logger log = LoggerFactory.getLogger(MS100Adapter.class.getName());

}

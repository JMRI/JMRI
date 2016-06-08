package jmri.jmrix.lenz.liusb;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import jmri.jmrix.lenz.LenzCommandStation;
import jmri.jmrix.lenz.XNetInitializationManager;
import jmri.jmrix.lenz.XNetSerialPortController;
import jmri.jmrix.lenz.XNetTrafficController;
import jmri.util.SerialUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide access to XPressNet via a LIUSB on an FTDI Virtual Comm Port.
 * Normally controlled by the lenz.liusb.LIUSBFrame class.
 *
 * @author	Paul Bender Copyright (C) 2005-2010
 */
public class LIUSBAdapter extends XNetSerialPortController implements jmri.jmrix.SerialPortAdapter {

    public LIUSBAdapter() {
        super();
        option1Name = "FlowControl";
        options.put(option1Name, new Option("LIUSB connection uses : ", validOption1));
        this.manufacturerName = jmri.jmrix.lenz.LenzConnectionTypeList.LENZ;
    }

    public String openPort(String portName, String appName) {
        // open the port in XPressNet mode, check ability to set moderators
        try {
            // get and open the primary port
            CommPortIdentifier portID = CommPortIdentifier.getPortIdentifier(portName);
            try {
                activeSerialPort = (SerialPort) portID.open(appName, 2000);  // name of program, msec to wait
            } catch (PortInUseException p) {
                return handlePortBusy(p, portName, log);
            }
            // try to set it for XNet
            try {
                setSerialPort();
            } catch (gnu.io.UnsupportedCommOperationException e) {
                log.error("Cannot set serial parameters on port " + portName + ": " + e.getMessage());
                return "Cannot set serial parameters on port " + portName + ": " + e.getMessage();
            }

            // set timeout
            try {
                activeSerialPort.enableReceiveTimeout(10);
                log.debug("Serial timeout was observed as: " + activeSerialPort.getReceiveTimeout()
                        + " " + activeSerialPort.isReceiveTimeoutEnabled());
            } catch (Exception et) {
                log.info("failed to set serial timeout: " + et);
            }

            // get and save stream
            serialStream = activeSerialPort.getInputStream();

            // purge contents, if any
            purgeStream(serialStream);

            // report status?
            if (log.isInfoEnabled()) {
                // report now
                log.info(portName + " port opened at "
                        + activeSerialPort.getBaudRate() + " baud with"
                        + " DTR: " + activeSerialPort.isDTR()
                        + " RTS: " + activeSerialPort.isRTS()
                        + " DSR: " + activeSerialPort.isDSR()
                        + " CTS: " + activeSerialPort.isCTS()
                        + "  CD: " + activeSerialPort.isCD()
                );
            }
            if (log.isDebugEnabled()) {
                // report additional status
                log.debug(" port flow control shows "
                        + (activeSerialPort.getFlowControlMode() == SerialPort.FLOWCONTROL_RTSCTS_OUT ? "hardware flow control" : "no flow control"));
            }
            // arrange to notify later
            activeSerialPort.addEventListener(new SerialPortEventListener() {
                public void serialEvent(SerialPortEvent e) {
                    int type = e.getEventType();
                    switch (type) {
                        case SerialPortEvent.DATA_AVAILABLE:
                            if (log.isDebugEnabled()) {
                                log.debug("SerialEvent: DATA_AVAILABLE is " + e.getNewValue());
                            }
                            return;
                        case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
                            if (log.isDebugEnabled()) {
                                log.debug("SerialEvent: OUTPUT_BUFFER_EMPTY is " + e.getNewValue());
                            }
                            setOutputBufferEmpty(true);
                            return;
                        case SerialPortEvent.CTS:
                            if (log.isDebugEnabled()) {
                                log.debug("SerialEvent: CTS is " + e.getNewValue());
                            }
                            return;
                        case SerialPortEvent.DSR:
                            if (log.isDebugEnabled()) {
                                log.debug("SerialEvent: DSR is " + e.getNewValue());
                            }
                            return;
                        case SerialPortEvent.RI:
                            if (log.isDebugEnabled()) {
                                log.debug("SerialEvent: RI is " + e.getNewValue());
                            }
                            return;
                        case SerialPortEvent.CD:
                            if (log.isDebugEnabled()) {
                                log.debug("SerialEvent: CD is " + e.getNewValue());
                            }
                            return;
                        case SerialPortEvent.OE:
                            if (log.isDebugEnabled()) {
                                log.debug("SerialEvent: OE (overrun error) is " + e.getNewValue());
                            }
                            return;
                        case SerialPortEvent.PE:
                            if (log.isDebugEnabled()) {
                                log.debug("SerialEvent: PE (parity error) is " + e.getNewValue());
                            }
                            return;
                        case SerialPortEvent.FE:
                            if (log.isDebugEnabled()) {
                                log.debug("SerialEvent: FE (framing error) is " + e.getNewValue());
                            }
                            return;
                        case SerialPortEvent.BI:
                            if (log.isDebugEnabled()) {
                                log.debug("SerialEvent: BI (break interrupt) is " + e.getNewValue());
                            }
                            return;
                        default:
                            if (log.isDebugEnabled()) {
                                log.debug("SerialEvent of unknown type: " + type + " value: " + e.getNewValue());
                            }
                            return;
                    }
                }
            }
            );
            try {
                activeSerialPort.notifyOnFramingError(true);
            } catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.debug("Could not notifyOnFramingError: " + e);
                }
            }

            try {
                activeSerialPort.notifyOnBreakInterrupt(true);
            } catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.debug("Could not notifyOnBreakInterrupt: " + e);
                }
            }

            try {
                activeSerialPort.notifyOnParityError(true);
            } catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.debug("Could not notifyOnParityError: " + e);
                }
            }

            try {
                activeSerialPort.notifyOnOutputEmpty(true);
            } catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.debug("Could not notifyOnOutputEmpty: " + e);
                }
            }

            try {
                activeSerialPort.notifyOnOverrunError(true);
            } catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.debug("Could not notifyOnOverrunError: " + e);
                }
            }

            opened = true;

        } catch (gnu.io.NoSuchPortException p) {
            return handlePortNotFound(p, portName, log);
        } catch (IOException ioe) {
            log.error("IOException exception while opening port " + portName + " trace follows: " + ioe);
            ioe.printStackTrace();
            return "IO exception while opening port " + portName + ": " + ioe;
        } catch (java.util.TooManyListenersException tmle) {
            log.error("TooManyListenersException while opening port " + portName + " trace follows: " + tmle);
            tmle.printStackTrace();
            return "Too Many Listeners exception while opening port " + portName + ": " + tmle;
        }

        return null; // normal operation
    }

    /**
     * set up all of the other objects to operate with a LIUSB connected to this
     * port
     */
    public void configure() {
        // connect to a packetizing traffic controller
        XNetTrafficController packets = new LIUSBXNetPacketizer(new LenzCommandStation());
        packets.connectPort(this);

        // start operation
        // packets.startThreads();
        this.getSystemConnectionMemo().setXNetTrafficController(packets);

        new XNetInitializationManager(this.getSystemConnectionMemo());

        jmri.jmrix.lenz.ActiveFlag.setActive();
    }

    // base class methods for the XNetSerialPortController interface
    public DataInputStream getInputStream() {
        if (!opened) {
            log.error("getInputStream called before load(), stream not available");
            return null;
        }
        return new DataInputStream(serialStream);
    }

    public DataOutputStream getOutputStream() {
        if (!opened) {
            log.error("getOutputStream called before load(), stream not available");
        }
        try {
            return new DataOutputStream(activeSerialPort.getOutputStream());
        } catch (IOException e) {
            log.error("getOutputStream exception: " + e.getMessage());
        }
        return null;
    }

    public boolean status() {
        return opened;
    }

    /**
     * Local method to do specific configuration
     */
    protected void setSerialPort() throws gnu.io.UnsupportedCommOperationException {
        // find the baud rate value, configure comm options
        int baud = validSpeedValues[0];  // default, but also defaulted in the initial value of selectedSpeed
        for (int i = 0; i < validSpeeds.length; i++) {
            if (validSpeeds[i].equals(mBaudRate)) {
                baud = validSpeedValues[i];
            }
        }
        SerialUtil.setSerialPortParams(activeSerialPort, baud,
                SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1,
                SerialPort.PARITY_NONE);

        // set RTS high, DTR high - done early, so flow control can be configured after
        activeSerialPort.setRTS(true);		// not connected in some serial ports and adapters
        activeSerialPort.setDTR(true);		// pin 1 in DIN8; on main connector, this is DTR

        // find and configure flow control
        int flow = SerialPort.FLOWCONTROL_RTSCTS_OUT; // default, but also deftaul for getOptionState(option1Name)
        if (!getOptionState(option1Name).equals(validOption1[0])) {
            flow = 0;
        }
        activeSerialPort.setFlowControlMode(flow);
        //if (getOptionState(option2Name).equals(validOption2[0]))
        //    checkBuffer = true;
    }

    /**
     * Get an array of valid baud rates. This is currently just a message saying
     * its fixed
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "EI_EXPOSE_REP") // OK to expose array instead of copy until Java 1.6
    public String[] validBaudRates() {
        return validSpeeds;
    }

    protected String[] validSpeeds = new String[]{"57,600 baud"};
    protected int[] validSpeedValues = new int[]{57600};

    // meanings are assigned to these above, so make sure the order is consistent
    protected String[] validOption1 = new String[]{"hardware flow control (recommended for Lenz 23150)", "no flow control (required for Lenz 23151)"};

    private boolean opened = false;
    InputStream serialStream = null;

    @Deprecated
    static public LIUSBAdapter instance() {
        if (mInstance == null) {
            mInstance = new LIUSBAdapter();
        }
        return mInstance;
    }
    static volatile LIUSBAdapter mInstance = null;

    private final static Logger log = LoggerFactory.getLogger(LIUSBAdapter.class.getName());

}

package jmri.jmrix.lenz.li101;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.TooManyListenersException;
import jmri.jmrix.lenz.LenzCommandStation;
import jmri.jmrix.lenz.XNetInitializationManager;
import jmri.jmrix.lenz.XNetPacketizer;
import jmri.jmrix.lenz.XNetSerialPortController;
import jmri.jmrix.lenz.XNetTrafficController;
import jmri.util.SerialUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import purejavacomm.CommPortIdentifier;
import purejavacomm.NoSuchPortException;
import purejavacomm.PortInUseException;
import purejavacomm.SerialPort;
import purejavacomm.SerialPortEvent;
import purejavacomm.SerialPortEventListener;
import purejavacomm.UnsupportedCommOperationException;

/**
 * Provide access to XpressNet via a LI101 on an attached serial comm port.
 * Normally controlled by the lenz.li101.LI101Frame class.
 *
 * @author Bob Jacobsen Copyright (C) 2002
 * @author Paul Bender, Copyright (C) 2003-2010
 */
public class LI101Adapter extends XNetSerialPortController implements jmri.jmrix.SerialPortAdapter {

    public LI101Adapter() {
        super();
        option1Name = "FlowControl"; // NOI18N
        options.put(option1Name, new Option(Bundle.getMessage("XconnectionUsesLabel", Bundle.getMessage("IFTypeLI101")), validOption1));
        this.manufacturerName = jmri.jmrix.lenz.LenzConnectionTypeList.LENZ;
    }

    @Override
    public String openPort(String portName, String appName) {
        // open the port in XpressNet mode, check ability to set moderators
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
            } catch (UnsupportedCommOperationException e) {
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
            // report additional status
            log.debug(" port flow control shows {}", (activeSerialPort.getFlowControlMode() == SerialPort.FLOWCONTROL_RTSCTS_OUT ? "hardware flow control" : "no flow control"));
            // arrange to notify later
            activeSerialPort.addEventListener(new SerialPortEventListener() {
                @Override
                public void serialEvent(SerialPortEvent e) {
                    int type = e.getEventType();
                    switch (type) {
                        case SerialPortEvent.DATA_AVAILABLE:
                            log.debug("SerialEvent: DATA_AVAILABLE is {}", e.getNewValue());
                            return;
                        case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
                            log.debug("SerialEvent: OUTPUT_BUFFER_EMPTY is {}", e.getNewValue());
                            setOutputBufferEmpty(true);
                            return;
                        case SerialPortEvent.CTS:
                            log.debug("SerialEvent: CTS is {}", e.getNewValue());
                            return;
                        case SerialPortEvent.DSR:
                            log.debug("SerialEvent: DSR is {}", e.getNewValue());
                            return;
                        case SerialPortEvent.RI:
                            log.debug("SerialEvent: RI is {}", e.getNewValue());
                            return;
                        case SerialPortEvent.CD:
                            log.debug("SerialEvent: CD is {}", e.getNewValue());
                            return;
                        case SerialPortEvent.OE:
                            log.debug("SerialEvent: OE (overrun error) is {}", e.getNewValue());
                            return;
                        case SerialPortEvent.PE:
                            log.debug("SerialEvent: PE (parity error) is {}", e.getNewValue());
                            return;
                        case SerialPortEvent.FE:
                            log.debug("SerialEvent: FE (framing error) is {}", e.getNewValue());
                            return;
                        case SerialPortEvent.BI:
                            log.debug("SerialEvent: BI (break interrupt) is {}", e.getNewValue());
                            return;
                        default:
                            log.debug("SerialEvent of unknown type: {} value: {}", type, e.getNewValue());
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

        } catch (NoSuchPortException p) {
            return handlePortNotFound(p, portName, log);
        } catch (IOException | TooManyListenersException ex) {
            log.error("Unexpected exception while opening port " + portName + " trace follows: " + ex);
            ex.printStackTrace();
            return "Unexpected error while opening port " + portName + ": " + ex;
        }

        return null; // normal operation
    }

    /**
     * Set up all of the other objects to operate with a LI101 connected to this
     * port.
     */
    @Override
    public void configure() {
        // connect to a packetizing traffic controller
        XNetTrafficController packets = new XNetPacketizer(new LenzCommandStation());
        packets.connectPort(this);

        // start operation
        // packets.startThreads();
        this.getSystemConnectionMemo().setXNetTrafficController(packets);

        new XNetInitializationManager(this.getSystemConnectionMemo());
    }

    // Base class methods for the XNetSerialPortController interface

    @Override
    public DataInputStream getInputStream() {
        if (!opened) {
            log.error("getInputStream called before load(), stream not available");
            return null;
        }
        return new DataInputStream(serialStream);
    }

    @Override
    public DataOutputStream getOutputStream() {
        if (!opened) {
            log.error("getOutputStream called before load(), stream not available");
        }
        try {
            return new DataOutputStream(activeSerialPort.getOutputStream());
        } catch (java.io.IOException e) {
            log.error("getOutputStream exception: " + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean status() {
        return opened;
    }

    /**
     * Local method to do specific configuration.
     */
    protected void setSerialPort() throws UnsupportedCommOperationException {
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

        // find and configure flow control
        int flow = SerialPort.FLOWCONTROL_RTSCTS_OUT; // default, but also deftaul for getOptionState(option1Name)
        if (!getOptionState(option1Name).equals(validOption1[0])) {
            flow = 0;
        }

        configureLeadsAndFlowControl(activeSerialPort, flow);

        /*if (getOptionState(option2Name).equals(validOption2[0]))
         checkBuffer = true;*/
    }

    @Override
    public String[] validBaudRates() {
        return Arrays.copyOf(validSpeeds, validSpeeds.length);
    }

    protected String[] validSpeeds = new String[]{Bundle.getMessage("LIBaud19200"), Bundle.getMessage("Baud38400"),
            Bundle.getMessage("Baud57600"), Bundle.getMessage("Baud115200")};
    protected int[] validSpeedValues = new int[]{19200, 38400, 57600, 115200};

    // meanings are assigned to these above, so make sure the order is consistent
    protected String[] validOption1 = new String[]{Bundle.getMessage("FlowOptionHwRecomm"), Bundle.getMessage("FlowOptionNo")};

    private boolean opened = false;
    InputStream serialStream = null;

    @Deprecated
    static public LI101Adapter instance() {
        if (mInstance == null) {
            mInstance = new LI101Adapter();
        }
        return mInstance;
    }
    static volatile LI101Adapter mInstance = null;

    private final static Logger log = LoggerFactory.getLogger(LI101Adapter.class);

}

package jmri.jmrix.lenz.hornbyelite;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.TooManyListenersException;
import java.util.Vector;
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
 * Provide access to XpressNet via the Hornby Elite's built in USB port.
 * Normally controlled by the lenz.hornbyelite.EliteFrame class.
 *
 * @author Bob Jacobsen Copyright (C) 2002
 * @author Paul Bender, Copyright (C) 2003,2008-2010
 */
public class EliteAdapter extends XNetSerialPortController implements jmri.jmrix.SerialPortAdapter {

    public EliteAdapter() {
        super(new EliteXNetSystemConnectionMemo());
        option1Name = "FlowControl"; // NOI18N
        options.put(option1Name, new Option(Bundle.getMessage("HornbyEliteConnectionLabel"), validOption1));
        option2Name = Bundle.getMessage("BufferTitle");
        options.put(option2Name, new Option(Bundle.getMessage("HornbyEliteCheckLabel"), validOption2));
        setCheckBuffer(true); // default to true for elite
        this.manufacturerName = EliteConnectionTypeList.HORNBY;
    }

    Vector<String> portNameVector = null;

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
                return "Cannot set serial parameters on port " + portName + ": " + e.getMessage(); // NOI18N
            }

            // set timeout
            try {
                activeSerialPort.enableReceiveTimeout(10);
                log.debug("Serial timeout was observed as: {} {}", activeSerialPort.getReceiveTimeout(),
                        activeSerialPort.isReceiveTimeoutEnabled());
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
     * Set up all of the other objects to operate with the Hornby Elite
     * connected to this port.
     */
    @Override
    public void configure() {
        // connect to a packetizing traffic controller
        XNetTrafficController packets = new XNetPacketizer(new HornbyEliteCommandStation());
        packets.connectPort(this);

        // start operation
        // packets.startThreads();
        this.getSystemConnectionMemo().setXNetTrafficController(packets);

        new EliteXNetInitializationManager(this.getSystemConnectionMemo());
    }

    // base class methods for the XNetSerialPortController interface

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
        } catch (IOException e) {
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
        int flow = 0;  // no flow control is first in the elite setup,
        // since it doesn't seem to work with flow
        // control enabled.
        if (!getOptionState(option1Name).equals(validOption1[0])) {
            flow = SerialPort.FLOWCONTROL_RTSCTS_OUT;
        }
        configureLeadsAndFlowControl(activeSerialPort, flow);

        /*if (!getOptionState(option2Name).equals(validOption2[0]))
         CheckBuffer = false;*/
    }

    @Override
    public String[] validBaudRates() {
        return Arrays.copyOf(validSpeeds, validSpeeds.length);
    }

    /**
     * validOption1 controls flow control option.
     */
    /*public String option1Name() { return "Elite connection uses "; }
     public String[] validOption1() { return Arrays.copyOf(validOption1, validOption1.length); }*/
    protected String[] validSpeeds = new String[]{"9,600 baud", "19,200 baud", "38,400 baud", "57,600 baud", "115,200 baud"};
    protected int[] validSpeedValues = new int[]{9600, 19200, 38400, 57600, 115200};

    // meanings are assigned to these above, so make sure the order is consistent
    protected String[] validOption1 = new String[]{Bundle.getMessage("FlowOptionNo"), Bundle.getMessage("FlowOptionHw")};

    private boolean opened = false;
    InputStream serialStream = null;

    
    /**
     * @deprecated JMRI Since 4.4 instance() shouldn't be used. Convert to JMRI multi-system support structure.
     */
    @Deprecated
    static public EliteAdapter instance() {
        if (mInstance == null) {
            mInstance = new EliteAdapter();
        }
        return mInstance;
    }

    static volatile EliteAdapter mInstance = null;

    private final static Logger log = LoggerFactory.getLogger(EliteAdapter.class);

}

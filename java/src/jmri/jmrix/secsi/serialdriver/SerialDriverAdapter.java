package jmri.jmrix.secsi.serialdriver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import jmri.jmrix.secsi.SecsiSystemConnectionMemo;
import jmri.jmrix.secsi.SerialPortController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import purejavacomm.CommPortIdentifier;
import purejavacomm.NoSuchPortException;
import purejavacomm.PortInUseException;
import purejavacomm.SerialPort;
import purejavacomm.UnsupportedCommOperationException;

/**
 * Provide access to Oak Tree via a serial comm port. Normally controlled by the
 * oaktree.serialdriver.SerialDriverFrame class.
 *
 * @author	Bob Jacobsen Copyright (C) 2006, 2007
 */
public class SerialDriverAdapter extends SerialPortController implements jmri.jmrix.SerialPortAdapter {

    SerialPort activeSerialPort = null;

    public SerialDriverAdapter() {
        super(new SecsiSystemConnectionMemo());
        this.manufacturerName = jmri.jmrix.secsi.SerialConnectionTypeList.TRACTRONICS;
    }

    @Override
    public String openPort(String portName, String appName) {
        try {
            // get and open the primary port
            CommPortIdentifier portID = CommPortIdentifier.getPortIdentifier(portName);
            try {
                activeSerialPort = (SerialPort) portID.open(appName, 2000);  // name of program, msec to wait
            } catch (PortInUseException p) {
                return handlePortBusy(p, portName, log);
            }
            // try to set it for serial
            try {
                setSerialPort();
            } catch (UnsupportedCommOperationException e) {
                log.error("Cannot set serial parameters on port " + portName + ": " + e.getMessage());
                return "Cannot set serial parameters on port " + portName + ": " + e.getMessage();
            }

            // set framing (end) character
            try {
                activeSerialPort.enableReceiveFraming(0x03);
                log.debug("Serial framing was observed as: " + activeSerialPort.isReceiveFramingEnabled()
                        + " " + activeSerialPort.getReceiveFramingByte());
            } catch (Exception ef) {
                log.debug("failed to set serial framing: " + ef);
            }

            // set timeout; framing should work before this anyway
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
                log.debug(" port flow control shows " // NOI18N
                        + (activeSerialPort.getFlowControlMode() == SerialPort.FLOWCONTROL_RTSCTS_OUT ? "hardware flow control" : "no flow control")); // NOI18N

                // log events
                setPortEventLogging(activeSerialPort);
            }

            opened = true;

        } catch (NoSuchPortException p) {
            return handlePortNotFound(p, portName, log);
        } catch (IOException ex) {
            log.error("Unexpected exception while opening port {}", portName, ex);
            return "Unexpected error while opening port " + portName + ": " + ex;
        }

        return null; // normal operation
    }

    /**
     * Can the port accept additional characters? Yes, always
     */
    public boolean okToSend() {
        return true;
    }

    /**
     * set up all of the other objects to operate connected to this port
     */
    @Override
    public void configure() {
        // connect to the traffic controller
        ((SecsiSystemConnectionMemo) getSystemConnectionMemo()).getTrafficController().connectPort(this);
        ((SecsiSystemConnectionMemo) getSystemConnectionMemo()).configureManagers();

    }

    // base class methods for the SerialPortController interface
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
     * Local method to do specific port configuration
     */
    protected void setSerialPort() throws UnsupportedCommOperationException {
        // find the baud rate value, configure comm options
        int baud = 38400;  // default, but also defaulted in the initial value of selectedSpeed
        for (int i = 0; i < validSpeeds.length; i++) {
            if (validSpeeds[i].equals(selectedSpeed)) {
                baud = validSpeedValues[i];
            }
        }
        activeSerialPort.setSerialPortParams(baud, SerialPort.DATABITS_8,
                SerialPort.STOPBITS_2, SerialPort.PARITY_NONE);

        // find and configure flow control
        int flow = SerialPort.FLOWCONTROL_NONE; // default
        configureLeadsAndFlowControl(activeSerialPort, flow);
    }

    @Override
    public String[] validBaudRates() {
        return Arrays.copyOf(validSpeeds, validSpeeds.length);
    }

    /**
     * Set the baud rate.
     */
    @Override
    public void configureBaudRate(String rate) {
        log.debug("configureBaudRate: " + rate);
        selectedSpeed = rate;
        super.configureBaudRate(rate);
    }

    protected String[] validSpeeds = new String[]{"38,400 baud"};
    protected int[] validSpeedValues = new int[]{38400};
    protected String selectedSpeed = validSpeeds[0];

    /**
     * Get an array of valid values for "option 2"; used to display valid
     * options. May not be null, but may have zero entries
     */
    public String[] validOption2() {
        return new String[]{""};
    }

    /**
     * Get a String that says what Option 2 represents May be an empty string,
     * but will not be null
     */
    public String option2Name() {
        return "";
    }

    // private control members
    private boolean opened = false;
    InputStream serialStream = null;

    static public SerialDriverAdapter instance() {
        if (mInstance == null) {
            mInstance = new SerialDriverAdapter();
        }
        return mInstance;
    }
    static SerialDriverAdapter mInstance = null;

    private final static Logger log = LoggerFactory.getLogger(SerialDriverAdapter.class);

}

package jmri.jmrix.tmcc.serialdriver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.TooManyListenersException;
import jmri.jmrix.tmcc.SerialPortController;
import jmri.jmrix.tmcc.SerialTrafficController;
import jmri.jmrix.tmcc.TmccSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import purejavacomm.CommPortIdentifier;
import purejavacomm.NoSuchPortException;
import purejavacomm.PortInUseException;
import purejavacomm.SerialPort;
import purejavacomm.SerialPortEvent;
import purejavacomm.UnsupportedCommOperationException;

/**
 * Provide access to TMCC via a serial com port. Normally controlled by the
 * tmcc.serialdriver.SerialDriverFrame class.
 *
 * @author	Bob Jacobsen Copyright (C) 2006
 */
public class SerialDriverAdapter extends SerialPortController {

    SerialPort activeSerialPort = null;

    public SerialDriverAdapter() {
        super(new TmccSystemConnectionMemo());
        this.manufacturerName = jmri.jmrix.tmcc.SerialConnectionTypeList.LIONEL;
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

            // no framing character is used
            // set receive timeout; framing not in use
            try {
                activeSerialPort.enableReceiveTimeout(10);
                log.debug("Serial timeout was observed as: " + activeSerialPort.getReceiveTimeout()
                        + " " + activeSerialPort.isReceiveTimeoutEnabled());
            } catch (UnsupportedCommOperationException et) {
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
            log.error("Unexpected exception while opening port {} trace follows: ", portName, ex);
            return "Unexpected error while opening port " + portName + ": " + ex;
        }

        return null; // normal operation
    }

    /**
     * Can the port accept additional characters?
     *
     * @return true
     */
    public boolean okToSend() {
        return true;
    }

    /**
     * Set up all of the other objects to operate connected to this port.
     */
    @Override
    public void configure() {
        // connect to the traffic controller
        log.debug("set tc for memo {}", getSystemConnectionMemo().getUserName());
        SerialTrafficController control = new SerialTrafficController(getSystemConnectionMemo());
        control.connectPort(this);
        this.getSystemConnectionMemo().setTrafficController(control);
        // do the common manager config
        this.getSystemConnectionMemo().configureManagers();
    }

    // Base class methods for the SerialPortController interface

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
     * Local method to do specific port configuration.
     *
     * @throws UnsupportedCommOperationException if unable to configure
     *                                                  port
     */
    protected void setSerialPort() throws UnsupportedCommOperationException {
        // find the baud rate value, configure comm options
        int baud = currentBaudNumber(mBaudRate);
        activeSerialPort.setSerialPortParams(baud, SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

        // find and configure flow control
        int flow = SerialPort.FLOWCONTROL_NONE; // default
        configureLeadsAndFlowControl(activeSerialPort, flow);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] validBaudRates() {
        return Arrays.copyOf(validSpeeds, validSpeeds.length);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int[] validBaudNumbers() {
        return Arrays.copyOf(validSpeedValues, validSpeedValues.length);
    }

    protected String[] validSpeeds = new String[]{Bundle.getMessage("Baud9600"),
            Bundle.getMessage("Baud19200"), Bundle.getMessage("Baud57600")};
    protected int[] validSpeedValues = new int[]{9600, 19200, 57600};

    @Override
    public int defaultBaudIndex() {
        return 0;
    }

    /**
     * Get an array of valid values for "option 2"; used to display valid
     * options. May not be null, but may have zero entries.
     *
     * @return a single element array containing an empty string
     */
    public String[] validOption2() {
        return new String[]{""};
    }

    /**
     * Get a String that says what Option 2 represents May be an empty string,
     * but will not be null.
     *
     * @return an empty string
     */
    public String option2Name() {
        return "";
    }

    // private control members
    private boolean opened = false;
    InputStream serialStream = null;

    /**
     * @return the default adapter
     * @deprecated JMRI Since 4.4 instance() shouldn't be used
     */
    @Deprecated
    static public SerialDriverAdapter instance() {
        if (mInstance == null) {
            mInstance = new SerialDriverAdapter();
        }
        return mInstance;
    }
    /**
     * @deprecated JMRI Since 4.4 instance() shouldn't be used
     */
    @Deprecated
    static SerialDriverAdapter mInstance = null;

    private final static Logger log = LoggerFactory.getLogger(SerialDriverAdapter.class);

}

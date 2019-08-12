package jmri.jmrix.qsi.serialdriver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import jmri.jmrix.qsi.QsiPortController;
import jmri.jmrix.qsi.QsiSystemConnectionMemo;
import jmri.jmrix.qsi.QsiTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import purejavacomm.CommPortIdentifier;
import purejavacomm.NoSuchPortException;
import purejavacomm.PortInUseException;
import purejavacomm.SerialPort;
import purejavacomm.UnsupportedCommOperationException;

/**
 * Implements SerialPortAdapter for the QSI system.
 * <p>
 * This connects an QSI command station via a serial com port. Also used for the
 * USB QSI, which appears to the computer as a serial port. Normally controlled
 * by the SerialDriverFrame class.
 * <p>
 * The current implementation only handles the 19,200 baud rate, and does not
 * use any other options at configuration time.
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2002
 */
public class SerialDriverAdapter extends QsiPortController {

    public SerialDriverAdapter() {
        super(new QsiSystemConnectionMemo());
        this.manufacturerName = jmri.jmrix.qsi.QSIConnectionTypeList.QSI;
    }

    SerialPort activeSerialPort = null;

    @Override
    public String openPort(String portName, String appName) {
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
                activeSerialPort.setSerialPortParams(19200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            } catch (UnsupportedCommOperationException e) {
                log.error("Cannot set serial parameters on port {}: {}", portName, e.getMessage());
                return "Cannot set serial parameters on port " + portName + ": " + e.getMessage();
            }

            // disable flow control; hardware lines used for signaling, XON/XOFF might appear in data
            configureLeadsAndFlowControl(activeSerialPort, 0);

            // set timeout
            // activeSerialPort.enableReceiveTimeout(1000);
            log.debug("Serial timeout was observed as: {} {}", activeSerialPort.getReceiveTimeout(),
                    activeSerialPort.isReceiveTimeoutEnabled());

            // get and save stream
            serialStream = activeSerialPort.getInputStream();

            // purge contents, if any
            purgeStream(serialStream);

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
            opened = true;

        } catch (NoSuchPortException p) {
            return handlePortNotFound(p, portName, log);
        } catch (IOException ex) {
            log.error("Unexpected exception while opening port {}", portName, ex);
            return "Unexpected error while opening port " + portName + ": " + ex;
        }
        return null; // indicates OK return
    }

    public void setHandshake(int mode) {
        try {
            activeSerialPort.setFlowControlMode(mode);
        } catch (UnsupportedCommOperationException ex) {
            log.error("Unexpected exception while setting COM port handshake mode", ex);
        }
    }

    /**
     * Set up all of the other objects to operate with an QSI command station
     * connected to this port
     */
    @Override
    public void configure() {

        this.getSystemConnectionMemo().setQsiTrafficController(new QsiTrafficController());
        // connect to the traffic controller
        this.getSystemConnectionMemo().getQsiTrafficController().connectPort(this);

        this.getSystemConnectionMemo().configureManagers();

        sinkThread = new Thread(this.getSystemConnectionMemo().getQsiTrafficController());
        sinkThread.start();

        // jmri.InstanceManager.setThrottleManager(new jmri.jmrix.qsi.QsiThrottleManager());
    }

    private Thread sinkThread;

    // base class methods for the QsiPortController interface
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
            log.error("getOutputStream exception: ", e);
        }
        return null;
    }

    @Override
    public boolean status() {
        return opened;
    }

    /**
     * {@inheritDoc}
     *
     * Currently only 19,200 bps
     */
    @Override
    public String[] validBaudRates() {
        return new String[]{"19,200 bps"};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int[] validBaudNumbers() {
        return new int[]{19200};
    }

    @Override
    public int defaultBaudIndex() {
        return 0;
    }

    private boolean opened = false;
    InputStream serialStream = null;

    private final static Logger log = LoggerFactory.getLogger(SerialDriverAdapter.class);

}

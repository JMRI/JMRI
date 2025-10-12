package jmri.jmrix.bachrus.drmserialdriver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.TooManyListenersException;

import jmri.jmrix.bachrus.DRMConnectionTypeList;
import jmri.jmrix.bachrus.SpeedoPortController;
import jmri.jmrix.bachrus.SpeedoSystemConnectionMemo;
import jmri.jmrix.bachrus.SpeedoTrafficController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.jmrix.purejavacomm.CommPortIdentifier;
import jmri.jmrix.purejavacomm.NoSuchPortException;
import jmri.jmrix.purejavacomm.PortInUseException;
import jmri.jmrix.purejavacomm.UnsupportedCommOperationException;

/**
 * Implements SerialPortAdapter for the drM SPC200R speedo.
 * <p>
 * This connects a drM speedo reader interface via a serial com port.
 * Normally controlled by the SerialDriverFrame class.
 * <p>
 * The current implementation only handles the 115,200 baud rate, and does not use
 * any other options at configuration time.
 *
 * Updated January 2010 for gnu io (RXTX) - Andrew Berridge. Comments tagged
 * with "AJB" indicate changes or observations by me
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002
 * @author Andrew Crosland Copyright (C) 2010
 * @author Lolke Bijlsma Copyright (C) 2025
 */
public class SerialDriverAdapter extends SpeedoPortController {

    public SerialDriverAdapter() {
        super(new SpeedoSystemConnectionMemo());
        setManufacturer(DRMConnectionTypeList.DRM);
        this.getSystemConnectionMemo().setSpeedoTrafficController(new SpeedoTrafficController(this.getSystemConnectionMemo()));
    }

    jmri.jmrix.purejavacomm.SerialPort activeSerialPort = null;

    @Override
    public String openPort(String portName, String appName) {
        // open the port, check ability to set moderators
        try {
            // get and open the primary port
            CommPortIdentifier portID = CommPortIdentifier.getPortIdentifier(portName);
            try {
                activeSerialPort = portID.open(appName, 2000);  // name of program, msec to wait
            } catch (PortInUseException p) {
                return handlePortBusy(p, portName, log);
            }

            // try to set it for communication via SerialDriver
            try {
                activeSerialPort.setSerialPortParams(115200,
                        jmri.jmrix.purejavacomm.SerialPort.DATABITS_8,
                        jmri.jmrix.purejavacomm.SerialPort.STOPBITS_1,
                        jmri.jmrix.purejavacomm.SerialPort.PARITY_NONE);
            } catch (UnsupportedCommOperationException e) {
                log.error("Cannot set serial parameters on port {}: {}", portName, e.getMessage());
                return "Cannot set serial parameters on port " + portName + ": " + e.getMessage();
            }

            // set RTS high, DTR high
            // disable flow control; hardware lines used for signaling, XON/XOFF might appear in data
            //AJB: Removed Jan 2010 -
            //Setting flow control mode to zero kills comms - SPROG doesn't send data
            //Concern is that will disabling this affect other SPROGs? Serial ones?
            configureLeadsAndFlowControl(activeSerialPort, 0);

            // set timeout
            // activeSerialPort.enableReceiveTimeout(1000);
            log.debug("Serial timeout was observed as: {} {}", activeSerialPort.getReceiveTimeout(), activeSerialPort.isReceiveTimeoutEnabled());

            // get and save stream
            serialStream = activeSerialPort.getInputStream();

            // purge contents, if any
            purgeStream(serialStream);

            // report status?
            if (log.isInfoEnabled()) {
                log.info("{} port opened at {} baud, sees  DTR: {} RTS: {} DSR: {} CTS: {}  CD: {}", portName, activeSerialPort.getBaudRate(), activeSerialPort.isDTR(), activeSerialPort.isRTS(), activeSerialPort.isDSR(), activeSerialPort.isCTS(), activeSerialPort.isCD());
            }

            //AJB - add Sprog Traffic Controller as event listener
            try {
                activeSerialPort.addEventListener(this.getSystemConnectionMemo().getTrafficController());
            } catch (TooManyListenersException e) {
            }
            setManufacturer(DRMConnectionTypeList.DRM);

            // AJB - activate the DATA_AVAILABLE notifier
            activeSerialPort.notifyOnDataAvailable(true);

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
     * set up all of the other objects to operate with an Sprog command station
     * connected to this port
     */
    @Override
    public void configure() {
        // connect to the traffic controller
        this.getSystemConnectionMemo().getTrafficController().connectPort(this);

        this.getSystemConnectionMemo().configureManagers();
    }

    // base class methods for the SprogPortController interface
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
            log.error("getOutputStream exception", e);
        }
        return null;
    }

    @Override
    public boolean status() {
        return opened;
    }

    /**
     * {@inheritDoc}
     * Currently only 115,200 bps
     */
    @Override
    public String[] validBaudRates() {
        return new String[]{"115,200 bps"};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int[] validBaudNumbers() {
        return new int[]{115200};
    }

    @Override
    public int defaultBaudIndex() {
        return 0;
    }

    private boolean opened = false;
    InputStream serialStream = null;

    private final static Logger log = LoggerFactory.getLogger(SerialDriverAdapter.class);

}

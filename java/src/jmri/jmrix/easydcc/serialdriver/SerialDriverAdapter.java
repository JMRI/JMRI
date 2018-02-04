package jmri.jmrix.easydcc.serialdriver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import jmri.jmrix.easydcc.EasyDccPortController;
import jmri.jmrix.easydcc.EasyDccSystemConnectionMemo;
import jmri.jmrix.easydcc.EasyDccTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import purejavacomm.CommPortIdentifier;
import purejavacomm.NoSuchPortException;
import purejavacomm.PortInUseException;
import purejavacomm.SerialPort;
import purejavacomm.UnsupportedCommOperationException;

/**
 * Implements SerialPortAdapter for the EasyDCC system.
 * <p>
 * This connects an EasyDCC command station via a serial com port.
 * Normally controlled by the SerialDriverFrame class.
 * <p>
 * The current implementation only handles the 9,600 baud rate, and does not use
 * any other options at configuration time.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002
 */
public class SerialDriverAdapter extends EasyDccPortController implements jmri.jmrix.SerialPortAdapter {

    public SerialDriverAdapter() {
        super(new EasyDccSystemConnectionMemo("E", "EasyDCC via Serial")); // pass customized user name
        setManufacturer(jmri.jmrix.easydcc.EasyDccConnectionTypeList.EASYDCC);
    }

    SerialPort activeSerialPort = null;

    @Override
    public String openPort(String portName, String appName) {
        // open the port, check ability to set moderators
        try {
            // get and open the primary port
            CommPortIdentifier portID = CommPortIdentifier.getPortIdentifier(portName);
            try {
                activeSerialPort = (SerialPort) portID.open(appName, 2000); // name of program, msec to wait
            } catch (PortInUseException p) {
                return handlePortBusy(p, portName, log);
            }

            // try to set it for comunication via SerialDriver
            try {
                activeSerialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            } catch (UnsupportedCommOperationException e) {
                log.error("Cannot set serial parameters on port " + portName + ": " + e.getMessage());
                return "Cannot set serial parameters on port " + portName + ": " + e.getMessage();
            }

            // disable flow control; hardware lines used for signaling, XON/XOFF might appear in data
            configureLeadsAndFlowControl(activeSerialPort, 0);

            // set timeout
            // activeSerialPort.enableReceiveTimeout(1000);
            log.debug("Serial timeout was observed as: " + activeSerialPort.getReceiveTimeout()
                    + " " + activeSerialPort.isReceiveTimeoutEnabled());

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

    /**
     * Set up all of the other objects to operate with an EasyDCC command
     * station connected to this port.
     */
    @Override
    public void configure() {
        // connect to the traffic controller
        log.debug("set tc for memo {}", getSystemConnectionMemo().getUserName());
        EasyDccTrafficController control = new EasyDccTrafficController(getSystemConnectionMemo());
        control.connectPort(this);
        this.getSystemConnectionMemo().setEasyDccTrafficController(control);
        // do the common manager config
        this.getSystemConnectionMemo().configureManagers();
    }

    // Base class methods for the EasyDccPortController interface

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
            log.error("getOutputStream exception: " + e);
        }
        return null;
    }

    @Override
    public boolean status() {
        return opened;
    }

    /**
     * Get an array of valid baud rates. This is currently only 9,600 bps.
     */
    @Override
    public String[] validBaudRates() {
        return new String[]{Bundle.getMessage("Baud9600")};
    }

    // private control members
    private boolean opened = false;
    InputStream serialStream = null;

    /**
     * @deprecated JMRI Since 4.9.5 instance() shouldn't be used, convert to JMRI multi-system support structure
     */
    static public SerialDriverAdapter instance() {
        return null;
    }

    private final static Logger log = LoggerFactory.getLogger(SerialDriverAdapter.class);

}

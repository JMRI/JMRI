package jmri.jmrix.acela.serialdriver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import jmri.jmrix.acela.AcelaPortController;
import jmri.jmrix.acela.AcelaSystemConnectionMemo;
import jmri.jmrix.acela.AcelaTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import purejavacomm.CommPortIdentifier;
import purejavacomm.NoSuchPortException;
import purejavacomm.PortInUseException;
import purejavacomm.SerialPort;
import purejavacomm.UnsupportedCommOperationException;

/**
 * Implements SerialPortAdapter for the Acela system. This connects an Acela
 * interface to the CTI network via a serial com port. Normally controlled by
 * the SerialDriverFrame class.
 * <p>
 * The current implementation only handles the 9,600 baud rate, and does not use
 * any other options at configuration time.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002
 *
 * @author Bob Coleman, Copyright (C) 2007, 2008 Based on MRC example, modified
 * to establish Acela support.
 */
public class SerialDriverAdapter extends AcelaPortController {

    public SerialDriverAdapter() {
        super(new AcelaSystemConnectionMemo());
        log.debug("opening Acela serial connection from memo");
        setManufacturer(jmri.jmrix.acela.AcelaConnectionTypeList.CTI);
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
                activeSerialPort.setSerialPortParams(currentBaudNumber(getCurrentBaudRate()), SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
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
     * Set up all of the other objects to operate with a serial command station
     * connected to this port.
     */
    @Override
    public void configure() {
        // connect to the traffic controller
        AcelaTrafficController control = new AcelaTrafficController();
        control.connectPort(this);

        getSystemConnectionMemo().setAcelaTrafficController(control);
        getSystemConnectionMemo().configureManagers();
    }

    // base class methods for the AcelaPortController interface

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
     * {@inheritDoc}
     */
    @Override
    public String[] validBaudRates() {
    // Really just want 9600 Baud for Acela
        return new String[]{Bundle.getMessage("Baud9600")};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int[] validBaudNumbers() {
    // Only 9600 Baud for Acela
        return new int[]{9600};
    }

    // private control members
    private boolean opened = false;
    InputStream serialStream = null;

    /**
     * @deprecated JMRI Since 4.4 instance() shouldn't be used, convert to JMRI multi-system support structure
     */
    @Deprecated
    static public SerialDriverAdapter instance() {
        if (mInstance == null) {
            mInstance = new SerialDriverAdapter();
        }
        return mInstance;
    }
    /**
     * @deprecated JMRI Since 4.4 instance() shouldn't be used, convert to JMRI multi-system support structure
     */
    @Deprecated
    static SerialDriverAdapter mInstance = null;

    private final static Logger log = LoggerFactory.getLogger(SerialDriverAdapter.class);

}

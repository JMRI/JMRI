package jmri.jmrix.sprog.serialdriver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.TooManyListenersException;
import jmri.jmrix.sprog.SprogConstants.SprogMode;
import jmri.jmrix.sprog.SprogPortController;
import jmri.jmrix.sprog.SprogSystemConnectionMemo;
import jmri.jmrix.sprog.SprogTrafficController;
import jmri.jmrix.sprog.update.SprogType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import purejavacomm.CommPortIdentifier;
import purejavacomm.NoSuchPortException;
import purejavacomm.PortInUseException;
import purejavacomm.PureJavaIllegalStateException;
import purejavacomm.SerialPort;
import purejavacomm.UnsupportedCommOperationException;

/**
 * Implements SerialPortAdapter for the Sprog system.
 * <p>
 * This connects an Sprog command station via a serial com port. Also used for
 * the USB SPROG, which appears to the computer as a serial port.
 * <p>
 * The current implementation only handles the 9,600 baud rate, and does not use
 * any other options at configuration time.
 *
 * Updated January 2010 for gnu io (RXTX) - Andrew Berridge.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002
 */
public class SerialDriverAdapter extends SprogPortController {

    public SerialDriverAdapter() {
        super(new SprogSystemConnectionMemo(SprogMode.SERVICE));
        // Set the username to match name, once refactored to handle multiple connections or user setable names/prefixes then this can be removed
        this.baudRate = 9600;
        this.getSystemConnectionMemo().setUserName(Bundle.getMessage("SprogProgrammerTitle"));
        // create the traffic controller
        this.getSystemConnectionMemo().setSprogTrafficController(new SprogTrafficController(this.getSystemConnectionMemo()));
    }

    public SerialDriverAdapter(SprogMode sm) {
        super(new SprogSystemConnectionMemo(sm));
        this.baudRate = 9600;
        this.getSystemConnectionMemo().setUserName("SPROG");
        // create the traffic controller
        this.getSystemConnectionMemo().setSprogTrafficController(new SprogTrafficController(this.getSystemConnectionMemo()));
    }

    public SerialDriverAdapter(SprogMode sm, int baud, SprogType type) {
        super(new SprogSystemConnectionMemo(sm, type));
        this.baudRate = baud;
        this.getSystemConnectionMemo().setUserName("SPROG");
        // create the traffic controller
        this.getSystemConnectionMemo().setSprogTrafficController(new SprogTrafficController(this.getSystemConnectionMemo()));
    }

    public SerialDriverAdapter(SprogMode sm, int baud) {
        super(new SprogSystemConnectionMemo(sm));
        this.baudRate = baud;
        this.getSystemConnectionMemo().setUserName("SPROG");
        // create the traffic controller
        this.getSystemConnectionMemo().setSprogTrafficController(new SprogTrafficController(this.getSystemConnectionMemo()));
    }

    SerialPort activeSerialPort = null;

    private int baudRate = -1;

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
                activeSerialPort.setSerialPortParams(baudRate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            } catch (UnsupportedCommOperationException e) {
                log.error("Cannot set serial parameters on port {}: {}", portName, e.getMessage());
                return "Cannot set serial parameters on port " + portName + ": " + e.getMessage();
            }

            // set RTS high, DTR high
            boolean doNotlog = false; // if using socat to forward serial port though network, following
            try {                     // commands will fail, as well as bellow logging
                activeSerialPort.setRTS(true);		// not connected in some serial ports and adapters
                activeSerialPort.setDTR(true);	
            } catch (PureJavaIllegalStateException e) {
                log.info("Cannot setRTS/DTR will continue anyway");
                doNotlog = true;
            }
            	// pin 1 in DIN8; on main connector, this is DTR
            // disable flow control; hardware lines used for signaling, XON/XOFF might appear in data
            //AJB: Removed Jan 2010 -
            //Setting flow control mode to zero kills comms - SPROG doesn't send data
            //Concern is that will disabling this affect other SPROGs? Serial ones?
            //activeSerialPort.setFlowControlMode(0);

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
                if (doNotlog) {
                    log.info(portName + " port opened at "
                            + activeSerialPort.getBaudRate() + " baud");
                } else {
                    log.info(portName + " port opened at "
                            + activeSerialPort.getBaudRate() + " baud, sees "
                            + " DTR: " + activeSerialPort.isDTR()
                            + " RTS: " + activeSerialPort.isRTS()
                            + " DSR: " + activeSerialPort.isDSR()
                            + " CTS: " + activeSerialPort.isCTS()
                            + "  CD: " + activeSerialPort.isCD()
                    );
                    }
            }

            //add Sprog Traffic Controller as event listener
            try {
                activeSerialPort.addEventListener(this.getSystemConnectionMemo().getSprogTrafficController());
            } catch (TooManyListenersException e) {
            }

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
            log.error("Unexpected exception while setting COM port handshake mode,", ex);
        }
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
            log.error("getOutputStream exception: " + e);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     * Currently only 9,600 bps
     */
    @Override
    public String[] validBaudRates() {
        return new String[]{"9,600 bps"};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int[] validBaudNumbers() {
        return new int[]{9600};
    }

    @Override
    public int defaultBaudIndex() {
        return 0;
    }

    InputStream serialStream = null;

    /**
     * @deprecated JMRI Since 4.4 instance() shouldn't be used, convert to JMRI multi-system support structure
     */
    @Deprecated
    static public SerialDriverAdapter instance() {
        return null;
    }

    /**
     * Set up all of the other objects to operate with an Sprog command station
     * connected to this port.
     */
    @Override
    public void configure() {
        // connect to the traffic controller
        this.getSystemConnectionMemo().getSprogTrafficController().connectPort(this);

        this.getSystemConnectionMemo().configureCommandStation();
        this.getSystemConnectionMemo().configureManagers();

        if (getOptionState("TrackPowerState") != null && getOptionState("TrackPowerState").equals(Bundle.getMessage("PowerStateOn"))) {
            try {
                this.getSystemConnectionMemo().getPowerManager().setPower(jmri.PowerManager.ON);
            } catch (jmri.JmriException e) {
                log.error(e.toString());
            }
        }
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(SerialDriverAdapter.class);

}

package jmri.jmrix.can.adapters.lawicell;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import jmri.jmrix.can.TrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import purejavacomm.CommPortIdentifier;
import purejavacomm.NoSuchPortException;
import purejavacomm.PortInUseException;
import purejavacomm.SerialPort;
import purejavacomm.UnsupportedCommOperationException;

/**
 * Implements SerialPortAdapter for the LAWICELL protocol.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002, 2008
 * @author Andrew Crosland Copyright (C) 2008
 */
public class SerialDriverAdapter extends PortController {

    SerialPort activeSerialPort = null;

    public SerialDriverAdapter() {
        super(new jmri.jmrix.can.CanSystemConnectionMemo());
        option1Name = "Protocol"; // NOI18N
        options.put(option1Name, new Option(Bundle.getMessage("ConnectionProtocol"),
                jmri.jmrix.can.ConfigurationManager.getSystemOptions()));
    }

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
                // find the baud rate value, configure comm options
                int baud = currentBaudNumber(mBaudRate);
                activeSerialPort.setSerialPortParams(baud, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            } catch (UnsupportedCommOperationException e) {
                log.error("Cannot set serial parameters on port {}: {}", portName, e.getMessage());
                return "Cannot set serial parameters on port " + portName + ": " + e.getMessage();
            }

            // disable flow control; hardware lines used for signaling, XON/XOFF might appear in data
            configureLeadsAndFlowControl(activeSerialPort, 0);
            activeSerialPort.enableReceiveTimeout(50);  // 50 mSec timeout before sending chars

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
        } catch (UnsupportedCommOperationException | IOException ex) {
            log.error("Unexpected exception while opening port {}", portName, ex);
            return "Unexpected error while opening port " + portName + ": " + ex;
        }

        return null; // indicates OK return
    }

    /**
     * Set up all of the other objects to operate with a CAN RS adapter
     * connected to this port.
     */
    @Override
    public void configure() {

        // Register the CAN traffic controller being used for this connection
        TrafficController tc = new LawicellTrafficController();
        this.getSystemConnectionMemo().setTrafficController(tc);

        // Now connect to the traffic controller
        log.debug("Connecting port");
        tc.connectPort(this);

        // send a request for version information, set 125kbps, open channel
        log.debug("send version request");
        jmri.jmrix.can.CanMessage m
                = new jmri.jmrix.can.CanMessage(new int[]{'V', 13, 'S', '4', 13, 'O', 13}, tc.getCanid());
        m.setTranslated(true);
        tc.sendCanMessage(m, null);

        // do central protocol-specific configuration
        this.getSystemConnectionMemo().setProtocol(getOptionState(option1Name));

        this.getSystemConnectionMemo().configureManagers();
    }

    // base class methods for the PortController interface
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
     */
    @Override
    public String[] validBaudRates() {
        return Arrays.copyOf(validSpeeds, validSpeeds.length);
    }

    @Override
    public int[] validBaudNumbers() {
        return Arrays.copyOf(validSpeedValues, validSpeedValues.length);
    }

    /**
     * Migration method
     * @deprecated since 4.16
     */
    @Deprecated
    public int[] validBaudValues() {
        return validBaudNumbers();
    }

    protected String[] validSpeeds = new String[]{Bundle.getMessage("Baud57600"),
            Bundle.getMessage("Baud115200"), Bundle.getMessage("Baud230400"),
            Bundle.getMessage("Baud250000"), Bundle.getMessage("Baud333333"),
            Bundle.getMessage("Baud460800"), Bundle.getMessage("Baud500000")};
    protected int[] validSpeedValues = new int[]{57600, 115200, 230400, 250000, 333333, 460800, 500000};

    @Override
    public int defaultBaudIndex() {
        return 0;
    }

    // private control members
    private boolean opened = false;
    InputStream serialStream = null;

    private final static Logger log = LoggerFactory.getLogger(SerialDriverAdapter.class);

}

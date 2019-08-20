package jmri.jmrix.direct.serial;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Vector;
import jmri.jmrix.direct.PortController;
import jmri.jmrix.direct.TrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import purejavacomm.CommPortIdentifier;
import purejavacomm.NoSuchPortException;
import purejavacomm.PortInUseException;
import purejavacomm.SerialPort;
import purejavacomm.UnsupportedCommOperationException;

/**
 * Implements SerialPortAdapter for direct serial drive.
 * <p>
 * Normally controlled by the SerialDriverFrame class.
 * <p>
 * The current implementation only handles the 19,200 baud rate, and does not
 * use any other options at configuration time.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002, 2004
 */
public class SerialDriverAdapter extends PortController {

    Vector<String> portNameVector = null;
    SerialPort activeSerialPort = null;

    @Override
    public Vector<String> getPortNames() {
        // first, check that the comm package can be opened and ports seen
        portNameVector = new Vector<>();
        Enumeration<CommPortIdentifier> portIDs = CommPortIdentifier.getPortIdentifiers();
        // find the names of suitable ports
        while (portIDs.hasMoreElements()) {
            CommPortIdentifier id = portIDs.nextElement();
            // filter out line printers
            if (id.getPortType() != CommPortIdentifier.PORT_PARALLEL) // accumulate the names in a vector
            {
                portNameVector.addElement(id.getName());
            }
        }
        return portNameVector;
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value="SR_NOT_CHECKED",
    justification="this is for skip-chars while loop: no matter how many, we're skipping")
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

            // try to set it for 17240, then 16457 baud, then 19200 if needed
            try {
                activeSerialPort.setSerialPortParams(17240, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            } catch (UnsupportedCommOperationException e) {
                // assume that's a baudrate problem, fall back.
                log.warn("attempting to fall back to 16457 baud after 17240 failed");
                try {
                    activeSerialPort.setSerialPortParams(16457, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
                } catch (UnsupportedCommOperationException e2) {
                    log.warn("trouble setting 16457 baud");
                    activeSerialPort.setSerialPortParams(19200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
                    javax.swing.JOptionPane.showMessageDialog(null,
                            Bundle.getMessage("DirectBaudError", activeSerialPort.getBaudRate()),
                            Bundle.getMessage("ErrorConnectionTitle"), javax.swing.JOptionPane.ERROR_MESSAGE);
                }
            }

            // disable flow control; hardware lines used for signaling, XON/XOFF might appear in data
            configureLeadsAndFlowControl(activeSerialPort, 0);

            // activeSerialPort.enableReceiveTimeout(1000);
            log.debug("Serial timeout was observed as: {} {}", activeSerialPort.getReceiveTimeout(),
                    activeSerialPort.isReceiveTimeoutEnabled());

            // get and save stream
            serialInStream = activeSerialPort.getInputStream();
            serialOutStream = activeSerialPort.getOutputStream();

            // port is open, start work on the stream
            // purge contents, if any
            int count = serialInStream.available();
            log.debug("input stream shows {} bytes available", count);
            while (count > 0) {
                serialInStream.skip(count);
                count = serialInStream.available();
            }

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

        } catch (NoSuchPortException p) {
            return handlePortNotFound(p, portName, log);
        } catch (UnsupportedCommOperationException | IOException ex) {
            log.error("Unexpected exception while opening port {}", portName, ex);
            return "Unexpected error while opening port " + portName + ": " + ex;
        }

        return null; // normal termination
    }

    /**
     * Set up all of the other objects to operate with direct drive on this port.
     */
    @Override
    public void configure() {
        // connect to the traffic controller
        TrafficController tc = new TrafficController((jmri.jmrix.direct.DirectSystemConnectionMemo)getSystemConnectionMemo());
        ((jmri.jmrix.direct.DirectSystemConnectionMemo)getSystemConnectionMemo()).setTrafficController(tc);
        // connect to the traffic controller
        tc.connectPort(this);

        // do the common manager config
        ((jmri.jmrix.direct.DirectSystemConnectionMemo)getSystemConnectionMemo()).configureManagers();
    }

    // base class methods for the PortController interface

    /**
     * {@inheritDoc}
     */
    @Override
    public DataInputStream getInputStream() {
        if (!opened) {
            log.error("getInputStream called before load(), stream not available");
            return null;
        }
        return new DataInputStream(serialInStream);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataOutputStream getOutputStream() {
        if (!opened) {
            log.error("getOutputStream called before load(), stream not available");
        }
        return new DataOutputStream(serialOutStream);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean status() {
        return opened;
    }

    /**
     * {@inheritDoc}
     * Currently only 19,200 bps.
     */
    @Override
    public String[] validBaudRates() {
        return new String[]{Bundle.getMessage("Baud19200")};
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

    // private control members
    private boolean opened = false;
    InputStream serialInStream = null;
    OutputStream serialOutStream = null;

    /**
     * @deprecated JMRI Since 4.4 instance() shouldn't be used, convert to JMRI
     * multi-system support structure
     */
    @Deprecated
    static public SerialDriverAdapter instance() {
        if (mInstance == null) {
            mInstance = new SerialDriverAdapter();
        }
        return mInstance;
    }
    /**
     * @deprecated JMRI Since 4.4 instance() shouldn't be used, convert to JMRI
     * multi-system support structure
     */
    @Deprecated
    static SerialDriverAdapter mInstance = null;

    private final static Logger log = LoggerFactory.getLogger(SerialDriverAdapter.class);

}

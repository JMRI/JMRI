package jmri.jmrix.loconet.ms100;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Vector;
import jmri.jmrix.loconet.LnPacketizer;
import jmri.jmrix.loconet.LnPortController;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import purejavacomm.CommPortIdentifier;
import purejavacomm.NoSuchPortException;
import purejavacomm.PortInUseException;
import purejavacomm.SerialPort;
import purejavacomm.UnsupportedCommOperationException;

/**
 * Provide access to LocoNet via a MS100 attached to a serial com port.
 * Normally controlled by the jmri.jmrix.loconet.ms100.ConnectionConfig class.
 * <p>
 * By default, this attempts to use 16600 baud. If that fails, it falls back to
 * 16457 baud. Neither the baud rate configuration nor the "option 1" option are
 * used.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public class MS100Adapter extends LnPortController {

    public MS100Adapter() {
        super(new LocoNetSystemConnectionMemo());
        option2Name = "CommandStation"; // NOI18N
        option3Name = "TurnoutHandle"; // NOI18N
        options.put(option2Name, new Option(Bundle.getMessage("CommandStationTypeLabel"), commandStationNames, false));
        options.put(option3Name, new Option(Bundle.getMessage("TurnoutHandling"),
                new String[]{Bundle.getMessage("HandleNormal"), Bundle.getMessage("HandleSpread"), Bundle.getMessage("HandleOneOnly"), Bundle.getMessage("HandleBoth")})); // I18N

    }

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
            // try to set it for LocoNet direct (e.g. via MS100)
            // spec is 16600, says 16457 is OK also. Try that as a second choice
            try {
                activeSerialPort.setSerialPortParams(16600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            } catch (UnsupportedCommOperationException e) {
                // assume that's a baudrate problem, fall back.
                log.warn("attempting to fall back to 16457 baud after 16600 failed");
                try {
                    activeSerialPort.setSerialPortParams(16457, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
                } catch (UnsupportedCommOperationException e2) {
                    log.warn("trouble setting 16600 baud");
                    javax.swing.JOptionPane.showMessageDialog(null,
                            "Failed to set the correct baud rate for the MS100. Port is set to "
                            + activeSerialPort.getBaudRate()
                            + " baud. See the README file for more info.",
                            "Connection failed", javax.swing.JOptionPane.ERROR_MESSAGE);
                }
            }

            // disable flow control; hardware lines used for signaling, XON/XOFF might appear in data
            // set RTS high, DTR low to power the MS100
            configureLeadsAndFlowControl(activeSerialPort, 0, true, false);

            // set timeout
            try {
                activeSerialPort.enableReceiveTimeout(10);
                log.debug("Serial timeout was observed as: {} {}",
                        activeSerialPort.getReceiveTimeout(), activeSerialPort.isReceiveTimeoutEnabled());
            } catch (UnsupportedCommOperationException et) {
                log.info("failed to set serial timeout: " + et);
            }

            // get and save stream
            serialInStream = activeSerialPort.getInputStream();
            serialOutStream = activeSerialPort.getOutputStream();

            // port is open, start work on the stream
            // purge contents, if any
            purgeStream(serialInStream);

            opened = true;

        } catch (NoSuchPortException p) {
            return handlePortNotFound(p, portName, log);
        } catch (IOException ex) {
            log.error("Unexpected exception while opening port {}", portName, ex);
            return "Unexpected error while opening port " + portName + ": " + ex;
        }

        return null; // normal termination
    }

    /**
     * set up all of the other objects to operate with a MS100 connected to this
     * port
     */
    @Override
    public void configure() {

        setCommandStationType(getOptionState(option2Name));
        setTurnoutHandling(getOptionState(option3Name));
        // connect to a packetizing traffic controller
        LnPacketizer packets = new LnPacketizer(this.getSystemConnectionMemo());
        packets.connectPort(this);

        // create memo
        this.getSystemConnectionMemo().setLnTrafficController(packets);
        // do the common manager config
        this.getSystemConnectionMemo().configureCommandStation(commandStationType,
                mTurnoutNoRetry, mTurnoutExtraSpace, mTranspondingAvailable);
        this.getSystemConnectionMemo().configureManagers();

        // start operation
        packets.startThreads();
    }

    // base class methods for the LnPortController interface
    @Override
    public DataInputStream getInputStream() {
        if (!opened) {
            log.error("called before load(), stream not available");
            return null;
        }
        return new DataInputStream(serialInStream);
    }

    @Override
    public DataOutputStream getOutputStream() {
        if (!opened) {
            log.error("getOutputStream called before load(), stream not available");
            return null;
        }
        return new DataOutputStream(serialOutStream);
    }

    @Override
    public boolean status() {
        return opened;
    }

    /**
     * {@inheritDoc}
     *
     * Just a message saying it's fixed
     */
    @Override
    public String[] validBaudRates() {
        return new String[]{"fixed at 16,600 baud"};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int[] validBaudNumbers() {
        return new int[]{16600};
    }

    @Override
    public int defaultBaudIndex() {
        return 0;
    }

    /**
     * Set the second port option. Only to be used after construction, but
     * before the openPort call
     */
    @Override
    public void configureOption2(String value) {
        super.configureOption2(value);
        log.debug("configureOption2: " + value);
        setCommandStationType(value);
    }

    // private control members
    private boolean opened = false;
    InputStream serialInStream = null;
    OutputStream serialOutStream = null;

    private final static Logger log = LoggerFactory.getLogger(MS100Adapter.class);

}

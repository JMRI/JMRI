package jmri.jmrix.lenz.ztc640;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import jmri.jmrix.lenz.LenzCommandStation;
import jmri.jmrix.lenz.XNetInitializationManager;
import jmri.jmrix.lenz.XNetSerialPortController;
import jmri.jmrix.lenz.XNetTrafficController;
import jmri.util.SerialUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import purejavacomm.CommPortIdentifier;
import purejavacomm.NoSuchPortException;
import purejavacomm.PortInUseException;
import purejavacomm.SerialPort;
import purejavacomm.UnsupportedCommOperationException;

/**
 * Provide access to XpressNet via a ZTC640 connected via an FTDI virtual comm
 * port. Normally controlled by the lenz.ztc640.ZTC640Frame class.
 *
 * @author Bob Jacobsen Copyright (C) 2002
 * @author Paul Bender, Copyright (C) 2003-2010
 */
public class ZTC640Adapter extends XNetSerialPortController {

    public ZTC640Adapter() {
        super();
        option1Name = "FlowControl"; // NOI18N
        options.put(option1Name, new Option(Bundle.getMessage("ZTC640ConnectionLabel"), validOption1));
    }

    @Override
    public String openPort(String portName, String appName) {
        // open the port in XpressNet mode, check ability to set moderators
        try {
            // get and open the primary port
            CommPortIdentifier portID = CommPortIdentifier.getPortIdentifier(portName);
            try {
                activeSerialPort = (SerialPort) portID.open(appName, 2000);  // name of program, msec to wait
            } catch (PortInUseException p) {
                return handlePortBusy(p, portName, log);
            }
            // try to set it for XNet
            try {
                setSerialPort();
            } catch (UnsupportedCommOperationException e) {
                log.error("Cannot set serial parameters on port {}: {}", portName, e.getMessage());
                return "Cannot set serial parameters on port " + portName + ": " + e.getMessage();
            }

            try {
                // set timeout
                activeSerialPort.enableReceiveTimeout(10);
            } catch (UnsupportedCommOperationException ex) {
                log.error("Cannot set receive timeout on port {}: {}", portName, ex.getMessage());
                return "Cannot set receive timeout on port " + portName + ": " + ex.getMessage();
            }
            log.debug("Serial timeout was observed as: {} {}", activeSerialPort.getReceiveTimeout(),
                    activeSerialPort.isReceiveTimeoutEnabled());

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
            return "IO Exception while opening port " + portName + ": " + ex;
        }

        return null; // normal operation
    }

    /**
     * Set up all of the other objects to operate with a ZTC640 connected to
     * this port.
     */
    @Override
    public void configure() {
        // connect to a packetizing traffic controller
        XNetTrafficController packets = new ZTC640XNetPacketizer(new LenzCommandStation());
        packets.connectPort(this);

        // start operation
        // packets.startThreads();
        this.getSystemConnectionMemo().setXNetTrafficController(packets);
        new XNetInitializationManager(this.getSystemConnectionMemo());
    }

    // base class methods for the XNetSerialPortController interface

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
        } catch (IOException e) {
            log.error("getOutputStream exception: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public boolean status() {
        return opened;
    }

    /**
     * Local method to do specific configuration.
     */
    protected void setSerialPort() throws UnsupportedCommOperationException {
        // find the baud rate value, configure comm options
        int baud = currentBaudNumber(mBaudRate);
        SerialUtil.setSerialPortParams(activeSerialPort, baud,
                SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1,
                SerialPort.PARITY_NONE);

        // find and configure flow control
        int flow = 0; // default, but also default for getOptionState(option1Name)
        if (!getOptionState(option1Name).equals(validOption1[0])) {
            flow = SerialPort.FLOWCONTROL_RTSCTS_OUT;
        }

        configureLeadsAndFlowControl(activeSerialPort, flow);

        /* if (getOptionState(option2Name).equals(validOption2[0]))
         setCheckBuffer(true);*/
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

    protected String[] validSpeeds = new String[]{Bundle.getMessage("Baud19200")};
    protected int[] validSpeedValues = new int[]{19200};

    @Override
    public int defaultBaudIndex() {
        return 0;
    }

    // meanings are assigned to these above, so make sure the order is consistent
    protected String[] validOption1 = new String[]{Bundle.getMessage("FlowOptionNoRecomm"), Bundle.getMessage("FlowOptionHw")};

    private boolean opened = false;
    InputStream serialStream = null;

    @Deprecated
    static public ZTC640Adapter instance() {
        if (mInstance == null) {
            mInstance = new ZTC640Adapter();
        }
        return mInstance;
    }

    static volatile ZTC640Adapter mInstance = null;

    private final static Logger log = LoggerFactory.getLogger(ZTC640Adapter.class);

}

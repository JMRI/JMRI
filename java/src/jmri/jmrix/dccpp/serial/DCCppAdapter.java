package jmri.jmrix.dccpp.serial;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import jmri.jmrix.dccpp.DCCppCommandStation;
import jmri.jmrix.dccpp.DCCppInitializationManager;
import jmri.jmrix.dccpp.DCCppSerialPortController;
import jmri.jmrix.dccpp.DCCppTrafficController;
import jmri.util.SerialUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import purejavacomm.CommPortIdentifier;
import purejavacomm.NoSuchPortException;
import purejavacomm.PortInUseException;
import purejavacomm.SerialPort;
import purejavacomm.UnsupportedCommOperationException;

/**
 * Provide access to DCC++ via a FTDI Virtual Com Port. Normally controlled by
 * the lenz.liusb.LIUSBFrame class.
 *
 * @author Mark Underwood Copyright (C) 2015
 *
 * Based on jmri.jmirx.lenz.liusb.LIUSBAdapter by Paul Bender
 */
public class DCCppAdapter extends DCCppSerialPortController {

    public DCCppAdapter() {
        super();
        //option1Name = "FlowControl";
        //options.put(option1Name, new Option("DCC++ connection uses : ", validOption1));
        this.manufacturerName = jmri.jmrix.dccpp.DCCppConnectionTypeList.DCCPP;
    }

    @Override
    public String openPort(String portName, String appName) {
        // open the port in DCC++ mode, check ability to set moderators
        try {
            // get and open the primary port
            CommPortIdentifier portID = CommPortIdentifier.getPortIdentifier(portName);
            try {
                activeSerialPort = (SerialPort) portID.open(appName, 2000);  // name of program, msec to wait
            } catch (PortInUseException p) {
                return handlePortBusy(p, portName, log);
            }
            // try to set it for DCC++
            try {
                setSerialPort();
            } catch (UnsupportedCommOperationException e) {
                log.error("Cannot set serial parameters on port {}: {}", portName, e.getMessage());
                return "Cannot set serial parameters on port " + portName + ": " + e.getMessage();
            }

            // set timeout
            try {
                activeSerialPort.enableReceiveTimeout(10);
                log.debug("Serial timeout was observed as: {} {}",
                        activeSerialPort.getReceiveTimeout(),
                        activeSerialPort.isReceiveTimeoutEnabled());
            } catch (Exception et) {
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
            log.error("Unexpected exception while opening port {}", portName, ex);
            return "Unexpected error while opening port " + portName + ": " + ex;
        }

        return null; // normal operation
    }

    /**
     * Set up all of the other objects to operate with a DCC++ device connected
     * to this port.
     */
    @Override
    public void configure() {
        // connect to a packetizing traffic controller
        DCCppTrafficController packets = new SerialDCCppPacketizer(new DCCppCommandStation());
        packets.connectPort(this);

        // start operation
        // packets.startThreads();
        this.getSystemConnectionMemo().setDCCppTrafficController(packets);

        new DCCppInitializationManager(this.getSystemConnectionMemo());
    }

    // base class methods for the XNetSerialPortController interface

    public BufferedReader getInputStreamBR() {
        if (!opened) {
            log.error("getInputStream called before load(), stream not available");
            return null;
        }
        return new BufferedReader(new InputStreamReader(serialStream));
    }

    @Override
    public DataInputStream getInputStream() {
        //log.error("Not Using DataInputStream version anymore!");
        if (!opened) {
            log.error("getInputStream called before load(), stream not available");
        }
        try {
            return new DataInputStream(activeSerialPort.getInputStream());
        } catch (java.io.IOException e) {
            log.error("getInputStream exception: " + e.getMessage());
        }
        return null;
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
        int flow = SerialPort.FLOWCONTROL_NONE;
        configureLeadsAndFlowControl(activeSerialPort, flow);
        // if (getOptionState(option2Name).equals(validOption2[0]))
        // checkBuffer = true;
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

    protected String[] validSpeeds = new String[]{Bundle.getMessage("Baud115200")};
    protected int[] validSpeedValues = new int[]{115200};

    @Override
    public int defaultBaudIndex() {
        return 0;
    }

    // meanings are assigned to these above, so make sure the order is consistent
    // protected String[] validOption1 = new String[]{Bundle.getMessage("FlowOptionHw"), Bundle.getMessage("FlowOptionNo")};
    protected String[] validOption1 = new String[]{Bundle.getMessage("FlowOptionNo")};

    private boolean opened = false;
    InputStream serialStream = null;

    @Deprecated
    static public DCCppAdapter instance() {
        if (mInstance == null) {
            mInstance = new DCCppAdapter();
        }
        return mInstance;
    }
    static volatile DCCppAdapter mInstance = null; // TODO: Rename this?

    private final static Logger log = LoggerFactory.getLogger(DCCppAdapter.class);

}

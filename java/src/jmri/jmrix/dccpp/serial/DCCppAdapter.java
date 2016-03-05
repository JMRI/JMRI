// DCCppAdapter.java
package jmri.jmrix.dccpp.serial;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import java.io.DataInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import jmri.jmrix.dccpp.DCCppCommandStation;
import jmri.jmrix.dccpp.DCCppSerialPortController;
import jmri.jmrix.dccpp.DCCppTrafficController;
import jmri.jmrix.dccpp.DCCppInitializationManager;
/*
 * TODO: Replace these with DCC++ equivalents
 *
import jmri.jmrix.lenz.XNetInitializationManager;
*/
import jmri.util.SerialUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide access to DCC++ via a FTDI Virtual Comm Port.
 * Normally controlled by the lenz.liusb.LIUSBFrame class.
 *
 * @author	Mark Underwood Copyright (C) 2015
 * @version	$Revision$
 *
 * Based on jmri.jmirx.lenz.liusb.LIUSBAdapter by Paul Bender
 */
public class DCCppAdapter extends DCCppSerialPortController implements jmri.jmrix.SerialPortAdapter {

    public DCCppAdapter() {
        super();
        //option1Name = "FlowControl";
        //options.put(option1Name, new Option("DCC++ connection uses : ", validOption1));
        this.manufacturerName = jmri.jmrix.DCCManufacturerList.DCCPP;
    }

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
            } catch (gnu.io.UnsupportedCommOperationException e) {
                log.error("Cannot set serial parameters on port " + portName + ": " + e.getMessage());
                return "Cannot set serial parameters on port " + portName + ": " + e.getMessage();
            }

            // set timeout
            try {
                activeSerialPort.enableReceiveTimeout(10);
                log.debug("Serial timeout was observed as: " + activeSerialPort.getReceiveTimeout()
                        + " " + activeSerialPort.isReceiveTimeoutEnabled());
            } catch (Exception et) {
                log.info("failed to set serial timeout: " + et);
            }

            // get and save stream
            serialStream = activeSerialPort.getInputStream();

            // purge contents, if any
            int count = serialStream.available();
            log.debug("input stream shows " + count + " bytes available");
            while (count > 0) {
                serialStream.skip(count);
                count = serialStream.available();
            }

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
                log.debug(" port flow control shows "
                        + (activeSerialPort.getFlowControlMode() == SerialPort.FLOWCONTROL_RTSCTS_OUT ? "hardware flow control" : "no flow control"));
            }
            // arrange to notify later
            activeSerialPort.addEventListener(new SerialPortEventListener() {
                public void serialEvent(SerialPortEvent e) {
                    int type = e.getEventType();
                    switch (type) {
                        case SerialPortEvent.DATA_AVAILABLE:
                            if (log.isDebugEnabled()) {
                                log.debug("SerialEvent: DATA_AVAILABLE is " + e.getNewValue());
                            }
                            return;
                        case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
                            if (log.isDebugEnabled()) {
                                log.debug("SerialEvent: OUTPUT_BUFFER_EMPTY is " + e.getNewValue());
                            }
                            setOutputBufferEmpty(true);
                            return;
                        case SerialPortEvent.CTS:
                            if (log.isDebugEnabled()) {
                                log.debug("SerialEvent: CTS is " + e.getNewValue());
                            }
                            return;
                        case SerialPortEvent.DSR:
                            if (log.isDebugEnabled()) {
                                log.debug("SerialEvent: DSR is " + e.getNewValue());
                            }
                            return;
                        case SerialPortEvent.RI:
                            if (log.isDebugEnabled()) {
                                log.debug("SerialEvent: RI is " + e.getNewValue());
                            }
                            return;
                        case SerialPortEvent.CD:
                            if (log.isDebugEnabled()) {
                                log.debug("SerialEvent: CD is " + e.getNewValue());
                            }
                            return;
                        case SerialPortEvent.OE:
                            if (log.isDebugEnabled()) {
                                log.debug("SerialEvent: OE (overrun error) is " + e.getNewValue());
                            }
                            return;
                        case SerialPortEvent.PE:
                            if (log.isDebugEnabled()) {
                                log.debug("SerialEvent: PE (parity error) is " + e.getNewValue());
                            }
                            return;
                        case SerialPortEvent.FE:
                            if (log.isDebugEnabled()) {
                                log.debug("SerialEvent: FE (framing error) is " + e.getNewValue());
                            }
                            return;
                        case SerialPortEvent.BI:
                            if (log.isDebugEnabled()) {
                                log.debug("SerialEvent: BI (break interrupt) is " + e.getNewValue());
                            }
                            return;
                        default:
                            if (log.isDebugEnabled()) {
                                log.debug("SerialEvent of unknown type: " + type + " value: " + e.getNewValue());
                            }
                            return;
                    }
                }
            }
            );
            try {
                activeSerialPort.notifyOnFramingError(true);
            } catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.debug("Could not notifyOnFramingError: " + e);
                }
            }

            try {
                activeSerialPort.notifyOnBreakInterrupt(true);
            } catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.debug("Could not notifyOnBreakInterrupt: " + e);
                }
            }

            try {
                activeSerialPort.notifyOnParityError(true);
            } catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.debug("Could not notifyOnParityError: " + e);
                }
            }

            try {
                activeSerialPort.notifyOnOutputEmpty(true);
            } catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.debug("Could not notifyOnOutputEmpty: " + e);
                }
            }

            try {
                activeSerialPort.notifyOnOverrunError(true);
            } catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.debug("Could not notifyOnOverrunError: " + e);
                }
            }

            opened = true;

        } catch (gnu.io.NoSuchPortException p) {
            return handlePortNotFound(p, portName, log);
        } catch (Exception ex) {
            log.error("Unexpected exception while opening port " + portName + " trace follows: " + ex);
            ex.printStackTrace();
            return "Unexpected error while opening port " + portName + ": " + ex;
        }

        return null; // normal operation
    }

    /**
     * set up all of the other objects to operate with a DCC++ Device connected to this
     * port
     */
    public void configure() {
        // connect to a packetizing traffic controller
	DCCppTrafficController packets = new SerialDCCppPacketizer(new DCCppCommandStation());
        packets.connectPort(this);

        // start operation
        // packets.startThreads();
        this.getSystemConnectionMemo().setDCCppTrafficController(packets);

        new DCCppInitializationManager(this.getSystemConnectionMemo());

        jmri.jmrix.dccpp.ActiveFlag.setActive();
    }

    // base class methods for the XNetSerialPortController interface
    public BufferedReader getInputStreamBR() {
        if (!opened) {
            log.error("getInputStream called before load(), stream not available");
            return null;
        }
        return new BufferedReader(new InputStreamReader(serialStream));
    }

    public DataInputStream getInputStream() {
	//log.error("Not Using DataInputStream version anymore!");
    	//return(null);
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

    public boolean status() {
        return opened;
    }

    /**
     * Local method to do specific configuration
     */
    protected void setSerialPort() throws gnu.io.UnsupportedCommOperationException {
        // find the baud rate value, configure comm options
        int baud = validSpeedValues[0];  // default, but also defaulted in the initial value of selectedSpeed
        for (int i = 0; i < validSpeeds.length; i++) {
            if (validSpeeds[i].equals(mBaudRate)) {
                baud = validSpeedValues[i];
            }
        }
        SerialUtil.setSerialPortParams(activeSerialPort, baud,
                SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1,
                SerialPort.PARITY_NONE);

        // set RTS high, DTR high - done early, so flow control can be configured after
        activeSerialPort.setRTS(true);		// not connected in some serial ports and adapters
        activeSerialPort.setDTR(true);		// pin 1 in DIN8; on main connector, this is DTR

        // find and configure flow control
        //int flow = SerialPort.FLOWCONTROL_RTSCTS_OUT; // default, but also deftaul for getOptionState(option1Name)
        int flow = SerialPort.FLOWCONTROL_NONE;
//        if (!getOptionState(option1Name).equals(validOption1[0])) {
//            flow = SerialPort.FLOWCONTROL_NONE;
//        }
        activeSerialPort.setFlowControlMode(flow);
        //if (getOptionState(option2Name).equals(validOption2[0]))
        //    checkBuffer = true;
    }

    /**
     * Get an array of valid baud rates. This is currently just a message saying
     * its fixed
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "EI_EXPOSE_REP") // OK to expose array instead of copy until Java 1.6
    public String[] validBaudRates() {
        return validSpeeds;
    }

    protected String[] validSpeeds = new String[]{"115,200 baud"};
    protected int[] validSpeedValues = new int[]{115200};

    // meanings are assigned to these above, so make sure the order is consistent
//    protected String[] validOption1 = new String[]{"hardware flow control", "no flow control"};
    protected String[] validOption1 = new String[]{"no flow control"};

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

    private final static Logger log = LoggerFactory.getLogger(DCCppAdapter.class.getName());

}

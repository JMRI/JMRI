package jmri.jmrix.powerline.serialdriver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import jmri.jmrix.powerline.SerialPortController;
import jmri.jmrix.powerline.SerialSystemConnectionMemo;
import jmri.jmrix.powerline.SerialTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import purejavacomm.CommPortIdentifier;
import purejavacomm.NoSuchPortException;
import purejavacomm.PortInUseException;
import purejavacomm.SerialPort;
import purejavacomm.UnsupportedCommOperationException;

/**
 * Provide access to Powerline devices via a serial com port.
 * Derived from the Oaktree code.
 *
 * @author Bob Jacobsen Copyright (C) 2006, 2007, 2008
 * @author Ken Cameron, (C) 2009, sensors from poll replies Converted to multiple connection
 * @author kcameron Copyright (C) 2011
 */
public class SerialDriverAdapter extends SerialPortController {

    SerialPort activeSerialPort = null;

    public SerialDriverAdapter() {
        super(new SerialSystemConnectionMemo());
        option1Name = "Adapter"; // NOI18N
        options.put(option1Name, new Option("Adapter", stdOption1Values));
        this.manufacturerName = jmri.jmrix.powerline.SerialConnectionTypeList.POWERLINE;
    }

    @Override
    public String openPort(String portName, String appName) {
        try {
            // get and open the primary port
            CommPortIdentifier portID = CommPortIdentifier.getPortIdentifier(portName);
            try {
                activeSerialPort = (SerialPort) portID.open(appName, 2000); // name of program, msec to wait
            } catch (PortInUseException p) {
                return handlePortBusy(p, portName, log);
            }
            // try to set it for serial
            try {
                setSerialPort();
            } catch (UnsupportedCommOperationException e) {
                log.error("Cannot set serial parameters on port {}: {}", portName, e.getMessage());
                return "Cannot set serial parameters on port " + portName + ": " + e.getMessage();
            }

            // set framing (end) character
            try {
                log.debug("Serial framing was observed as: {} {}", activeSerialPort.isReceiveFramingEnabled(),
                        activeSerialPort.getReceiveFramingByte());
            } catch (Exception ef) {
                log.debug("failed to set serial framing: ", ef);
            }

            // set timeout; framing should work before this anyway
            try {
                activeSerialPort.enableReceiveTimeout(10);
                log.debug("Serial timeout was observed as: " + activeSerialPort.getReceiveTimeout()
                        + " " + activeSerialPort.isReceiveTimeoutEnabled());
            } catch (Exception et) {
                log.info("failed to set serial timeout: ", et);
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
                log.debug(" port flow control shows {}", // NOI18N
                        (activeSerialPort.getFlowControlMode() == SerialPort.FLOWCONTROL_RTSCTS_OUT ? "hardware flow control" : "no flow control")); // NOI18N

                // log events
                setPortEventLogging(activeSerialPort);
            }

            opened = true;

        } catch (NoSuchPortException p) {
            return handlePortNotFound(p, portName, log);
        } catch (IOException ex) {
            log.error("Unexpected exception while opening port {} ", portName, ex);
            return "Unexpected error while opening port " + portName + ": " + ex;
        }

        return null; // normal operation
    }

    /**
     * Can the port accept additional characters? Yes, always
     * @return boolean of xmit port status
     */
    public boolean okToSend() {
        return true;
    }

    /**
     * Set up all of the other objects to operate connected to this port.
     */
    @Override
    public void configure() {
        SerialTrafficController tc = null;
        // set up the system connection first
        String opt1 = getOptionState(option1Name);
        if (opt1.equals("CM11")) {
            // create a CM11 port controller
            this.setSystemConnectionMemo(new jmri.jmrix.powerline.cm11.SpecificSystemConnectionMemo());
            tc = new jmri.jmrix.powerline.cm11.SpecificTrafficController(this.getSystemConnectionMemo());
        } else if (opt1.equals("CP290")) {
            // create a CP290 port controller
            this.setSystemConnectionMemo(new jmri.jmrix.powerline.cp290.SpecificSystemConnectionMemo());
            tc = new jmri.jmrix.powerline.cp290.SpecificTrafficController(this.getSystemConnectionMemo());
        } else if (opt1.equals("Insteon 2412S")) {
            // create an Insteon 2412s port controller
            this.setSystemConnectionMemo(new jmri.jmrix.powerline.insteon2412s.SpecificSystemConnectionMemo());
            tc = new jmri.jmrix.powerline.insteon2412s.SpecificTrafficController(this.getSystemConnectionMemo());
        } else {
            // no connection at all - warn
            log.warn("protocol option {} defaults to CM11", opt1);
            // create a CM11 port controller
            this.setSystemConnectionMemo(new jmri.jmrix.powerline.cm11.SpecificSystemConnectionMemo());
            tc = new jmri.jmrix.powerline.cm11.SpecificTrafficController(this.getSystemConnectionMemo());
        }

        // connect to the traffic controller
        this.getSystemConnectionMemo().setTrafficController(tc);
        tc.setAdapterMemo(this.getSystemConnectionMemo());
        this.getSystemConnectionMemo().configureManagers();
        tc.connectPort(this);
        // Configure the form of serial address validation for this connection
        this.getSystemConnectionMemo().setSerialAddress(new jmri.jmrix.powerline.SerialAddress(this.getSystemConnectionMemo()));
    }

    // base class methods for the SerialPortController interface
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
            log.error("getOutputStream exception: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public boolean status() {
        return opened;
    }

    String[] stdOption1Values = new String[]{"CM11", "CP290", "Insteon 2412S"}; // NOI18N

    /**
     * Local method to do specific port configuration
     * @throws purejavacomm.UnsupportedCommOperationException used if invalid parms
     */
    protected void setSerialPort() throws UnsupportedCommOperationException {
        // find the baud rate value, configure comm options
        int baud = currentBaudNumber(mBaudRate);

        // check for specific port type
        String opt1 = getOptionState(option1Name);
        if (opt1.equals("CM11")) {
            // leave as 4800 baud
        } else if (opt1.equals("CP290")) {
            // set to 600 baud
            baud = 600;
        } else if (opt1.equals("Insteon 2412S")) {
            // set to 19200 baud
            baud = 19200;
        }

        activeSerialPort.setSerialPortParams(baud, SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

        // set RTS high, DTR high
        // find and configure flow control
        int flow = SerialPort.FLOWCONTROL_NONE; // default
        configureLeadsAndFlowControl(activeSerialPort, flow);
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

    protected String[] validSpeeds = new String[]{Bundle.getMessage("BaudAutomatic")};
    protected int[] validSpeedValues = new int[]{4800};

    @Override
    public int defaultBaudIndex() {
        return 0;
    }

    // private control members
    private boolean opened = false;
    InputStream serialStream = null;

    private final static Logger log = LoggerFactory.getLogger(SerialDriverAdapter.class);

}

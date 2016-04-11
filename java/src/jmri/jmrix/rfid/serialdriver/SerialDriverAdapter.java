package jmri.jmrix.rfid.serialdriver;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.TooManyListenersException;
import jmri.jmrix.rfid.RfidPortController;
import jmri.jmrix.rfid.RfidProtocol;
import jmri.jmrix.rfid.RfidSystemConnectionMemo;
import jmri.jmrix.rfid.RfidTrafficController;
import jmri.jmrix.rfid.generic.standalone.StandaloneReporterManager;
import jmri.jmrix.rfid.generic.standalone.StandaloneSensorManager;
import jmri.jmrix.rfid.generic.standalone.StandaloneTrafficController;
import jmri.jmrix.rfid.merg.concentrator.ConcentratorReporterManager;
import jmri.jmrix.rfid.merg.concentrator.ConcentratorSensorManager;
import jmri.jmrix.rfid.merg.concentrator.ConcentratorTrafficController;
import jmri.jmrix.rfid.protocol.coreid.CoreIdRfidProtocol;
import jmri.jmrix.rfid.protocol.em18.Em18RfidProtocol;
import jmri.jmrix.rfid.protocol.olimex.OlimexRfidProtocol;
import jmri.jmrix.rfid.protocol.parallax.ParallaxRfidProtocol;
import jmri.jmrix.rfid.protocol.seeedstudio.SeeedStudioRfidProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide access to RFID devices via a serial comm port. Derived from the
 * oaktree code.
 *
 * @author Bob Jacobsen Copyright (C) 2006, 2007, 2008
 * @author Matthew Harris Copyright (C) 2011
 * @author Oscar A. Pruitt Copyright (C) 2015
 * @since 2.11.4
 */
public class SerialDriverAdapter extends RfidPortController implements jmri.jmrix.SerialPortAdapter {

    SerialPort activeSerialPort = null;

    public SerialDriverAdapter() {
        super(new RfidSystemConnectionMemo());
        option1Name = "Adapter";
        option2Name = "Concentrator-Range";
        option3Name = "Protocol";
        options.put(option1Name, new Option("Adapter:", new String[]{"Generic Stand-alone", "MERG Concentrator"}, false));
        options.put(option2Name, new Option("Concentrator range:", new String[]{"A-H", "I-P"}, false));
        options.put(option3Name, new Option("Protocol:", new String[]{"CORE-ID", "Olimex", "Parallax", "SeeedStudio", "EM-18"}, false));
        this.manufacturerName = jmri.jmrix.rfid.RfidConnectionTypeList.RFID;
    }

    @Override
    @SuppressWarnings("CallToPrintStackTrace")
    public String openPort(String portName, String appName) {
        try {
            // get and open the primary port
            CommPortIdentifier portID = CommPortIdentifier.getPortIdentifier(portName);
            try {
                activeSerialPort = (SerialPort) portID.open(appName, 2000);  // name of program, msec to wait
            } catch (PortInUseException p) {
                return handlePortBusy(p, portName, log);
            }
            // try to set it for serial
            try {
                setSerialPort();
            } catch (gnu.io.UnsupportedCommOperationException e) {
                log.error("Cannot set serial parameters on port " + portName + ": " + e.getMessage());
                return "Cannot set serial parameters on port " + portName + ": " + e.getMessage();
            }

            // set framing (end) character
            try {
                log.debug("Serial framing was observed as: " + activeSerialPort.isReceiveFramingEnabled()
                        + " " + activeSerialPort.getReceiveFramingByte());
            } catch (Exception ef) {
                log.debug("failed to set serial framing: " + ef);
            }

            // set timeout; framing should work before this anyway
            try {
                activeSerialPort.enableReceiveTimeout(10);
                log.debug("Serial timeout was observed as: " + activeSerialPort.getReceiveTimeout()
                        + " " + activeSerialPort.isReceiveTimeoutEnabled());
            } catch (UnsupportedCommOperationException et) {
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
                log.debug(" port flow control shows "
                        + (activeSerialPort.getFlowControlMode() == SerialPort.FLOWCONTROL_RTSCTS_OUT ? "hardware flow control" : "no flow control"));
            }
            if (log.isDebugEnabled()) {
                // arrange to notify later
                activeSerialPort.addEventListener(new SerialPortEventListener() {
                    @Override
                    public void serialEvent(SerialPortEvent e) {
                        int type = e.getEventType();
                        switch (type) {
                            case SerialPortEvent.DATA_AVAILABLE:
                                log.info("SerialEvent: DATA_AVAILABLE is " + e.getNewValue());
                                return;
                            case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
                                log.info("SerialEvent: OUTPUT_BUFFER_EMPTY is " + e.getNewValue());
                                return;
                            case SerialPortEvent.CTS:
                                log.info("SerialEvent: CTS is " + e.getNewValue());
                                return;
                            case SerialPortEvent.DSR:
                                log.info("SerialEvent: DSR is " + e.getNewValue());
                                return;
                            case SerialPortEvent.RI:
                                log.info("SerialEvent: RI is " + e.getNewValue());
                                return;
                            case SerialPortEvent.CD:
                                log.info("SerialEvent: CD is " + e.getNewValue());
                                return;
                            case SerialPortEvent.OE:
                                log.info("SerialEvent: OE (overrun error) is " + e.getNewValue());
                                return;
                            case SerialPortEvent.PE:
                                log.info("SerialEvent: PE (parity error) is " + e.getNewValue());
                                return;
                            case SerialPortEvent.FE:
                                log.info("SerialEvent: FE (framing error) is " + e.getNewValue());
                                return;
                            case SerialPortEvent.BI:
                                log.info("SerialEvent: BI (break interrupt) is " + e.getNewValue());
                                return;
                            default:
                                log.info("SerialEvent of unknown type: " + type + " value: " + e.getNewValue());
                        }
                    }
                }
                );
                try {
                    activeSerialPort.notifyOnFramingError(true);
                } catch (Exception e) {
                    log.debug("Could not notifyOnFramingError: " + e);
                }

                try {
                    activeSerialPort.notifyOnBreakInterrupt(true);
                } catch (Exception e) {
                    log.debug("Could not notifyOnBreakInterrupt: " + e);
                }

                try {
                    activeSerialPort.notifyOnParityError(true);
                } catch (Exception e) {
                    log.debug("Could not notifyOnParityError: " + e);
                }

                try {
                    activeSerialPort.notifyOnOverrunError(true);
                } catch (Exception e) {
                    log.debug("Could not notifyOnOverrunError: " + e);
                }

            }

            opened = true;

        } catch (gnu.io.NoSuchPortException p) {
            return handlePortNotFound(p, portName, log);
        } catch (IOException ex) {
            log.error("Unexpected exception while opening port " + portName + " trace follows: " + ex);
            ex.printStackTrace();
            return "Unexpected error while opening port " + portName + ": " + ex;
        } catch (TooManyListenersException ex) {
            log.error("Unexpected exception while opening port " + portName + " trace follows: " + ex);
            ex.printStackTrace();
            return "Unexpected error while opening port " + portName + ": " + ex;
        }

        return null; // normal operation
    }

    /**
     * Can the port accept additional characters? Yes, always
     *
     * @return True if OK
     */
    public boolean okToSend() {
        return true;
    }

    /**
     * set up all of the other objects to operate connected to this port
     */
    @Override
    public void configure() {
        RfidTrafficController control;
        RfidProtocol protocol;

        // set up the system connection first
        String opt1 = getOptionState(option1Name);
        switch (opt1) {
            case "Generic Stand-alone":
                // create a Generic Stand-alone port controller
                log.debug("Create Generic Standalone SpecificTrafficController");
                control = new StandaloneTrafficController(this.getSystemConnectionMemo());
                this.getSystemConnectionMemo().configureManagers(
                        new StandaloneSensorManager(control, this.getSystemPrefix()),
                        new StandaloneReporterManager(control, this.getSystemPrefix()));
                break;
            case "MERG Concentrator":
                // create a MERG Concentrator port controller
                log.debug("Create MERG Concentrator SpecificTrafficController");
                control = new ConcentratorTrafficController(this.getSystemConnectionMemo(), getOptionState(option2Name));
                this.getSystemConnectionMemo().configureManagers(
                        new ConcentratorSensorManager(control, this.getSystemPrefix()),
                        new ConcentratorReporterManager(control, this.getSystemPrefix()));
                break;
            default:
                // no connection at all - warn
                log.warn("adapter option " + opt1 + " defaults to Generic Stand-alone");
                // create a Generic Stand-alone port controller
                control = new StandaloneTrafficController(this.getSystemConnectionMemo());
                this.getSystemConnectionMemo().configureManagers(
                        new StandaloneSensorManager(control, this.getSystemPrefix()),
                        new StandaloneReporterManager(control, this.getSystemPrefix()));
                break;
        }

        // Now do the protocol
        String opt3 = getOptionState(option3Name);
        if (opt1.equals("MERG Concentrator")) {
            // MERG Concentrator only supports CORE-ID
            log.info("set protocol to CORE-ID");
            String opt2 = getOptionState(option2Name);
            switch (opt2) {
                case "A-H" :
                    log.info("set concentrator range to 'A-H' at position 1");
                    protocol = new CoreIdRfidProtocol('A', 'H', 1);
                    break;
                case "I-P" :
                    log.info("set concentrator range to 'I-P' at position 1");
                    protocol = new CoreIdRfidProtocol('I', 'P', 1);
                    break;
                default :
                    // unrecognised concentrator range - warn
                    log.warn("concentrator range '{}' not supported - default to no concentrator", opt2);
                    protocol = new CoreIdRfidProtocol();
                    break;
            }
        } else {
            switch (opt3) {
                case "CORE-ID":
                    log.info("set protocol to CORE-ID");
                    protocol = new CoreIdRfidProtocol();
                    break;
                case "Olimex":
                    log.info("set protocol to Olimex");
                    protocol = new OlimexRfidProtocol();
                    break;
                case "Parallax":
                    log.info("set protocol to Parallax");
                    protocol = new ParallaxRfidProtocol();
                    break;
                case "SeeedStudio":
                    log.info("set protocol to SeeedStudio");
                    protocol = new SeeedStudioRfidProtocol();
                    break;
                case "EM-18":
                    log.info("set protocol to EM-18");
                    protocol = new Em18RfidProtocol();
                    break;
                default:
                    // no protocol at all - warn
                    log.warn("protocol option " + opt3 + " defaults to CORE-ID");
                    // create a coreid protocol
                    protocol = new CoreIdRfidProtocol();
                    break;
            }
        }
        this.getSystemConnectionMemo().setProtocol(protocol);

        // connect to the traffic controller
        this.getSystemConnectionMemo().setRfidTrafficController(control);
        control.setAdapterMemo(this.getSystemConnectionMemo());
        control.connectPort(this);
        control.sendInitString();

        // declare up
        jmri.jmrix.rfid.ActiveFlag.setActive();
    }

    // base class methods for the RfidPortController interface
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
            log.error("getOutputStream exception: " + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean status() {
        return opened;
    }

    /**
     * Local method to do specific port configuration
     *
     * @throws gnu.io.UnsupportedCommOperationException
     */
    protected void setSerialPort() throws gnu.io.UnsupportedCommOperationException {
        // find the baud rate value, configure comm options
        int baud = 9600;  // default, but also defaulted in the initial value of selectedSpeed

        // check for specific port type
        activeSerialPort.setSerialPortParams(baud, SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

        // set RTS high, DTR high - done early, so flow control can be configured after
        activeSerialPort.setRTS(true);          // not connected in some serial ports and adapters
        activeSerialPort.setDTR(true);          // pin 1 in DIN8; on main connector, this is DTR

        // find and configure flow control
        int flow = SerialPort.FLOWCONTROL_NONE; // default
        if (getOptionState(option1Name).equals("MERG Concentrator")) {
            // Set Hardware Flow Control for Concentrator
            log.debug("Set hardware flow control for Concentrator");
            flow = SerialPort.FLOWCONTROL_RTSCTS_OUT;
        }
        activeSerialPort.setFlowControlMode(flow);
    }

    /**
     * Get an array of valid baud rates.
     *
     * @return list of rates
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "EI_EXPOSE_REP")
    @Override
    public String[] validBaudRates() {
        return validSpeeds;
    }

    /**
     * Set the baud rate.
     *
     * @param rate
     */
    @Override
    public void configureBaudRate(String rate) {
        log.debug("configureBaudRate: " + rate);
        selectedSpeed = rate;
        super.configureBaudRate(rate);
    }

    protected String[] validSpeeds = new String[]{"(automatic)"};
    protected int[] validSpeedValues = new int[]{9600};
    protected String selectedSpeed = validSpeeds[0];

    // private control members
    private boolean opened = false;
    InputStream serialStream = null;

    private static final Logger log = LoggerFactory.getLogger(SerialDriverAdapter.class.getName());

}

// SerialDriverAdapter.java
package jmri.jmrix.grapevine.serialdriver;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import jmri.jmrix.grapevine.SerialPortController;
import jmri.jmrix.grapevine.SerialSensorManager;
import jmri.jmrix.grapevine.SerialTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide access to ProTrak Grapevine via a serial comm port. Normally
 * controlled by the serialdriver.SerialDriverFrame class.
 *
 * @author	Bob Jacobsen Copyright (C) 2006, 2007
 * @version	$Revision$
 */
public class SerialDriverAdapter extends SerialPortController implements jmri.jmrix.SerialPortAdapter {

    SerialPort activeSerialPort = null;

    public SerialDriverAdapter() {
        super(null);
        this.manufacturerName = jmri.jmrix.grapevine.SerialConnectionTypeList.PROTRAK;
    }

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

            // set timeout; no framing in Grapevine
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
            if (log.isDebugEnabled()) {
                // arrange to notify later
                activeSerialPort.addEventListener(new SerialPortEventListener() {
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
                                return;
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
        } catch (Exception ex) {
            log.error("Unexpected exception while opening port " + portName + " trace follows: " + ex);
            ex.printStackTrace();
            return "Unexpected error while opening port " + portName + ": " + ex;
        }

        return null; // normal operation
    }

    /**
     * Can the port accept additional characters? Yes, always
     */
    public boolean okToSend() {
        return true;
    }

    /**
     * set up all of the other objects to operate connected to this port
     */
    public void configure() {
        // connect to the traffic controller
        SerialTrafficController.instance().connectPort(this);

        jmri.InstanceManager.setTurnoutManager(jmri.jmrix.grapevine.SerialTurnoutManager.instance());
        jmri.InstanceManager.setLightManager(jmri.jmrix.grapevine.SerialLightManager.instance());

        SerialSensorManager s;
        jmri.InstanceManager.setSensorManager(s = jmri.jmrix.grapevine.SerialSensorManager.instance());
        SerialTrafficController.instance().setSensorManager(s);
        jmri.jmrix.grapevine.ActiveFlag.setActive();
    }

    // base class methods for the SerialPortController interface
    public DataInputStream getInputStream() {
        if (!opened) {
            log.error("getInputStream called before load(), stream not available");
            return null;
        }
        return new DataInputStream(serialStream);
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
     * Local method to do specific port configuration
     */
    protected void setSerialPort() throws gnu.io.UnsupportedCommOperationException {
        // find the baud rate value, configure comm options
        int baud = 38400;  // default, but also defaulted in the initial value of selectedSpeed
        for (int i = 0; i < validSpeeds.length; i++) {
            if (validSpeeds[i].equals(selectedSpeed)) {
                baud = validSpeedValues[i];
            }
        }
        activeSerialPort.setSerialPortParams(baud, SerialPort.DATABITS_8,
                SerialPort.STOPBITS_2, SerialPort.PARITY_NONE);

        // set RTS high, DTR high - done early, so flow control can be configured after
        activeSerialPort.setRTS(true);		// not connected in some serial ports and adapters
        activeSerialPort.setDTR(true);		// pin 1 in DIN8; on main connector, this is DTR

        // find and configure flow control
        int flow = SerialPort.FLOWCONTROL_NONE; // default
        activeSerialPort.setFlowControlMode(flow);
    }

    /**
     * Get an array of valid baud rates.
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "EI_EXPOSE_REP") // OK to expose array instead of copy until Java 1.6
    public String[] validBaudRates() {
        return validSpeeds;
    }

    /**
     * Set the baud rate.
     */
    public void configureBaudRate(String rate) {
        log.debug("configureBaudRate: " + rate);
        selectedSpeed = rate;
        super.configureBaudRate(rate);
    }

    protected String[] validSpeeds = new String[]{"38,400 baud"};
    protected int[] validSpeedValues = new int[]{38400};
    protected String selectedSpeed = validSpeeds[0];

    // private control members
    private boolean opened = false;
    InputStream serialStream = null;

    static public SerialDriverAdapter instance() {
        if (mInstance == null) {
            mInstance = new SerialDriverAdapter();
        }
        return mInstance;
    }
    static SerialDriverAdapter mInstance = null;

    private final static Logger log = LoggerFactory.getLogger(SerialDriverAdapter.class.getName());

}

// XBeeAdapter.java
package jmri.jmrix.ieee802154.xbee;

import com.rapplogic.xbee.XBeeConnection;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide access to IEEE802.15.4 devices via a serial comm port.
 *
 * @author Paul Bender Copyright (C) 2013
 * @version	$Revision$
 */
public class XBeeAdapter extends jmri.jmrix.ieee802154.serialdriver.SerialDriverAdapter implements jmri.jmrix.SerialPortAdapter, XBeeConnection, SerialPortEventListener {

    public XBeeAdapter() {
        super(new XBeeConnectionMemo());
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
            // try to set it for serial
            try {
                setSerialPort();
            } catch (gnu.io.UnsupportedCommOperationException e) {
                log.error("Cannot set serial parameters on port " + portName + ": " + e.getMessage());
                return "Cannot set serial parameters on port " + portName + ": " + e.getMessage();
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

    public void serialEvent(SerialPortEvent e) {
        int type = e.getEventType();
        try {
            if (type == SerialPortEvent.DATA_AVAILABLE) {
                if (this.getInputStream().available() > 0) {
                    if (log.isDebugEnabled()) {
                        log.debug("SerialEvent: DATA_AVAILABLE is " + e.getNewValue());
                    }
                    synchronized (this) {
                        this.notify();
                    }
                } else {
                    log.warn("SerialEvent: DATA_AVAILABLE but no data available.");
                }
                return;
            } else if (log.isDebugEnabled()) {
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
        } catch (java.io.IOException ex) {
            // it's best not to throw the exception because the RXTX thread may not be prepared to handle
            log.error("RXTX error in serialEvent method", ex);
        }
    }

    /**
     * Local method to do specific port configuration
     */
    @Override
    protected void setSerialPort() throws gnu.io.UnsupportedCommOperationException {
        // find the baud rate value, configure comm options
        int baud = validSpeedValues[0];  // default, but also defaulted in the initial value of selectedSpeed
        for (int i = 0; i < validSpeeds.length; i++) {
            if (validSpeeds[i].equals(mBaudRate)) {
                baud = validSpeedValues[i];
            }
        }

        activeSerialPort.setSerialPortParams(baud, SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

        // find and configure flow control
        int flow = SerialPort.FLOWCONTROL_NONE; // default
        activeSerialPort.setFlowControlMode(flow);

        if (log.isDebugEnabled()) {
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

        // The following are required for the XBee API's input thread.
        activeSerialPort.notifyOnDataAvailable(true);
        // arrange to notify later
        try {
            activeSerialPort.addEventListener(this);
        } catch (java.lang.Exception e) {
            log.error("Exception adding listener " + e);
        }
    }

    /**
     * set up all of the other objects to operate connected to this port
     */
    @Override
    public void configure() {
        log.debug("configure() called.");
        XBeeTrafficController tc = new XBeeTrafficController();

        // connect to the traffic controller
        this.getSystemConnectionMemo().setTrafficController(tc);
        tc.setAdapterMemo(this.getSystemConnectionMemo());
        //tc.setXBee(xbee);
        this.getSystemConnectionMemo().configureManagers();
        tc.connectPort(this);
        // Configure the form of serial address validation for this connection
//        adaptermemo.setSerialAddress(new jmri.jmrix.ieee802154.SerialAddress(adaptermemo));
        // declare up
        jmri.jmrix.ieee802154.ActiveFlag.setActive();
    }

    /**
     * Get an array of valid baud rates. This is currently just a message saying
     * its fixed
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "EI_EXPOSE_REP") // OK to expose array instead of copy until Java 1.6
    @Override
    public String[] validBaudRates() {
        return validSpeeds;
    }

    @Override
    public XBeeConnectionMemo getSystemConnectionMemo() {
        return (XBeeConnectionMemo) super.getSystemConnectionMemo();
    }

    protected String[] validSpeeds = new String[]{"1,200 baud", "2,400 baud",
        "4,800 baud", "9,600 baud", "19,200 baud", "38,400 baud",
        "57,600 baud", "115,200 baud"};
    protected int[] validSpeedValues = new int[]{1200, 2400, 4800, 9600, 19200,
        38400, 57600, 115200};

    // methods for XBeeConnection
    public void close() {
        activeSerialPort.close();
    }

    private final static Logger log = LoggerFactory.getLogger(XBeeAdapter.class.getName());

}

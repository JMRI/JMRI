package jmri.jmrix.ieee802154.xbee;

import com.digi.xbee.api.connection.IConnectionInterface;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import purejavacomm.*;

/**
 * Provide access to IEEE802.15.4 devices via a serial com port.
 *
 * @author Paul Bender Copyright (C) 2013
 */
public class XBeeAdapter extends jmri.jmrix.ieee802154.serialdriver.SerialDriverAdapter implements IConnectionInterface, SerialPortEventListener {

    private boolean iConnectionOpened = false;

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
            } catch (UnsupportedCommOperationException e) {
                log.error("Cannot set serial parameters on port {}: {}", portName, e.getMessage());
                return "Cannot set serial parameters on port " + portName + ": " + e.getMessage();
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
     *
     */
    @SuppressFBWarnings(value = {"NO_NOTIFY_NOT_NOTIFYALL","NN_NAKED_NOTIFY"}, justification="The notify call is notifying the receive thread that data is available.  There is only one receive thead, so no reason to call notifyAll.")
    @Override
    public void serialEvent(SerialPortEvent e) {
        int type = e.getEventType();
        try {
            if (type == SerialPortEvent.DATA_AVAILABLE) {
                if (this.getInputStream().available() > 0) {
                    if (log.isDebugEnabled()) {
                        log.debug("SerialEvent: DATA_AVAILABLE is {}", e.getNewValue());
                    }
                    synchronized (this) {
                        this.notifyAll();
                    }
                } else {
                    log.warn("SerialEvent: DATA_AVAILABLE but no data available.");
                }
                return;
            } else if (log.isDebugEnabled()) {
                switch (type) {
                    case SerialPortEvent.DATA_AVAILABLE:
                        log.info("SerialEvent: DATA_AVAILABLE is {}", e.getNewValue());
                        return;
                    case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
                        log.info("SerialEvent: OUTPUT_BUFFER_EMPTY is {}", e.getNewValue());
                        return;
                    case SerialPortEvent.CTS:
                        log.info("SerialEvent: CTS is {}", e.getNewValue());
                        return;
                    case SerialPortEvent.DSR:
                        log.info("SerialEvent: DSR is {}", e.getNewValue());
                        return;
                    case SerialPortEvent.RI:
                        log.info("SerialEvent: RI is {}", e.getNewValue());
                        return;
                    case SerialPortEvent.CD:
                        log.info("SerialEvent: CD is {}", e.getNewValue());
                        return;
                    case SerialPortEvent.OE:
                        log.info("SerialEvent: OE (overrun error) is {}", e.getNewValue());
                        return;
                    case SerialPortEvent.PE:
                        log.info("SerialEvent: PE (parity error) is {}", e.getNewValue());
                        return;
                    case SerialPortEvent.FE:
                        log.info("SerialEvent: FE (framing error) is {}", e.getNewValue());
                        return;
                    case SerialPortEvent.BI:
                        log.info("SerialEvent: BI (break interrupt) is {}", e.getNewValue());
                        return;
                    default:
                        log.info("SerialEvent of unknown type: {} value: {}", type, e.getNewValue());
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
    protected void setSerialPort() throws UnsupportedCommOperationException {
        log.debug("setSerialPort() called.");
        // find the baud rate value, configure comm options
        int baud = currentBaudNumber(mBaudRate);
        activeSerialPort.setSerialPortParams(baud, SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

        // find and configure flow control
        int flow = SerialPort.FLOWCONTROL_NONE; // default
        configureLeadsAndFlowControl(activeSerialPort, flow);

        if (log.isDebugEnabled()) {
            activeSerialPort.notifyOnFramingError(true);
            activeSerialPort.notifyOnBreakInterrupt(true);
            activeSerialPort.notifyOnParityError(true);
            activeSerialPort.notifyOnOverrunError(true);
        }

        activeSerialPort.enableReceiveTimeout(10);

        // The following are required for the XBee API's input thread.
        activeSerialPort.notifyOnDataAvailable(true);

        // arrange to notify later
        try {
            activeSerialPort.addEventListener(this);
        } catch (java.util.TooManyListenersException e) {
            log.error("Exception adding listener ", e);
        }
    }

    /**
     * Set up all of the other objects to operate connected to this port.
     */
    @Override
    public void configure() {
        log.debug("configure() called.");
        XBeeTrafficController tc = new XBeeTrafficController();

        // connect to the traffic controller
        this.getSystemConnectionMemo().setTrafficController(tc);
        tc.setAdapterMemo(this.getSystemConnectionMemo());
        tc.connectPort(this);
        this.getSystemConnectionMemo().configureManagers();
        // Configure the form of serial address validation for this connection
        // adaptermemo.setSerialAddress(new jmri.jmrix.ieee802154.SerialAddress(adaptermemo));
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

    @Override
    public XBeeConnectionMemo getSystemConnectionMemo() {
        jmri.jmrix.ieee802154.IEEE802154SystemConnectionMemo m = super.getSystemConnectionMemo();
        if (m instanceof XBeeConnectionMemo ) {
           return (XBeeConnectionMemo) m;
        } else {
           throw new java.lang.IllegalArgumentException("System Connection Memo associated with this connection is not the right type.");
        }
    }

    private String[] validSpeeds = new String[]{Bundle.getMessage("Baud1200"),
            Bundle.getMessage("Baud2400"), Bundle.getMessage("Baud4800"),
            Bundle.getMessage("Baud9600"), Bundle.getMessage("Baud19200"),
            Bundle.getMessage("Baud38400"), Bundle.getMessage("Baud57600"),
            Bundle.getMessage("Baud115200")};
    private int[] validSpeedValues = new int[]{1200, 2400, 4800, 9600, 19200, 38400, 57600, 115200};

    @Override
    public int defaultBaudIndex() {
        return 0;
    }

    // methods for IConnectionInterface

    @Override
    public void close() {
        activeSerialPort.close();
        iConnectionOpened = false;
    }

    @Override
    public int readData(byte[] b) throws java.io.IOException {
       log.debug("read data called with {}", b);
       return serialStream.read(b);
    }

    @Override
    public int readData(byte[] b,int off, int len) throws java.io.IOException {
       log.debug("read data called with {} {} {}", b, off, len);
       return serialStream.read(b,off,len);
    }

    @Override
    public void writeData(byte[] b) throws java.io.IOException {
       log.debug("write data called with {}", b);
       getOutputStream().write(b);
    }

    @Override
    public void writeData(byte[] b,int off, int len) throws java.io.IOException {
       log.debug("write data called with {} {} {}", b, off, len);
       getOutputStream().write(b,off,len);
    }

    @Override
    public boolean isOpen(){
       log.debug("isOpen called");
       return ( iConnectionOpened );
    }

    @Override
    public void open(){
       log.debug("open called");
       iConnectionOpened = true;
       // don't do anything here.  We handle the details of open through the
       // openPort call, which is called from the JMRI infrastructure.
    }

    private final static Logger log = LoggerFactory.getLogger(XBeeAdapter.class);

}

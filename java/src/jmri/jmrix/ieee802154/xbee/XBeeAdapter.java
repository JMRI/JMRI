package jmri.jmrix.ieee802154.xbee;

import com.digi.xbee.api.connection.ConnectionType;
import com.digi.xbee.api.connection.IConnectionInterface;

import java.util.Arrays;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import jmri.jmrix.SerialPort;
import jmri.jmrix.SerialPortDataListener;
import jmri.jmrix.SerialPortEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide access to IEEE802.15.4 devices via a serial com port.
 *
 * @author Paul Bender Copyright (C) 2013,2023
 */
public class XBeeAdapter extends jmri.jmrix.ieee802154.serialdriver.SerialDriverAdapter implements IConnectionInterface, SerialPortDataListener {

    private boolean iConnectionOpened = false;

    public XBeeAdapter() {
        super(new XBeeConnectionMemo());
    }

    @Override
    public String openPort(String portName, String appName) {
           // get and open the primary port
           currentSerialPort = activatePort(portName,log);
           // try to set it for serial
           setSerialPort();

        // report status
        reportPortStatus(log,portName);
        opened = true;
        return null; // normal operation
    }

    /**
     * Local method to do specific port configuration
     */
    @Override
    protected void setSerialPort() {
        log.debug("setSerialPort() called.");
        // find the baud rate value, configure comm options
        int baud = currentBaudNumber(mBaudRate);
        setBaudRate(currentSerialPort,baud);
        configureLeads(currentSerialPort,true,true);

        // The following are required for the XBee API's input thread.
        setDataListener(currentSerialPort,this);
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

    private final String[] validSpeeds = new String[]{Bundle.getMessage("Baud1200"),
            Bundle.getMessage("Baud2400"), Bundle.getMessage("Baud4800"),
            Bundle.getMessage("Baud9600"), Bundle.getMessage("Baud19200"),
            Bundle.getMessage("Baud38400"), Bundle.getMessage("Baud57600"),
            Bundle.getMessage("Baud115200")};
    private final int[] validSpeedValues = new int[]{1200, 2400, 4800, 9600, 19200, 38400, 57600, 115200};

    @Override
    public int defaultBaudIndex() {
        return 0;
    }

    // methods for IConnectionInterface

    @Override
    public void close() {
        closeSerialPort(currentSerialPort);
        iConnectionOpened = false;
    }

    @Override
    public int readData(byte[] b) throws java.io.IOException {
       log.debug("read data called with {}", b);
       return getInputStream().read(b);
    }

    @Override
    public int readData(byte[] b,int off, int len) throws java.io.IOException {
       log.debug("read data called with {} {} {}", b, off, len);
       return getInputStream().read(b,off,len);
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

    @Override
    public ConnectionType getConnectionType() {
        return ConnectionType.UNKNOWN;
    }

    // SerialPortEventListener methods
    @Override
    public int getListeningEvents() {
        return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
    }

    @SuppressFBWarnings(value = {"NN_NAKED_NOTIFY"}, justification="The notify call is notifying the receive thread that data is available due to an event.")
    @Override
    public void serialEvent(SerialPortEvent serialPortEvent) {
        if (serialPortEvent.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE)
            return;
        synchronized (this) {
            this.notifyAll();
        }
    }

    private final static Logger log = LoggerFactory.getLogger(XBeeAdapter.class);

}

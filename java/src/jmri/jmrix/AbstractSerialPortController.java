package jmri.jmrix;

import java.util.Enumeration;
import java.util.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import purejavacomm.CommPortIdentifier;
import purejavacomm.NoSuchPortException;
import purejavacomm.PortInUseException;
import purejavacomm.SerialPort;
import purejavacomm.SerialPortEvent;
import purejavacomm.SerialPortEventListener;

/**
 * Provide an abstract base for *PortController classes.
 * <P>
 * This is complicated by the lack of multiple inheritance. SerialPortAdapter is
 * an Interface, and its implementing classes also inherit from various
 * PortController types. But we want some common behaviours for those, so we put
 * them here.
 *
 * @see jmri.jmrix.SerialPortAdapter
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002
 */
abstract public class AbstractSerialPortController extends AbstractPortController implements SerialPortAdapter {

    protected AbstractSerialPortController(SystemConnectionMemo connectionMemo) {
        super(connectionMemo);
    }

    /**
     * Standard error handling for port-busy case.
     *
     * @param p        the exception being handled, if additional information
     *                 from it is desired
     * @param portName name of the port being accessed
     * @param log      where to log a status message
     * @return Localized message, in case separate presentation to user is
     *         desired
     */
    @Override
    public String handlePortBusy(PortInUseException p, String portName, Logger log) {
        log.error(portName + " port is in use: " + p.getMessage());
        /*JOptionPane.showMessageDialog(null, "Port is in use",
         "Error", JOptionPane.ERROR_MESSAGE);*/
        ConnectionStatus.instance().setConnectionState(this.getSystemPrefix(), portName, ConnectionStatus.CONNECTION_DOWN);
        return Bundle.getMessage("SerialPortInUse", portName);
    }

    /**
     * Standard error handling for port-not-found case.
     */
    public String handlePortNotFound(NoSuchPortException p, String portName, Logger log) {
        log.error("Serial port " + portName + " not found");
        /*JOptionPane.showMessageDialog(null, "Serial port "+portName+" not found",
         "Error", JOptionPane.ERROR_MESSAGE);*/
        ConnectionStatus.instance().setConnectionState(this.getSystemPrefix(), portName, ConnectionStatus.CONNECTION_DOWN);
        return Bundle.getMessage("SerialPortNotFound", portName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void connect() throws java.io.IOException {
        openPort(mPort, "JMRI app");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPort(String port) {
        log.debug("Setting port to " + port);
        mPort = port;
    }
    protected String mPort = null;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCurrentPortName() {
        if (mPort == null) {
            if (getPortNames() == null) {
                // This shouldn't happen in normal operation
                // but in the tests this can happen if the receive thread has been interrupted
                log.error("Port names returned as null");
                return null;
            }
            if (getPortNames().size() <= 0) {
                log.error("No usable ports returned");
                return null;
            }
            return null;
            // return (String)getPortNames().elementAt(0);
        }
        return mPort;
    }

    /**
     * Set the control leads and flow control. This handles any necessary
     * ordering.
     *
     * @param serialPort Port to be updated
     * @param flow       flow control mode from (@link purejavacomm.SerialPort}
     * @param rts        Set RTS active if true
     * @param dtr        set DTR active if true
     */
    protected void configureLeadsAndFlowControl(SerialPort serialPort, int flow, boolean rts, boolean dtr) {
        // (Jan 2018) PJC seems to mix termios and ioctl access, so it's not clear
        // what's preserved and what's not. Experimentally, it seems necessary
        // to write the control leads, set flow control, and then write the control 
        // leads again.
        serialPort.setRTS(rts);
        serialPort.setDTR(dtr);

        try {
            if (flow != purejavacomm.SerialPort.FLOWCONTROL_NONE) {
                serialPort.setFlowControlMode(flow);
            }
        } catch (purejavacomm.UnsupportedCommOperationException e) {
            log.warn("Could not set flow control, ignoring");
        }
        if (flow!=purejavacomm.SerialPort.FLOWCONTROL_RTSCTS_OUT) serialPort.setRTS(rts);  // not connected in some serial ports and adapters
        serialPort.setDTR(dtr);
    }

    /**
     * Set the flow control, while also setting RTS and DTR to active.
     *
     * @param serialPort Port to be updated
     * @param flow       flow control mode from (@link purejavacomm.SerialPort}
     */
    protected void configureLeadsAndFlowControl(SerialPort serialPort, int flow) {
        configureLeadsAndFlowControl(serialPort, flow, true, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configureBaudRate(String rate) {
        mBaudRate = rate;
    }
    protected String mBaudRate = null;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCurrentBaudRate() {
        if (mBaudRate == null) {
            return validBaudRates()[0];
        }
        return mBaudRate;
    }

    /**
     * Get an array of valid baud rates as integers. This allows subclasses to
     * change the arrays of speeds.
     * <p>
     * This method need not be reimplemented unless the subclass is using
     * currentBaudNumber, which requires it.
     */
    public int[] validBaudNumber() {
        log.error("default validBaudNumber implementation should not be used", new Exception());
        return null;
    }

    /**
     * Convert a baud rate String to a int number,e.g. "9,600" to 9600.
     * <p>
     * Uses the validBaudNumber and validBaudRates methods to do this.
     *
     * @param currentBaudRate a rate from validBaudRates
     * @return -1 if no match (configuration system should prevent this)
     */
    public int currentBaudNumber(String currentBaudRate) {
        String[] rates = validBaudRates();
        int[] numbers = validBaudNumber();

        // return if arrays invalid
        if (numbers == null) {
            log.error("numbers array null in currentBaudNumber");
            return -1;
        }
        if (rates == null) {
            log.error("rates array null in currentBaudNumber");
            return -1;
        }
        if (numbers.length < 1 || (numbers.length != rates.length)) {
            log.error("arrays wrong length in currentBaudNumber: " + numbers.length + "," + rates.length);
            return -1;
        }

        // find the baud rate value, configure comm options
        for (int i = 0; i < numbers.length; i++) {
            if (rates[i].equals(currentBaudRate)) {
                return numbers[i];
            }
        }

        // no match
        log.error("no match to (" + currentBaudRate + ") in currentBaudNumber");
        return -1;
    }


    /**
     * Set event logging
     */
    protected void setPortEventLogging(SerialPort port) {
        // arrange to notify later
        try {
            port.addEventListener(new SerialPortEventListener() {
                @Override
                public void serialEvent(SerialPortEvent e) {
                    int type = e.getEventType();
                    switch (type) {
                        case SerialPortEvent.DATA_AVAILABLE:
                            log.info("SerialEvent: DATA_AVAILABLE is " + e.getNewValue()); // NOI18N
                            return;
                        case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
                            log.info("SerialEvent: OUTPUT_BUFFER_EMPTY is " + e.getNewValue()); // NOI18N
                            return;
                        case SerialPortEvent.CTS:
                            log.info("SerialEvent: CTS is " + e.getNewValue()); // NOI18N
                            return;
                        case SerialPortEvent.DSR:
                            log.info("SerialEvent: DSR is " + e.getNewValue()); // NOI18N
                            return;
                        case SerialPortEvent.RI:
                            log.info("SerialEvent: RI is " + e.getNewValue()); // NOI18N
                            return;
                        case SerialPortEvent.CD:
                            log.info("SerialEvent: CD is " + e.getNewValue()); // NOI18N
                            return;
                        case SerialPortEvent.OE:
                            log.info("SerialEvent: OE (overrun error) is " + e.getNewValue()); // NOI18N
                            return;
                        case SerialPortEvent.PE:
                            log.info("SerialEvent: PE (parity error) is " + e.getNewValue()); // NOI18N
                            return;
                        case SerialPortEvent.FE:
                            log.info("SerialEvent: FE (framing error) is " + e.getNewValue()); // NOI18N
                            return;
                        case SerialPortEvent.BI:
                            log.info("SerialEvent: BI (break interrupt) is " + e.getNewValue()); // NOI18N
                            return;
                        default:
                            log.info("SerialEvent of unknown type: " + type + " value: " + e.getNewValue()); // NOI18N
                            return;
                    }
                }
            }
            );
        } catch (java.util.TooManyListenersException ex) {
            log.warn("cannot set listener for SerialPortEvents; was one already set?");
        }
        
        try {
            port.notifyOnFramingError(true);
        } catch (Exception e) {
            log.debug("Could not notifyOnFramingError: " + e); // NOI18N
        }

        try {
            port.notifyOnBreakInterrupt(true);
        } catch (Exception e) {
            log.debug("Could not notifyOnBreakInterrupt: " + e); // NOI18N
        }

        try {
            port.notifyOnParityError(true);
        } catch (Exception e) {
            log.debug("Could not notifyOnParityError: " + e); // NOI18N
        }

        try {
            port.notifyOnOverrunError(true);
        } catch (Exception e) {
            log.debug("Could not notifyOnOverrunError: " + e); // NOI18N
        }

        port.notifyOnCarrierDetect(true);
        port.notifyOnCTS(true);
        port.notifyOnDSR(true);

    }

    
    Vector<String> portNameVector = null;

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public Vector<String> getPortNames() {
        //reloadDriver(); // Refresh the list of communication ports
        // first, check that the comm package can be opened and ports seen
        portNameVector = new Vector<String>();
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        super.dispose();
    }

    /**
     * This is called when a connection is initially lost. It closes the client
     * side socket connection, resets the open flag and attempts a reconnection.
     */
    @Override
    public void recover() {
        if (!allowConnectionRecovery) {
            return;
        }
        opened = false;
        try {
            closeConnection();
        } catch (RuntimeException e) {
        }
        reconnect();
    }

    /*Each serial port adapter should handle this and it should be abstract.
     However this is in place until all the other code has been refactored */
    protected void closeConnection() {
        log.warn("abstract closeConnection() called; should be overriden");
    }

    /*Each port adapter should handle this and it should be abstract.
     However this is in place until all the other code has been refactored */
    protected void resetupConnection() {
        log.warn("abstract resetupConnection() called; should be overriden");
    }

    /**
     * Attempts to reconnect to a failed Server
     */
    public void reconnect() {
        // If the connection is already open, then we shouldn't try a re-connect.
        if (opened && !allowConnectionRecovery) {
            return;
        }
        ReconnectWait thread = new ReconnectWait();
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            log.error("Unable to join to the reconnection thread " + e.getMessage());
        }
        if (!opened) {
            log.error("Failed to re-establish connectivity");
        } else {
            log.info("Reconnected to " + getCurrentPortName());
            resetupConnection();
        }
    }

    class ReconnectWait extends Thread {

        public final static int THREADPASS = 0;
        public final static int THREADFAIL = 1;
        int _status;

        public int status() {
            return _status;
        }

        public ReconnectWait() {
            _status = THREADFAIL;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void run() {
            boolean reply = true;
            int count = 0;
            int secondCount = 0;
            while (reply) {
                safeSleep(reconnectinterval, "Waiting");
                count++;
                try {
                    log.error("Retrying Connection attempt " + secondCount + "-" + count);
                    Enumeration<CommPortIdentifier> portIDs = CommPortIdentifier.getPortIdentifiers();
                    while (portIDs.hasMoreElements()) {
                        CommPortIdentifier id = portIDs.nextElement();
                        // filter out line printers
                        if (id.getPortType() != CommPortIdentifier.PORT_PARALLEL) // accumulate the names in a vector
                        {
                            if (id.getName().equals(mPort)) {
                                log.info(mPort + " port has reappeared as being valid, trying to reconnect");
                                openPort(mPort, "jmri");
                            }
                        }
                    }
                } catch (RuntimeException e) {
                }
                reply = !opened;
                if (count >= retryAttempts) {
                    log.error("Unable to reconnect after " + count + " Attempts, increasing duration of retries");
                    //retrying but with twice the retry interval.
                    reconnectinterval = reconnectinterval * 2;
                    count = 0;
                    secondCount++;
                }
                if (secondCount >= 10) {
                    log.error("Giving up on reconnecting after 100 attempts to reconnect");
                    reply = false;
                }
            }
            if (!opened) {
                log.error("Failed to re-establish connectivity");
            } else {
                log.error("Reconnected to " + getCurrentPortName());
                resetupConnection();
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(AbstractSerialPortController.class);
}

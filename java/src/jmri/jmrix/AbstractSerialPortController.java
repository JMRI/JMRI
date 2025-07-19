package jmri.jmrix;


import java.io.*;
import java.util.Vector;

import jmri.SystemConnectionMemo;
import jmri.jmrix.fakeport.FakeInputStream;

/**
 * Provide an abstract base for *PortController classes.
 * <p>
 * The intent is to hide, to the extent possible, all the references to the
 * actual serial library in use within this class. Subclasses then
 * rely on methods here to maniplate the content of the
 * protected currentSerialPort variable/
 *
 * @see jmri.jmrix.SerialPortAdapter
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002, 2023
 */
abstract public class AbstractSerialPortController extends AbstractPortController implements SerialPortAdapter {

    protected AbstractSerialPortController(SystemConnectionMemo connectionMemo) {
        super(connectionMemo);
    }

    protected volatile SerialPort currentSerialPort = null;
    private final ReplaceableInputStream inputStream = new ReplaceableInputStream();
    private final ReplaceableOutputStream outputStream = new ReplaceableOutputStream();

    /**
     * Standard error handling for jmri.jmrix.purejavacomm port-busy case.
     *
     * @param p        the exception being handled, if additional information
     *                 from it is desired
     * @param portName name of the port being accessed
     * @param log      where to log a status message
     * @return Localized message, in case separate presentation to user is
     *         desired
     */
    //@Deprecated(forRemoval=true) // with jmri.jmrix.PureJavaComm
    public String handlePortBusy(jmri.jmrix.purejavacomm.PortInUseException p, String portName, org.slf4j.Logger log) {
        log.error("{} port is in use: {}", portName, p.getMessage());
        ConnectionStatus.instance().setConnectionState(this.getSystemPrefix(), portName, ConnectionStatus.CONNECTION_DOWN);
        return Bundle.getMessage("SerialPortInUse", portName);
    }

    /**
     * Specific error handling for jmri.jmrix.purejavacomm port-not-found case.
     * @param p no such port exception.
     * @param portName port name.
     * @param log system log.
     * @return human readable string with error detail.
     */
    //@Deprecated(forRemoval=true) // with jmri.jmrix.PureJavaComm
    public String handlePortNotFound(jmri.jmrix.purejavacomm.NoSuchPortException p, String portName, org.slf4j.Logger log) {
        log.error("Serial port {} not found", portName);
        ConnectionStatus.instance().setConnectionState(this.getSystemPrefix(), portName, ConnectionStatus.CONNECTION_DOWN);
        return Bundle.getMessage("SerialPortNotFound", portName);
    }

    /**
     * Standard error handling for the general port-not-found case.
     * @param systemPrefix the system prefix
     * @param portName port name.
     * @param log system log, passed so logging comes from bottom level class
     * @param ex Underlying Exception that caused this failure
     * @return human readable string with error detail.
     */
    public static String handlePortNotFound(String systemPrefix, String portName, org.slf4j.Logger log, Exception ex) {
        log.error("Serial port {} not found: {}", portName, ex.getMessage());
        if (systemPrefix != null) {
            ConnectionStatus.instance().setConnectionState(systemPrefix, portName, ConnectionStatus.CONNECTION_DOWN);
        }
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
     * Do the formal opening of the port,
     * set the port for blocking reads without timeout,
     * set the port to 8 data bits, 1 stop bit, no parity
     * and purge the port's input stream.
     * <p>
     * Does not do the rest of the setup implied in the {@link #openPort} method.
     * This is usually followed by calls to
     * {@link #setBaudRate}, {@link #configureLeads} and {@link #setFlowControl}.
     *
     * @param portName local system name for the desired port
     * @param log Logger to use for errors, passed so that errors are logged from low-level class
     * @return the serial port object for later use
     */
    final protected SerialPort activatePort(String portName, org.slf4j.Logger log) {
        return activatePort(this.getSystemPrefix(), portName, log, 1, SerialPort.Parity.NONE);
    }

    /**
     * Do the formal opening of the port,
     * set the port for blocking reads without timeout,
     * set the port to 8 data bits, the indicated number of stop bits, no parity,
     * and purge the port's input stream.
     * <p>
     * Does not do the rest of the setup implied in the {@link #openPort} method.
     * This is usually followed by calls to
     * {@link #setBaudRate}, {@link #configureLeads} and {@link #setFlowControl}.
     *
     * @param portName local system name for the desired port
     * @param log Logger to use for errors, passed so that errors are logged from low-level class'
     * @param stop_bits The number of stop bits, either 1 or 2
     * @return the serial port object for later use
     */
    final protected SerialPort activatePort(String portName, org.slf4j.Logger log, int stop_bits) {
        return activatePort(this.getSystemPrefix(), portName, log, stop_bits, SerialPort.Parity.NONE);
    }

    /**
     * Do the formal opening of the port,
     * set the port for blocking reads without timeout,
     * set the port to 8 data bits, the indicated number of stop bits and parity,
     * and purge the port's input stream.
     * <p>
     * Does not do the rest of the setup implied in the {@link #openPort} method.
     * This is usually followed by calls to
     * {@link #setBaudRate}, {@link #configureLeads} and {@link #setFlowControl}.
     *
     * @param systemPrefix the system prefix
     * @param portName local system name for the desired port
     * @param log Logger to use for errors, passed so that errors are logged from low-level class'
     * @param stop_bits The number of stop bits, either 1 or 2
     * @param parity one of the defined parity contants
     * @return the serial port object for later use
     */
    public static SerialPort activatePort(String systemPrefix, String portName, org.slf4j.Logger log, int stop_bits, SerialPort.Parity parity) {
        return jmri.jmrix.jserialcomm.JSerialPort.activatePort(systemPrefix, portName, log, stop_bits, parity);
    }

    final protected void setComPortTimeouts(SerialPort serialPort, Blocking blocking, int timeout) {
        serialPort.setComPortTimeouts(blocking.getValue(), timeout, 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPort(String port) {
        log.debug("Setting port to {}", port);
        mPort = port;
    }
    protected String mPort = null;

    /**
     * {@inheritDoc}
     *
     * Overridden in simulator adapter classes to return "";
     */
    @Override
    public String getCurrentPortName() {
        if (mPort == null) {
            if (getPortNames() == null) {
                // this shouldn't happen in normal operation
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
     * Provide the actual serial port names.
     * As a public static method, this can be accessed outside the jmri.jmrix
     * package to get the list of names for e.g. context reports.
     *
     * @return the port names in the form they can later be used to open the port
     */
    public static Vector<String> getActualPortNames() {
        return jmri.jmrix.jserialcomm.JSerialPort.getActualPortNames();
    }

    /**
     * Set the control leads and flow control for jmri.jmrix.purejavacomm. This handles any necessary
     * ordering.
     *
     * @param serialPort Port to be updated
     * @param flow       flow control mode from (@link jmri.jmrix.purejavacomm.SerialPort}
     * @param rts        set RTS active if true
     * @param dtr        set DTR active if true
     */
    //@Deprecated(forRemoval=true) // Removed with jmri.jmrix.PureJavaComm
    protected void configureLeadsAndFlowControl(jmri.jmrix.purejavacomm.SerialPort serialPort, int flow, boolean rts, boolean dtr) {
        // (Jan 2018) PJC seems to mix termios and ioctl access, so it's not clear
        // what's preserved and what's not. Experimentally, it seems necessary
        // to write the control leads, set flow control, and then write the control
        // leads again.
        serialPort.setRTS(rts);
        serialPort.setDTR(dtr);

        try {
            if (flow != jmri.jmrix.purejavacomm.SerialPort.FLOWCONTROL_NONE) {
                serialPort.setFlowControlMode(flow);
            }
        } catch (jmri.jmrix.purejavacomm.UnsupportedCommOperationException e) {
            log.warn("Could not set flow control, ignoring");
        }
        if (flow!=jmri.jmrix.purejavacomm.SerialPort.FLOWCONTROL_RTSCTS_OUT) serialPort.setRTS(rts); // not connected in some serial ports and adapters
        serialPort.setDTR(dtr);
    }

    /**
     * Set the baud rate on the port
     *
     * @param serialPort Port to be updated
     * @param baud baud rate to be set
     */
    final protected void setBaudRate(SerialPort serialPort, int baud) {
        serialPort.setBaudRate(baud);
    }

    /**
     * Set the control leads.
     *
     * @param serialPort Port to be updated
     * @param rts        set RTS active if true
     * @param dtr        set DTR active if true
     */
    final protected void configureLeads(SerialPort serialPort, boolean rts, boolean dtr) {
        if (rts) {
            serialPort.setRTS();
        } else {
            serialPort.clearRTS();
        }
        if (dtr) {
            serialPort.setDTR();
        } else {
            serialPort.clearDTR();
        }

    }

    /**
     * Enumerate the possible flow control choices
     */
    public enum FlowControl {
        NONE,
        RTSCTS,
        XONXOFF
    }

    /**
     * Enumerate the possible timeout choices
     */
    public enum Blocking {
        NONBLOCKING(com.fazecast.jSerialComm.SerialPort.TIMEOUT_NONBLOCKING),
        READ_BLOCKING(com.fazecast.jSerialComm.SerialPort.TIMEOUT_READ_BLOCKING),
        READ_SEMI_BLOCKING(com.fazecast.jSerialComm.SerialPort.TIMEOUT_READ_SEMI_BLOCKING);

        private final int value;

        Blocking(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    /**
     * Configure the flow control settings. Keep this in synch with the
     * FlowControl enum.
     *
     * @param serialPort Port to be updated
     * @param flow  set which kind of flow control to use
     */
    final protected void setFlowControl(SerialPort serialPort, FlowControl flow) {
        lastFlowControl = flow;
        serialPort.setFlowControl(flow);
    }

    private FlowControl lastFlowControl = FlowControl.NONE;
    /**
     * get the flow control mode back from the actual port.
     * @param serialPort Port to be examined
     * @return flow control setting observed in the port
     */
    final protected FlowControl getFlowControl(SerialPort serialPort) {
        // do a cross-check, just in case there's an issue
        int nowFlow = serialPort.getFlowControlSettings();

        switch (lastFlowControl) {

            case NONE:
                if (nowFlow != com.fazecast.jSerialComm.SerialPort.FLOW_CONTROL_DISABLED)
                    log.error("Expected flow {} but found {}", lastFlowControl, nowFlow);
                break;
            case RTSCTS:
                if (nowFlow != (com.fazecast.jSerialComm.SerialPort.FLOW_CONTROL_RTS_ENABLED
                                      | com.fazecast.jSerialComm.SerialPort.FLOW_CONTROL_CTS_ENABLED))
                    log.error("Expected flow {} but found {}", lastFlowControl, nowFlow);
                break;
            case XONXOFF:
                if (nowFlow != (com.fazecast.jSerialComm.SerialPort.FLOW_CONTROL_XONXOFF_IN_ENABLED
                                      | com.fazecast.jSerialComm.SerialPort.FLOW_CONTROL_XONXOFF_OUT_ENABLED))
                    log.error("Expected flow {} but found {}", lastFlowControl, nowFlow);
                break;
            default:
                log.warn("Unexpected FlowControl mode: {}", lastFlowControl);
        }

        return lastFlowControl;
    }

    /**
     * Add a data listener to the specified port
     * @param serialPort Port to be updated
     * @param serialPortDataListener the listener to add
     */
    final protected void setDataListener(SerialPort serialPort, SerialPortDataListener serialPortDataListener){
        serialPort.addDataListener(serialPortDataListener);
    }

    /**
     * Cleanly close the specified port
     * @param serialPort Port to be closed
     */
    final protected void closeSerialPort(SerialPort serialPort){
        serialPort.closePort();
    }

    /**
     * Set the flow control for jmri.jmrix.purejavacomm, while also setting RTS and DTR to active.
     *
     * @param serialPort Port to be updated
     * @param flow       flow control mode from (@link jmri.jmrix.purejavacomm.SerialPort}
     */
    //@Deprecated(forRemoval=true) // with jmri.jmrix.PureJavaComm
    final protected void configureLeadsAndFlowControl(jmri.jmrix.purejavacomm.SerialPort serialPort, int flow) {
        configureLeadsAndFlowControl(serialPort, flow, true, true);
    }

    /**
     * Report the connection status.
     * Typically used after the connection is complete
     * @param log The low-level logger to get this reported against the right class
     * @param portName low-level name of selected port
     */
    final protected void reportPortStatus(org.slf4j.Logger log, String portName) {
        if (log.isInfoEnabled()) {
            log.info("{}: Port {} opened at {} baud, sees DTR: {} RTS: {} DSR: {} CTS: {} DCD: {} flow: {}",
                    this.getSystemConnectionMemo().getUserName(), currentSerialPort.getDescriptivePortName(),
                    currentSerialPort.getBaudRate(), currentSerialPort.getDTR(),
                    currentSerialPort.getRTS(), currentSerialPort.getDSR(), currentSerialPort.getCTS(),
                    currentSerialPort.getDCD(), getFlowControl(currentSerialPort));
        }
        if (log.isDebugEnabled()) {
            String stopBits;
            switch (currentSerialPort.getNumStopBits()) {
                case com.fazecast.jSerialComm.SerialPort.TWO_STOP_BITS:
                    stopBits = "2";
                    break;
                case com.fazecast.jSerialComm.SerialPort.ONE_STOP_BIT:
                    stopBits = "1";
                    break;
                default:
                    stopBits = "unknown";
                    break;
            }
            log.debug("     {} data bits, {} stop bits",
                    currentSerialPort.getNumDataBits(), stopBits);
        }

    }


    // When PureJavaComm is removed, set this to 'final' to find
    // identical implementations in the subclasses - but note simulators are now overriding
    @Override
    public DataInputStream getInputStream() {
        if (!opened) {
            log.error("getInputStream called before open, stream not available");
            return null;
        }
        inputStream.replaceStream(currentSerialPort.getInputStream());
        return new DataInputStream(inputStream);
    }

    // When PureJavaComm is removed, set this to 'final' to find
    // identical implementations in the subclasses - but note simulators are now overriding
    @Override
    public DataOutputStream getOutputStream() {
        if (!opened) {
            log.error("getOutputStream called before open, stream not available");
        }
        outputStream.replaceStream(currentSerialPort.getOutputStream());
        return new DataOutputStream(outputStream);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    final public void configureBaudRate(String rate) {
        mBaudRate = rate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    final public void configureBaudRateFromNumber(String indexString) {
        int baudNum;
        int index = 0;
        final String[] rates = validBaudRates();
        final int[] numbers = validBaudNumbers();
        if ((numbers == null) || (numbers.length == 0)) { // simulators return null TODO for SpotBugs make that into an empty array
            mBaudRate = null;
            log.debug("no serial port speed values received (OK for simulator)");
            return;
        }
        if (numbers.length != rates.length) {
            mBaudRate = null;
            log.error("arrays wrong length in currentBaudNumber: {}, {}", numbers.length, rates.length);
            return;
        }
        if (indexString.isEmpty()) {
            mBaudRate = null; // represents "(none)"
            log.debug("empty baud rate received");
            return;
        }
        try {
            // since 4.16 first try to convert loaded value directly to integer
            baudNum = Integer.parseInt(indexString); // new storage format, will throw ex on old format
            log.debug("new profile format port speed value");
        } catch (NumberFormatException ex) {
            // old pre 4.15.8 format is i18n string including thousand separator and whatever suffix like "18,600 bps (J1)"
            log.warn("old profile format port speed value converted");
            // filter only numerical characters from indexString
            StringBuilder baudNumber = new StringBuilder();
            boolean digitSeen = false;
            for (int n = 0; n < indexString.length(); n++) {
                if (Character.isDigit(indexString.charAt(n))) {
                    digitSeen = true;
                    baudNumber.append(indexString.charAt(n));
                } else if ((indexString.charAt(n) == ' ') && digitSeen) {
                    break; // break on first space char encountered after at least 1 digit was found
                }
            }
            if (baudNumber.toString().equals("")) { // no number found in indexString e.g. "(automatic)"
                baudNum = 0;
            } else {
                try {
                    baudNum = Integer.parseInt(baudNumber.toString());
                } catch (NumberFormatException e2) {
                    mBaudRate = null; // represents "(none)"
                    log.error("error in filtering old profile format port speed value");
                    return;
                }
                log.debug("old format baud number: {}", indexString);
            }
        }
        // fetch baud rate description from validBaudRates[] array copy and set
        for (int i = 0; i < numbers.length; i++) {
            if (numbers[i] == baudNum) {
                index = i;
                log.debug("found new format baud value at index {}", i);
                break;
            }
        }
        mBaudRate = validBaudRates()[index];
        log.debug("mBaudRate set to: {}", mBaudRate);
    }

    /**
     * {@inheritDoc}
     * Invalid indexes are ignored.
     */
    @Override
    final public void configureBaudRateFromIndex(int index) {
        if (validBaudRates().length > index && index > -1 ) {
            mBaudRate = validBaudRates()[index];
            log.debug("mBaudRate set by index to: {}", mBaudRate);
        } else {
            // expected for simulators extending serialPortAdapter, mBaudRate already null
            log.debug("no baud rate index {} in array size {}", index, validBaudRates().length);
        }
    }

    protected String mBaudRate = null;

    @Override
    public int defaultBaudIndex() {
        return -1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCurrentBaudRate() {
        if (mBaudRate == null) {
            return "";
        }
        return mBaudRate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    final public String getCurrentBaudNumber() {
        int[] numbers = validBaudNumbers();
        String[] rates = validBaudRates();
        if (numbers == null || rates == null || numbers.length != rates.length) { // entries in arrays should correspond
            return "";
        }
        String baudNumString = "";
        // first try to find the configured baud rate value
        if (mBaudRate != null) {
            for (int i = 0; i < numbers.length; i++) {
                if (rates[i].equals(mBaudRate)) {
                    baudNumString = Integer.toString(numbers[i]);
                    break;
                }
            }
        } else if (defaultBaudIndex() > -1) {
            // use default
            baudNumString = Integer.toString(numbers[defaultBaudIndex()]);
            log.debug("using default port speed {}", baudNumString);
        }
        log.debug("mBaudRate = {}, matched to string {}", mBaudRate, baudNumString);
        return baudNumString;
    }

    @Override
    final public int getCurrentBaudIndex() {
        if (mBaudRate != null) {
            String[] rates = validBaudRates();
            // find the configured baud rate value
            for (int i = 0; i < rates.length; i++) {
                if (rates[i].equals(mBaudRate)) {
                    return i;
                }
            }
        }
        return defaultBaudIndex(); // default index or -1 if port speed not supported
    }

    /**
     * {@inheritDoc}
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "PZLA_PREFER_ZERO_LENGTH_ARRAYS",
    justification = "null signals incorrect implementation of portcontroller")
    @Override
    public String[] validBaudRates() {
        log.error("default validBaudRates implementation should not be used", new Exception());
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "PZLA_PREFER_ZERO_LENGTH_ARRAYS",
    justification = "null signals incorrect implementation of portcontroller")
    @Override
    public int[] validBaudNumbers() {
        log.error("default validBaudNumbers implementation should not be used", new Exception());
        return null;
    }

    /**
     * Convert a baud rate I18N String to an int number, e.g. "9,600 baud" to 9600.
     * <p>
     * Uses the validBaudNumbers() and validBaudRates() methods to do this.
     *
     * @param currentBaudRate a rate from validBaudRates()
     * @return baudrate as integer if available and matching first digits in currentBaudRate,
     *         0 if baudrate not supported by this adapter,
     *         -1 if no match (configuration system should prevent this)
     */
    final public int currentBaudNumber(String currentBaudRate) {
        String[] rates = validBaudRates();
        int[] numbers = validBaudNumbers();

        // return if arrays invalid
        if (numbers == null) {
            log.error("numbers array null in currentBaudNumber()");
            return -1;
        }
        if (rates == null) {
            log.error("rates array null in currentBaudNumber()");
            return -1;
        }
        if (numbers.length != rates.length) {
            log.error("arrays are of different length in currentBaudNumber: {} vs {}", numbers.length, rates.length);
            return -1;
        }
        if (numbers.length < 1) {
            log.warn("baudrate is not supported by adapter");
            return 0;
        }
        // find the baud rate value
        for (int i = 0; i < numbers.length; i++) {
            if (rates[i].equals(currentBaudRate)) {
                return numbers[i];
            }
        }

        // no match
        log.error("no match to ({}) in currentBaudNumber", currentBaudRate);
        return -1;
    }

    /**
     * {@inheritDoc}
     * Each serial port adapter should handle this and it should be abstract.
     */
    @Override
    protected void closeConnection(){}

    /**
     * Re-setup the connection.
     * Called when the physical connection has reconnected and can be linked to
     * this connection.
     * Each port adapter should handle this and it should be abstract.
     */
    @Override
    protected void resetupConnection(){}

    /**
     * Is the serial port open?
     * The LocoNet simulator uses this class but doesn't open the port.
     * @return true if the port is open, false otherwise
     */
    public boolean isPortOpen() {
        return currentSerialPort != null;
    }

    /**
     * Replace the serial port with a fake serial port and close the old
     * serial port.
     * Note that you can only replace the port once. This call is used when
     * you want to close the port and reopen it for some special task, for
     * example upload firmware.
     */
    public void replacePortWithFakePort() {
        log.warn("Replacing serial port with fake serial port: {}", currentSerialPort.getDescriptivePortName());
        SerialPort oldSerialPort = currentSerialPort;
        SerialPort serialPort = new jmri.jmrix.fakeport.FakeSerialPort();
        inputStream.replaceStream(new FakeInputStream());
        outputStream.replaceStream(OutputStream.nullOutputStream());
        currentSerialPort = serialPort;
        oldSerialPort.closePort();
    }

    /**
     * Get a string with the serial port settings.
     * @return the settings as a string
     */
    public String getPortSettingsString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Baudrate: ").append(currentSerialPort.getBaudRate()).append(", ");
        sb.append("FlowControl: ").append(currentSerialPort.getFlowControlSettings()).append(", ");
        sb.append("Num data bits: ").append(currentSerialPort.getNumDataBits()).append(", ");
        sb.append("Num stop bits: ").append(currentSerialPort.getNumStopBits()).append(", ");
        sb.append("Parity").append(currentSerialPort.getParity().name());
        return sb.toString();
    }


    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractSerialPortController.class);

}

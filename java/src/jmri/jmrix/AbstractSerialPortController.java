package jmri.jmrix;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.*;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jmri.SystemConnectionMemo;
import jmri.util.SystemType;

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

    protected SerialPort currentSerialPort = null;

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
        return activatePort(this.getSystemPrefix(), portName, log, 1, Parity.NONE);
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
        return activatePort(this.getSystemPrefix(), portName, log, stop_bits, Parity.NONE);
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
    public static SerialPort activatePort(String systemPrefix, String portName, org.slf4j.Logger log, int stop_bits, Parity parity) {
        com.fazecast.jSerialComm.SerialPort serialPort;

        // convert the 1 or 2 stop_bits argument to the proper jSerialComm code value
        int stop_bits_code;
        switch (stop_bits) {
            case 1:
                stop_bits_code = com.fazecast.jSerialComm.SerialPort.ONE_STOP_BIT;
                break;
            case 2:
                stop_bits_code = com.fazecast.jSerialComm.SerialPort.TWO_STOP_BITS;
                break;
            default:
                throw new IllegalArgumentException("Incorrect stop_bits argument: "+stop_bits);
        }

        try {
            serialPort = com.fazecast.jSerialComm.SerialPort.getCommPort(portName);
            serialPort.openPort();
            serialPort.setComPortTimeouts(com.fazecast.jSerialComm.SerialPort.TIMEOUT_READ_BLOCKING, 0, 0);
            serialPort.setNumDataBits(8);
            serialPort.setNumStopBits(stop_bits_code);
            serialPort.setParity(parity.getValue());
            purgeStream(serialPort.getInputStream());
        } catch (java.io.IOException | com.fazecast.jSerialComm.SerialPortInvalidPortException ex) {
            // IOException includes
            //      com.fazecast.jSerialComm.SerialPortIOException
            handlePortNotFound(systemPrefix, portName, log, ex);
            return null;
        }
        return new SerialPort(serialPort);
    }

    final protected void setComPortTimeouts(SerialPort serialPort, Blocking blocking, int timeout) {
        serialPort.serialPort.setComPortTimeouts(blocking.getValue(), timeout, 0);
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

    private static String getSymlinkTarget(File symlink) {
        try {
            // Path.toRealPath() follows a symlink
            return symlink.toPath().toRealPath().toFile().getName();
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Provide the actual serial port names.
     * As a public static method, this can be accessed outside the jmri.jmrix
     * package to get the list of names for e.g. context reports.
     *
     * @return the port names in the form they can later be used to open the port
     */
//    @SuppressWarnings("UseOfObsoleteCollectionType") // historical interface
    @SuppressFBWarnings("DMI_HARDCODED_ABSOLUTE_FILENAME")  // /dev/ is not expected to change on Linux and Mac
    public static Vector<String> getActualPortNames() {
        // first, check that the comm package can be opened and ports seen
        var portNameVector = new Vector<String>();

        com.fazecast.jSerialComm.SerialPort[] portIDs = com.fazecast.jSerialComm.SerialPort.getCommPorts();
                // find the names of suitable ports
        for (com.fazecast.jSerialComm.SerialPort portID : portIDs) {
            portNameVector.addElement(portID.getSystemPortName());
        }

        // On Linux and Mac, use the system property purejavacomm.portnamepattern
        // to let the user add additional serial ports
        String portnamePattern = System.getProperty("purejavacomm.portnamepattern");
        if ((portnamePattern != null) && (SystemType.isLinux() || SystemType.isMacOSX())) {
            Pattern pattern = Pattern.compile(portnamePattern);

            File[] files = new File("/dev").listFiles();
            if (files != null) {
                Set<String> ports = Stream.of(files)
                        .filter(file -> !file.isDirectory()
                                && (pattern.matcher(file.getName()).matches()
                                        || portNameVector.contains(getSymlinkTarget(file)))
                                && !portNameVector.contains(file.getName()))
                        .map(File::getName)
                        .collect(Collectors.toSet());

                portNameVector.addAll(ports);
            }

        }

        return portNameVector;
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
        serialPort.serialPort.setBaudRate(baud);
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
            serialPort.serialPort.setRTS();
        } else {
            serialPort.serialPort.clearRTS();
        }
        if (dtr) {
            serialPort.serialPort.setDTR();
        } else {
            serialPort.serialPort.clearDTR();
        }

    }

    /**
     * Configure the port's parity
     *
     * @param serialPort Port to be updated
     * @param parity the desired parity as one of the define static final constants
     */
    final protected void setParity(com.fazecast.jSerialComm.SerialPort serialPort, Parity parity) {
        serialPort.setParity(parity.getValue());  // constants are defined with values for the specific port class
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
     * Enumerate the possible parity choices
     */
    public enum Parity {
        NONE(com.fazecast.jSerialComm.SerialPort.NO_PARITY),
        EVEN(com.fazecast.jSerialComm.SerialPort.EVEN_PARITY),
        ODD(com.fazecast.jSerialComm.SerialPort.ODD_PARITY);

        private final int value;

        Parity(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static Parity getParity(int parity) {
            for (Parity p : Parity.values()) {
                if (p.value == parity) {
                    return p;
                }
            }
            throw new IllegalArgumentException("Unknown parity");
        }
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
        int nowFlow = serialPort.serialPort.getFlowControlSettings();

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
        serialPort.serialPort.addDataListener(new com.fazecast.jSerialComm.SerialPortDataListener() {
            @Override
            public int getListeningEvents() {
                return serialPortDataListener.getListeningEvents();
            }

            @Override
            public void serialEvent(com.fazecast.jSerialComm.SerialPortEvent event) {
                serialPortDataListener.serialEvent(new SerialPortEvent(event));
            }
        });
    }

    /**
     * Cleanly close the specified port
     * @param serialPort Port to be closed
     */
    final protected void closeSerialPort(SerialPort serialPort){
        serialPort.serialPort.closePort();
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
            log.info("Port {} {} opened at {} baud, sees DTR: {} RTS: {} DSR: {} CTS: {} DCD: {} flow: {}",
                    portName, currentSerialPort.serialPort.getDescriptivePortName(),
                    currentSerialPort.serialPort.getBaudRate(), currentSerialPort.serialPort.getDTR(),
                    currentSerialPort.serialPort.getRTS(), currentSerialPort.serialPort.getDSR(), currentSerialPort.serialPort.getCTS(),
                    currentSerialPort.serialPort.getDCD(), getFlowControl(currentSerialPort));
        }
        if (log.isDebugEnabled()) {
            String stopBits;
            switch (currentSerialPort.serialPort.getNumStopBits()) {
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
                    currentSerialPort.serialPort.getNumDataBits(), stopBits);
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
        return new DataInputStream(currentSerialPort.serialPort.getInputStream());
    }

    // When PureJavaComm is removed, set this to 'final' to find
    // identical implementations in the subclasses - but note simulators are now overriding
    @Override
    public DataOutputStream getOutputStream() {
        if (!opened) {
            log.error("getOutputStream called before open, stream not available");
        }

        return new DataOutputStream(currentSerialPort.serialPort.getOutputStream());
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


    public static class SerialPort {

        public static final int LISTENING_EVENT_DATA_AVAILABLE =
                com.fazecast.jSerialComm.SerialPort.LISTENING_EVENT_DATA_AVAILABLE;

        public static final int ONE_STOP_BIT = com.fazecast.jSerialComm.SerialPort.ONE_STOP_BIT;
        public static final int NO_PARITY = com.fazecast.jSerialComm.SerialPort.NO_PARITY;

        private final com.fazecast.jSerialComm.SerialPort serialPort;

        private SerialPort(com.fazecast.jSerialComm.SerialPort serialPort) {
            this.serialPort = serialPort;
        }

        public void addDataListener(SerialPortDataListener listener) {
            this.serialPort.addDataListener(new com.fazecast.jSerialComm.SerialPortDataListener() {
                @Override
                public int getListeningEvents() {
                    return listener.getListeningEvents();
                }

                @Override
                public void serialEvent(com.fazecast.jSerialComm.SerialPortEvent event) {
                    listener.serialEvent(new SerialPortEvent(event));
                }
            });
        }

        public InputStream getInputStream() {
            return this.serialPort.getInputStream();
        }

        public OutputStream getOutputStream() {
            return this.serialPort.getOutputStream();
        }

        public void setRTS() {
            this.serialPort.setRTS();
        }

        public void clearRTS() {
            this.serialPort.clearRTS();
        }

        public void setBaudRate(int baudrate) {
            this.serialPort.setBaudRate(baudrate);
        }

        public int getBaudRate() {
            return this.serialPort.getBaudRate();
        }

        public void setNumDataBits(int bits) {
            this.serialPort.setNumDataBits(bits);
        }

        public void setNumStopBits(int bits) {
            this.serialPort.setNumStopBits(bits);
        }

        public void setParity(Parity parity) {
            serialPort.setParity(parity.getValue());  // constants are defined with values for the specific port class
        }

        public void setDTR() {
            this.serialPort.setDTR();
        }

        public void clearDTR() {
            this.serialPort.clearDTR();
        }

        public boolean getDTR() {
            return this.serialPort.getDTR();
        }

        public boolean getRTS() {
            return this.serialPort.getRTS();
        }

        public boolean getDSR() {
            return this.serialPort.getDSR();
        }

        public boolean getCTS() {
            return this.serialPort.getCTS();
        }

        public boolean getDCD() {
            return this.serialPort.getDCD();
        }

        public boolean getRI() {
            return this.serialPort.getRI();
        }

        /**
         * Configure the flow control settings. Keep this in synch with the
         * FlowControl enum.
         *
         * @param flow  set which kind of flow control to use
         */
        public final void setFlowControl(FlowControl flow) {
            boolean result = true;

            if (null == flow) {
                log.error("Invalid null FlowControl enum member");
            } else switch (flow) {
                case RTSCTS:
                    result = serialPort.setFlowControl(com.fazecast.jSerialComm.SerialPort.FLOW_CONTROL_RTS_ENABLED
                            | com.fazecast.jSerialComm.SerialPort.FLOW_CONTROL_CTS_ENABLED );
                    break;
                case XONXOFF:
                    result = serialPort.setFlowControl(com.fazecast.jSerialComm.SerialPort.FLOW_CONTROL_XONXOFF_IN_ENABLED
                            | com.fazecast.jSerialComm.SerialPort.FLOW_CONTROL_XONXOFF_OUT_ENABLED);
                    break;
                case NONE:
                    result = serialPort.setFlowControl(com.fazecast.jSerialComm.SerialPort.FLOW_CONTROL_DISABLED);
                    break;
                default:
                    log.error("Invalid FlowControl enum member: {}", flow);
                    break;
            }

            if (!result) log.error("Port did not accept flow control setting {}", flow);
        }

        public void setBreak() {
            this.serialPort.setBreak();
        }

        public void clearBreak() {
            this.serialPort.clearBreak();
        }

        public void closePort() {
            this.serialPort.closePort();
        }

        public String getDescriptivePortName() {
            return this.serialPort.getDescriptivePortName();
        }

        @Override
        public String toString() {
            return this.serialPort.toString();
        }

    }


    public static interface SerialPortDataListener {

        void serialEvent(SerialPortEvent serialPortEvent);

        public int getListeningEvents();

    }

    public static class SerialPortEvent {

        private final com.fazecast.jSerialComm.SerialPortEvent event;

        private SerialPortEvent(com.fazecast.jSerialComm.SerialPortEvent event) {
            this.event = event;
        }

        public int getEventType() {
            return event.getEventType();
        }
    }


    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractSerialPortController.class);

}

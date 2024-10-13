package jmri.jmrix.jserialcomm;

import jmri.jmrix.*;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.*;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jmri.util.SystemType;

/**
 * Implementation of serial port using jSerialComm.
 *
 * @author Daniel Bergqvist (C) 2024
 */
public class JSerialPort implements SerialPort {

//    public static final int LISTENING_EVENT_DATA_AVAILABLE = com.fazecast.jSerialComm.SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
//    public static final int ONE_STOP_BIT = com.fazecast.jSerialComm.SerialPort.ONE_STOP_BIT;
//    public static final int NO_PARITY = com.fazecast.jSerialComm.SerialPort.NO_PARITY;
    private final com.fazecast.jSerialComm.SerialPort serialPort;

    /*.*
     * Enumerate the possible parity choices
     *./
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
*/
    private JSerialPort(com.fazecast.jSerialComm.SerialPort serialPort) {
        this.serialPort = serialPort;
    }

    @Override
    public void addDataListener(SerialPortDataListener listener) {
        this.serialPort.addDataListener(new com.fazecast.jSerialComm.SerialPortDataListener() {
            @Override
            public int getListeningEvents() {
                return listener.getListeningEvents();
            }

            @Override
            public void serialEvent(com.fazecast.jSerialComm.SerialPortEvent event) {
                listener.serialEvent(new JSerialPortEvent(event));
            }
        });
    }

    @Override
    public InputStream getInputStream() {
        return this.serialPort.getInputStream();
    }

    @Override
    public OutputStream getOutputStream() {
        return this.serialPort.getOutputStream();
    }

    @Override
    public void setRTS() {
        this.serialPort.setRTS();
    }

    @Override
    public void clearRTS() {
        this.serialPort.clearRTS();
    }

    @Override
    public void setBaudRate(int baudrate) {
        this.serialPort.setBaudRate(baudrate);
    }

    @Override
    public int getBaudRate() {
        return this.serialPort.getBaudRate();
    }

    @Override
    public void setNumDataBits(int bits) {
        this.serialPort.setNumDataBits(bits);
    }

    @Override
    public final int getNumDataBits() {
        return serialPort.getNumDataBits();
    }

    @Override
    public void setNumStopBits(int bits) {
        this.serialPort.setNumStopBits(bits);
    }

    @Override
    public final int getNumStopBits() {
        return serialPort.getNumStopBits();
    }

    @Override
    public void setParity(Parity parity) {
        serialPort.setParity(parity.getValue()); // constants are defined with values for the specific port class
    }

    @Override
    public Parity getParity() {
        return Parity.getParity(serialPort.getParity()); // constants are defined with values for the specific port class
    }

    @Override
    public void setDTR() {
        this.serialPort.setDTR();
    }

    @Override
    public void clearDTR() {
        this.serialPort.clearDTR();
    }

    @Override
    public boolean getDTR() {
        return this.serialPort.getDTR();
    }

    @Override
    public boolean getRTS() {
        return this.serialPort.getRTS();
    }

    @Override
    public boolean getDSR() {
        return this.serialPort.getDSR();
    }

    @Override
    public boolean getCTS() {
        return this.serialPort.getCTS();
    }

    @Override
    public boolean getDCD() {
        return this.serialPort.getDCD();
    }

    @Override
    public boolean getRI() {
        return this.serialPort.getRI();
    }

    /**
     * Configure the flow control settings. Keep this in synch with the
     * FlowControl enum.
     *
     * @param flow  set which kind of flow control to use
     */
    @Override
    public final void setFlowControl(AbstractSerialPortController.FlowControl flow) {
        boolean result = true;
        if (null == flow) {
            log.error("Invalid null FlowControl enum member");
        } else {
            switch (flow) {
                case RTSCTS:
                    result = serialPort.setFlowControl(com.fazecast.jSerialComm.SerialPort.FLOW_CONTROL_RTS_ENABLED | com.fazecast.jSerialComm.SerialPort.FLOW_CONTROL_CTS_ENABLED);
                    break;
                case XONXOFF:
                    result = serialPort.setFlowControl(com.fazecast.jSerialComm.SerialPort.FLOW_CONTROL_XONXOFF_IN_ENABLED | com.fazecast.jSerialComm.SerialPort.FLOW_CONTROL_XONXOFF_OUT_ENABLED);
                    break;
                case NONE:
                    result = serialPort.setFlowControl(com.fazecast.jSerialComm.SerialPort.FLOW_CONTROL_DISABLED);
                    break;
                default:
                    log.error("Invalid FlowControl enum member: {}", flow);
                    break;
            }
        }
        if (!result) {
            log.error("Port did not accept flow control setting {}", flow);
        }
    }

    @Override
    public void setBreak() {
        this.serialPort.setBreak();
    }

    @Override
    public void clearBreak() {
        this.serialPort.clearBreak();
    }

    @Override
    public final int getFlowControlSettings() {
        return serialPort.getFlowControlSettings();
    }

    @Override
    public final boolean setComPortTimeouts(int newTimeoutMode, int newReadTimeout, int newWriteTimeout) {
        return serialPort.setComPortTimeouts(newTimeoutMode, newReadTimeout, newWriteTimeout);
    }

    @Override
    public void closePort() {
        this.serialPort.closePort();
    }

    @Override
    public String getDescriptivePortName() {
        return this.serialPort.getDescriptivePortName();
    }

    @Override
    public String toString() {
        return this.serialPort.toString();
    }

    /**
     * Open the port.
     *
     * @param systemPrefix the system prefix
     * @param portName local system name for the desired port
     * @param log Logger to use for errors, passed so that errors are logged from low-level class'
     * @param stop_bits The number of stop bits, either 1 or 2
     * @param parity one of the defined parity contants
     * @return the serial port object for later use
     */
    public static JSerialPort activatePort(String systemPrefix, String portName, org.slf4j.Logger log, int stop_bits, Parity parity) {
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
                throw new IllegalArgumentException("Incorrect stop_bits argument: " + stop_bits);
        }
        try {
            serialPort = com.fazecast.jSerialComm.SerialPort.getCommPort(portName);
            serialPort.openPort();
            serialPort.setComPortTimeouts(com.fazecast.jSerialComm.SerialPort.TIMEOUT_READ_BLOCKING, 0, 0);
            serialPort.setNumDataBits(8);
            serialPort.setNumStopBits(stop_bits_code);
            serialPort.setParity(parity.getValue());
            AbstractPortController.purgeStream(serialPort.getInputStream());
        } catch (java.io.IOException | com.fazecast.jSerialComm.SerialPortInvalidPortException ex) {
            // IOException includes
            //      com.fazecast.jSerialComm.SerialPortIOException
            AbstractSerialPortController.handlePortNotFound(systemPrefix, portName, log, ex);
            return null;
        }
        return new JSerialPort(serialPort);
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
    @SuppressFBWarnings(value = "DMI_HARDCODED_ABSOLUTE_FILENAME")
    public static Vector<String> getActualPortNames() {
        // first, check that the comm package can be opened and ports seen
        java.util.Vector<java.lang.String> portNameVector = new Vector<String>();
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
                Set<String> ports = Stream.of(files).filter(
                        file -> !file.isDirectory()
                                && (pattern.matcher(file.getName()).matches()
                                        || portNameVector.contains(getSymlinkTarget(file)))
                                && !portNameVector.contains(file.getName())).map(File::getName).collect(Collectors.toSet());
                portNameVector.addAll(ports);
            }
        }
        return portNameVector;
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JSerialPort.class);
}

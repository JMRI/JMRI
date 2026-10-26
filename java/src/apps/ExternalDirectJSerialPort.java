package apps;

import com.fazecast.jSerialComm.SerialPort;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Thin wrapper confining all direct {@code com.fazecast.jSerialComm} access for the
 * standalone {@code apps} CLI tools to a single class.
 * <p>
 * JMRI's production serial access is abstracted behind
 * {@code jmri.jmrix.jserialcomm.JSerialPort}; the ArchitectureTest rule
 * {@code checkJSerialCommAllowedUses} confines {@code com.fazecast.jSerialComm} to that
 * abstraction. The standalone CLI utilities (e.g. {@link McanTcpBridge}) are deliberately
 * decoupled from the JMRI runtime, so instead of pulling in the full JMRI serial stack they
 * route their raw serial access through this one wrapper, which is the sole {@code apps}
 * class whitelisted for direct jSerialComm use.
 * <p>
 * This is intentionally a minimal pass-through exposing only the operations the CLI tools
 * need; it is not a general-purpose serial abstraction.
 *
 * @author Ralf Lang (ralf.lang@ralf-lang.de)
 */
public final class ExternalDirectJSerialPort {

    /** Immutable description of an available serial port. */
    public static final class PortInfo {
        public final String systemPortName;
        public final String portDescription;
        public final String portLocation;

        PortInfo(String systemPortName, String portDescription, String portLocation) {
            this.systemPortName = systemPortName;
            this.portDescription = portDescription;
            this.portLocation = portLocation;
        }
    }

    private final SerialPort port;

    private ExternalDirectJSerialPort(SerialPort port) {
        this.port = port;
    }

    /**
     * Enumerate the serial ports visible to the host.
     *
     * @return list of port descriptors (possibly empty, never null)
     */
    public static List<PortInfo> list() {
        List<PortInfo> result = new ArrayList<>();
        for (SerialPort p : SerialPort.getCommPorts()) {
            result.add(new PortInfo(p.getSystemPortName(), p.getPortDescription(), p.getPortLocation()));
        }
        return result;
    }

    /**
     * Look up a serial port by system name. Does not open it.
     *
     * @param portName system port name
     * @return a wrapper for the named port
     */
    public static ExternalDirectJSerialPort forName(String portName) {
        return new ExternalDirectJSerialPort(SerialPort.getCommPort(portName));
    }

    // ---- Re-exported jSerialComm constants, so callers need not import com.fazecast ----

    /** One stop bit ({@code SerialPort.ONE_STOP_BIT}). */
    public static final int ONE_STOP_BIT = SerialPort.ONE_STOP_BIT;
    /** Two stop bits ({@code SerialPort.TWO_STOP_BITS}). */
    public static final int TWO_STOP_BITS = SerialPort.TWO_STOP_BITS;
    /** No parity ({@code SerialPort.NO_PARITY}). */
    public static final int NO_PARITY = SerialPort.NO_PARITY;
    /** Even parity ({@code SerialPort.EVEN_PARITY}). */
    public static final int EVEN_PARITY = SerialPort.EVEN_PARITY;
    /** Odd parity ({@code SerialPort.ODD_PARITY}). */
    public static final int ODD_PARITY = SerialPort.ODD_PARITY;
    /** No flow control ({@code SerialPort.FLOW_CONTROL_DISABLED}). */
    public static final int FLOW_CONTROL_DISABLED = SerialPort.FLOW_CONTROL_DISABLED;
    /** RTS flow control enabled ({@code SerialPort.FLOW_CONTROL_RTS_ENABLED}). */
    public static final int FLOW_CONTROL_RTS_ENABLED = SerialPort.FLOW_CONTROL_RTS_ENABLED;

    /**
     * Open the port and apply the given line settings, then assert DTR/RTS if requested.
     * A blocking read mode is configured with the supplied read timeout.
     * <p>
     * This is a straight pass-through to the underlying serial library; the caller supplies
     * all policy, so the wrapper stays reusable across standalone tools. Use the re-exported
     * constants (e.g. {@link #ONE_STOP_BIT}, {@link #NO_PARITY}, {@link #FLOW_CONTROL_RTS_ENABLED})
     * for the stop-bit / parity / flow-control arguments.
     *
     * @param baudRate       line speed
     * @param dataBits       data bits per character (typically 8)
     * @param stopBits       stop bits, e.g. {@link #ONE_STOP_BIT}
     * @param parity         parity, e.g. {@link #NO_PARITY}
     * @param flowControl    flow control, e.g. {@link #FLOW_CONTROL_RTS_ENABLED}
     * @param readTimeoutMs  blocking read timeout in milliseconds
     * @param assertDtrRts   when true, assert DTR and RTS after opening
     * @return true if the port opened successfully
     */
    public boolean open(int baudRate, int dataBits, int stopBits, int parity,
                        int flowControl, int readTimeoutMs, boolean assertDtrRts) {
        if (!port.openPort()) {
            return false;
        }
        port.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, readTimeoutMs, 0);
        port.setNumDataBits(dataBits);
        port.setNumStopBits(stopBits);
        port.setParity(parity);
        port.setBaudRate(baudRate);
        port.setFlowControl(flowControl);
        if (assertDtrRts) {
            port.setDTR();
            port.setRTS();
        }
        return true;
    }

    /** @return true if the underlying port is currently open. */
    public boolean isOpen() {
        return port.isOpen();
    }

    /** Close the underlying port. */
    public void close() {
        port.closePort();
    }

    /** @return the port's input stream (raw {@link java.io} type; may be null if closed). */
    public InputStream getInputStream() {
        return port.getInputStream();
    }

    /** @return the port's output stream (raw {@link java.io} type; may be null if closed). */
    public OutputStream getOutputStream() {
        return port.getOutputStream();
    }

    /** @return the human-readable descriptive name of the port. */
    public String getDescriptivePortName() {
        return port.getDescriptivePortName();
    }
}

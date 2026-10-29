package jmri.jmrix;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * JMRI application level abstraction of a serial port.
 * <p>
 * Program towards this interface rather than to the underlying
 * {@code com.fazecast.jSerialComm} library directly and treat the latter as an
 * implementation detail which might change or might be mocked for testing.
 * The implementation {@link jmri.jmrix.jserialcomm.JSerialPort} delegates to
 * jSerialComm.
 * <p>
 * The integer constants exposed here ({@link #ONE_STOP_BIT}, etc.) mirror the
 * jSerialComm values but consider typed enums where they exist (e.g. {@link Parity}).
 *
 * @author Daniel Bergqvist (C) 2024
 */
public interface SerialPort {

    /** Event type fired when the port has data for reading */
    public static final int LISTENING_EVENT_DATA_AVAILABLE = com.fazecast.jSerialComm.SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
    /** Stop-bits constant: One stop bit */
    public static final int ONE_STOP_BIT = com.fazecast.jSerialComm.SerialPort.ONE_STOP_BIT;
    /** Parity constant: No parity. */
    public static final int NO_PARITY = com.fazecast.jSerialComm.SerialPort.NO_PARITY;

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
     * Register a listener that will be notified when serial port events occur,
     * such as data becoming available to read.
     *
     * @param listener the listener to register; must not be {@code null}
     */
    void addDataListener(SerialPortDataListener listener);

    /**
     * Get the input stream for reading data received on this port.
     *
     * @return the port's input stream
     */
    InputStream getInputStream();

    /**
     * Get the output stream for writing data to be transmitted on this port.
     *
     * @return the port's output stream
     */
    OutputStream getOutputStream();

    /**
     * Set high the RTS (Request To Send) signal.
     */
    void setRTS();

    /**
     * Set low / unset the RTS (Request To Send) signal.
     */
    void clearRTS();

    /**
     * Set the baud rate for this port.
     *
     * @param baudrate The desired baud rate (i.e.600 or 115200)
     */
    void setBaudRate(int baudrate);

    /**
     * Get the current baud rate of this port.
     *
     * @return the configured baud rate
     */
    int getBaudRate();

    /**
     * Set the number of data bits per character frame. 5-8 is usually supported.
     *
     * @param bits number of data bits
     */
    void setNumDataBits(int bits);

    /**
     * Get the number of data bits per character frame.
     *
     * @return number of data bits
     */
    int getNumDataBits();

    /**
     * Set the number of stop bits appended to each character frame.
     *
     * @param bits number of stop bits; use {@link #ONE_STOP_BIT} or the
     *             jSerialComm TWO_STOP_BITS constant
     */
    void setNumStopBits(int bits);

    /**
     * Get the number of stop bits appended to each character frame.
     *
     * @return number of stop bits
     */
    int getNumStopBits();

    /**
     * Set the parity mode used for error detection.
     *
     * @param parity The desired parity from the enum {@link Parity} but not {@code null}
     */
    void setParity(Parity parity);

    /**
     * Get the currently configured parity mode.
     *
     * @return the active parity setting
     */
    Parity getParity();

    /**
     * Assert (set high) the DTR (Data Terminal Ready) signal.
     */
    void setDTR();

    /**
     * De-assert (set low) the DTR (Data Terminal Ready) signal.
     */
    void clearDTR();

    /**
     * Get the current state of the DTR (Data Terminal Ready) output signal.
     *
     * @return {@code true} if DTR is currently asserted
     */
    boolean getDTR();

    /**
     * Get the current state of the RTS (Request To Send) output signal.
     *
     * @return {@code true} if RTS is currently asserted
     */
    boolean getRTS();

    /**
     * Get the state of the DSR (Data Set Ready) input signal from the remote device.
     *
     * @return {@code true} if DSR is asserted by the remote device
     */
    boolean getDSR();

    /**
     * Get the state of the CTS (Clear To Send) input signal from the remote device.
     *
     * @return {@code true} if CTS is asserted by the remote device
     */
    boolean getCTS();

    /**
     * Get the state of the DCD (Data Carrier Detect) input signal.
     *
     * @return {@code true} if a carrier is detected
     */
    boolean getDCD();

    /**
     * Get the state of the RI (Ring Indicator) input signal.
     *
     * @return {@code true} if a ring signal is present
     */
    boolean getRI();

    /**
     * Configure the flow control settings. Keep this in synch with the
     * FlowControl enum.
     *
     * @param flow  set which kind of flow control to use
     */
    void setFlowControl(AbstractSerialPortController.FlowControl flow);

    /**
     * Assert the BREAK signal on the transmit line. A BREAK condition is
     * defined as holding the line in the space (logic 0) state for longer than
     * one character time.
     */
    void setBreak();

    /**
     * Release the BREAK signal, returning the transmit line to its idle state.
     */
    void clearBreak();

    /**
     * Get the raw flow-control bitmask currently active on this port, using
     * the bit definitions from the underlying serial library.
     *
     * @return bitmask of active flow-control modes
     */
    int getFlowControlSettings();

    /**
     * Configure the read and write timeout behaviour of the port.
     *
     * @param newTimeoutMode  timeout mode bitmask (jSerialComm TIMEOUT_* constants)
     * @param newReadTimeout  read timeout in milliseconds or 0 for no timeout
     * @param newWriteTimeout write timeout in milliseconds or 0 for no timeout
     * @return {@code true} if the timeouts were applied successfully
     */
    boolean setComPortTimeouts(int newTimeoutMode, int newReadTimeout, int newWriteTimeout);

    /**
     * Close this port and release all associated OS resources.  After this
     * call the port is no longer usable and {@link #isOpen()} will return
     * {@code false}.
     */
    void closePort();

    /**
     * Check if the serial port is currently open.
     *
     * @return true if the port is open, false otherwise
     */
    boolean isOpen();

    /**
     * Get the human-readable name of this port as reported by the operating
     * system. Suitable for display in a UI (e.g. "COM3" on Windows,
     * "/dev/ttyUSB0" on Linux).
     *
     * @return the descriptive port name but never {@code null}
     */
    String getDescriptivePortName();

    /**
     * Get the description of the port from the operating system.
     * This typically includes hardware information like "USB Serial Port".
     *
     * @return the port description string
     */
    String getPortDescription();

    /**
     * Get the physical location of the port on the system.
     * For USB devices, this typically returns a USB path like "USB VID:PID=10C4:EA60".
     *
     * @return the port location string or empty string if not available
     */
    String getPortLocation();

    /**
     * Return a string representation of this port. Typically its descriptive
     * name.  Overriding {@code toString} here ensures implementations are
     * explicit about what they expose. This matters when port objects are
     * used as list model elements.
     *
     * @return string representation of this port. Never {@code null}
     */
    @Override
    String toString();

}

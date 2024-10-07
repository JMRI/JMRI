package jmri.jmrix;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Serial port
 *
 * @author Daniel Bergqvist (C) 2024
 */
public interface SerialPort {

    public static final int LISTENING_EVENT_DATA_AVAILABLE = com.fazecast.jSerialComm.SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
    public static final int ONE_STOP_BIT = com.fazecast.jSerialComm.SerialPort.ONE_STOP_BIT;
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

    void addDataListener(SerialPortDataListener listener);

    InputStream getInputStream();

    OutputStream getOutputStream();

    void setRTS();

    void clearRTS();

    void setBaudRate(int baudrate);

    int getBaudRate();

    void setNumDataBits(int bits);

    int getNumDataBits();

    void setNumStopBits(int bits);

    int getNumStopBits();

    void setParity(Parity parity);

    Parity getParity();

    void setDTR();

    void clearDTR();

    boolean getDTR();

    boolean getRTS();

    boolean getDSR();

    boolean getCTS();

    boolean getDCD();

    boolean getRI();

    /**
     * Configure the flow control settings. Keep this in synch with the
     * FlowControl enum.
     *
     * @param flow  set which kind of flow control to use
     */
    void setFlowControl(AbstractSerialPortController.FlowControl flow);

    void setBreak();

    void clearBreak();

    int getFlowControlSettings();

    boolean setComPortTimeouts(int newTimeoutMode, int newReadTimeout, int newWriteTimeout);

    void closePort();

    String getDescriptivePortName();

    @Override
    String toString();

}

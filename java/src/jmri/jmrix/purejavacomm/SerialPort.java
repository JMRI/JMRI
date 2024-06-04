package jmri.jmrix.purejavacomm;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.TooManyListenersException;

import jmri.jmrix.AbstractSerialPortController;

/**
 * Serial port.
 */
public class SerialPort {

    public static final int DATABITS_8 = 8;
    public static final int PARITY_NONE = 0;
    public static final int STOPBITS_1 = 1;
    public static final int FLOWCONTROL_NONE = 0;
    public static final int FLOWCONTROL_RTSCTS_IN = 1;
    public static final int FLOWCONTROL_RTSCTS_OUT = 2;

    private AbstractSerialPortController.SerialPort _serialPort;

    public SerialPort(AbstractSerialPortController.SerialPort serialPort) {
        this._serialPort = serialPort;
    }

    public void setSerialPortParams(int baudRate, int dataBits, int stopBits, int parity) throws UnsupportedCommOperationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void addEventListener(SerialPortEventListener listener) throws TooManyListenersException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void notifyOnDataAvailable(boolean value) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void setFlowControlMode(int mode) throws UnsupportedCommOperationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public InputStream getInputStream() throws IOException {
        return _serialPort.getInputStream();
    }

    public OutputStream getOutputStream() throws IOException {
        return _serialPort.getOutputStream();
    }

    public int getBaudRate() {
        return _serialPort.getBaudRate();
    }

    public void setDTR(boolean value) {
        if (value) {
            _serialPort.setDTR();
        } else {
            _serialPort.clearDTR();
        }
    }

    public void setRTS(boolean value) {
        if (value) {
            _serialPort.setRTS();
        } else {
            _serialPort.clearRTS();
        }
    }

    public boolean isDTR() {
        return _serialPort.getDTR();
    }

    public boolean isRTS() {
        return _serialPort.getRTS();
    }

    public boolean isDSR() {
        return _serialPort.getDSR();
    }

    public boolean isCTS() {
        return _serialPort.getCTS();
    }

    public boolean isCD() {
        return _serialPort.getDCD();
    }

    public boolean isReceiveTimeoutEnabled() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public int getReceiveTimeout() {
        throw new UnsupportedOperationException("Not implemented");
    }

}

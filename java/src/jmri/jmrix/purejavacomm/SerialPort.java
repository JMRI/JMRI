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
    public static final int PARITY_ODD = 1;
    public static final int PARITY_EVEN = 2;
    public static final int STOPBITS_1 = 1;
    public static final int STOPBITS_2 = 2;
    public static final int FLOWCONTROL_NONE = 0;
    public static final int FLOWCONTROL_RTSCTS_IN = 1;
    public static final int FLOWCONTROL_RTSCTS_OUT = 2;

    private jmri.jmrix.SerialPort _serialPort;
    private SerialPortEventListener _eventListener;
    private boolean _threadStarted = false;
    private Thread _inputThread;
    private volatile boolean _notifyOnDataAvailable;

    public SerialPort(jmri.jmrix.SerialPort serialPort) {
        this._serialPort = serialPort;

        Runnable runnable = () -> {
            final int TIMEOUT = Integer.getInteger("purejavacomm.pollperiod", 10);
            InputStream inputStream = _serialPort.getInputStream();

            while (true) {
                try {
                    while (_notifyOnDataAvailable && (_eventListener != null)
                            && (inputStream.available() > 0)) {
                        _eventListener.serialEvent(new SerialPortEvent(
                                this, SerialPortEvent.DATA_AVAILABLE, false, true));
                    }
                    Thread.sleep(TIMEOUT);
                } catch (InterruptedException e) {
                    log.error("Thread interrupted");
                    break;
                } catch (IOException e) {
                    log.error("An IO error occurred", e);
                    break;
                }
            }
        };
        _inputThread = jmri.util.ThreadingUtil.newThread(
                runnable, "jmri.jmrix.purejavacomm.SerialPort");
        _inputThread.setDaemon(true);
    }

    private void setParity(int parity) {
        switch (parity) {
            case PARITY_NONE:
                _serialPort.setParity(jmri.jmrix.SerialPort.Parity.NONE);
                break;

            case PARITY_EVEN:
                _serialPort.setParity(jmri.jmrix.SerialPort.Parity.EVEN);
                break;

            case PARITY_ODD:
                _serialPort.setParity(jmri.jmrix.SerialPort.Parity.ODD);
                break;

            default:
                throw new IllegalArgumentException("Unknown parity: "+Integer.toString(parity));
        }
    }

    public void setSerialPortParams(int baudRate, int dataBits, int stopBits, int parity) throws UnsupportedCommOperationException {
        _serialPort.setBaudRate(baudRate);
        _serialPort.setNumDataBits(dataBits);
        _serialPort.setNumStopBits(stopBits);
        setParity(parity);
    }

    public void addEventListener(SerialPortEventListener listener) throws TooManyListenersException {
        if (listener == null) {
            throw new IllegalArgumentException("listener cannot be null");
        }
        if (this._eventListener != null) {
            throw new TooManyListenersException();
        }
        this._eventListener = listener;
        if (!_threadStarted) {
            _threadStarted = true;
            _inputThread.start();
        }
    }

    public void notifyOnDataAvailable(boolean value) {
        _notifyOnDataAvailable = value;
    }

    public void setFlowControlMode(int mode) throws UnsupportedCommOperationException {
        switch (mode) {
            case FLOWCONTROL_NONE:
                _serialPort.setFlowControl(AbstractSerialPortController.FlowControl.NONE);
                break;

            case (FLOWCONTROL_RTSCTS_IN | FLOWCONTROL_RTSCTS_OUT):
                _serialPort.setFlowControl(AbstractSerialPortController.FlowControl.RTSCTS);
                break;

            default:
                throw new IllegalArgumentException("Unknown flow control mode: "+Integer.toString(mode));
        }
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

    public boolean isRI() {
        return _serialPort.getRI();
    }

    public boolean isReceiveTimeoutEnabled() {
        return false;   // Not implemented
    }

    public int getReceiveTimeout() {
        return 0;   // Not implemented
    }

    public void close() {
        _serialPort.closePort();
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SerialPort.class);
}

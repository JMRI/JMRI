package jmri.jmrix.fakeport;

import java.io.InputStream;
import java.io.OutputStream;

import jmri.jmrix.*;

/**
 * Implementation of a fake serial port.
 *
 * @author Daniel Bergqvist (C) 2024
 */
public class FakeSerialPort implements SerialPort {

    @Override
    public void addDataListener(SerialPortDataListener listener) {
        // Do nothing
    }

    @Override
    public InputStream getInputStream() {
        return InputStream.nullInputStream();
    }

    @Override
    public OutputStream getOutputStream() {
        return OutputStream.nullOutputStream();
    }

    @Override
    public void setRTS() {
        // Do nothing
    }

    @Override
    public void clearRTS() {
        // Do nothing
    }

    @Override
    public void setBaudRate(int baudrate) {
        // Do nothing
    }

    @Override
    public int getBaudRate() {
        return 9600;
    }

    @Override
    public void setNumDataBits(int bits) {
        // Do nothing
    }

    @Override
    public int getNumDataBits() {
        return 8;
    }

    @Override
    public void setNumStopBits(int bits) {
        // Do nothing
    }

    @Override
    public int getNumStopBits() {
        return 1;
    }

    @Override
    public void setParity(Parity parity) {
        // Do nothing
    }

    @Override
    public Parity getParity() {
        return Parity.NONE;
    }

    @Override
    public void setDTR() {
        // Do nothing
    }

    @Override
    public void clearDTR() {
        // Do nothing
    }

    @Override
    public boolean getDTR() {
        return false;
    }

    @Override
    public boolean getRTS() {
        return false;
    }

    @Override
    public boolean getDSR() {
        return false;
    }

    @Override
    public boolean getCTS() {
        return false;
    }

    @Override
    public boolean getDCD() {
        return false;
    }

    @Override
    public boolean getRI() {
        return false;
    }

    @Override
    public void setFlowControl(AbstractSerialPortController.FlowControl flow) {
        // Do nothing
    }

    @Override
    public void setBreak() {
        // Do nothing
    }

    @Override
    public void clearBreak() {
        // Do nothing
    }

    @Override
    public int getFlowControlSettings() {
        return 0;
    }

    @Override
    public boolean setComPortTimeouts(int newTimeoutMode, int newReadTimeout, int newWriteTimeout) {
        return true;
    }

    @Override
    public void closePort() {
        // Do nothing
    }

    @Override
    public String getDescriptivePortName() {
        return "FakePort";
    }

}

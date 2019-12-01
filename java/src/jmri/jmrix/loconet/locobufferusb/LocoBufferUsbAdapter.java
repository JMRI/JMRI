package jmri.jmrix.loconet.locobufferusb;

import jmri.jmrix.loconet.locobuffer.LocoBufferAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import purejavacomm.SerialPort;
import purejavacomm.UnsupportedCommOperationException;

/**
 * Override {@link jmri.jmrix.loconet.locobuffer.LocoBufferAdapter} so that it refers to the
 * (switch) settings on the LocoBuffer-USB.
 *
 * @author Bob Jacobsen Copyright (C) 2004, 2005
 */
public class LocoBufferUsbAdapter extends LocoBufferAdapter {

    public LocoBufferUsbAdapter() {
        super();
        options.remove(option1Name);
    }

    /**
     * Always use flow control, not considered a user-settable option.
     */
    @Override
    protected void setSerialPort(SerialPort activeSerialPort) throws UnsupportedCommOperationException {
        // find the baud rate value, configure comm options
        // default, must match fixed adapter setting as speed not stored for LB usb
        int baud = currentBaudNumber(mBaudRate);
        activeSerialPort.setSerialPortParams(baud, SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

        // configure flow control to always on
        int flow = SerialPort.FLOWCONTROL_RTSCTS_OUT;
        configureLeadsAndFlowControl(activeSerialPort, flow);

        log.info("LocoBuffer-USB adapter"
                + (activeSerialPort.getFlowControlMode() == SerialPort.FLOWCONTROL_RTSCTS_OUT ? " set hardware flow control, mode=" : " set no flow control, mode=")
                + activeSerialPort.getFlowControlMode()
                + " RTSCTS_OUT=" + SerialPort.FLOWCONTROL_RTSCTS_OUT
                + " RTSCTS_IN=" + SerialPort.FLOWCONTROL_RTSCTS_IN);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] validBaudRates() {
        return new String[]{Bundle.getMessage("Baud57600")};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int[] validBaudNumbers() {
        return new int[]{57600};
    }

    @Override
    public int defaultBaudIndex() {
        return 0;
    }

    private final static Logger log = LoggerFactory.getLogger(LocoBufferUsbAdapter.class);

}

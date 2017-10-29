package jmri.jmrix.loconet.locobufferusb;

import jmri.jmrix.loconet.locobuffer.LocoBufferAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import purejavacomm.SerialPort;
import purejavacomm.UnsupportedCommOperationException;

/**
 * Update the code in jmri.jmrix.loconet.locobuffer so that it refers to the
 * switch settings on the new LocoBuffer-USB
 *
 * @author Bob Jacobsen Copyright (C) 2004, 2005
  */
public class LocoBufferUsbAdapter extends LocoBufferAdapter {

    public LocoBufferUsbAdapter() {
        super();
        options.remove(option1Name);
    }

    /**
     * Always use flow control, not considered a user-setable option
     */
    @Override
    protected void setSerialPort(SerialPort activeSerialPort) throws UnsupportedCommOperationException {
        // find the baud rate value, configure comm options
        int baud = 19200;  // default, but also defaulted in the initial value of selectedSpeed
        for (int i = 0; i < validBaudNumber().length; i++) {
            if (validBaudRates()[i].equals(mBaudRate)) {
                baud = validBaudNumber()[i];
            }
        }
        activeSerialPort.setSerialPortParams(baud, SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

        // configure flow control to always on
        int flow = SerialPort.FLOWCONTROL_RTSCTS_OUT;
        configureLeadsAndFlowControl(activeSerialPort, flow);

        log.debug("Found flow control " + activeSerialPort.getFlowControlMode()
                + " RTSCTS_OUT=" + SerialPort.FLOWCONTROL_RTSCTS_OUT
                + " RTSCTS_IN= " + SerialPort.FLOWCONTROL_RTSCTS_IN);
    }

    /**
     * Get an array of valid baud rates.
     */
    @Override
    public String[] validBaudRates() {
        return new String[]{"57,600 baud"};
    }

    /**
     * Get an array of valid baud rates as integers. This allows subclasses to
     * change the arrays of speeds.
     */
    @Override
    public int[] validBaudNumber() {
        return new int[]{57600};
    }

    private final static Logger log = LoggerFactory.getLogger(LocoBufferUsbAdapter.class);
}

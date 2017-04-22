package jmri.jmrix.loconet.locobufferusb;

import gnu.io.SerialPort;
import jmri.jmrix.loconet.locobuffer.LocoBufferAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    protected void setSerialPort(SerialPort activeSerialPort) throws gnu.io.UnsupportedCommOperationException {
        // find the baud rate value, configure comm options
        int baud = 19200;  // default, but also defaulted in the initial value of selectedSpeed
        for (int i = 0; i < validBaudNumber().length; i++) {
            if (validBaudRates()[i].equals(mBaudRate)) {
                baud = validBaudNumber()[i];
            }
        }
        activeSerialPort.setSerialPortParams(baud, SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

        // set RTS high, DTR high - done early, so flow control can be configured after
        activeSerialPort.setRTS(true);  // not connected in some serial ports and adapters
        activeSerialPort.setDTR(true);  // pin 1 in Mac DIN8; on main connector, this is DTR

        // configure flow control to always on
        int flow = SerialPort.FLOWCONTROL_RTSCTS_OUT;
        activeSerialPort.setFlowControlMode(flow);
        log.debug("Found flow control " + activeSerialPort.getFlowControlMode() // NOI18N
                + " RTSCTS_OUT=" + SerialPort.FLOWCONTROL_RTSCTS_OUT // NOI18N
                + " RTSCTS_IN= " + SerialPort.FLOWCONTROL_RTSCTS_IN); // NOI18N
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

    private final static Logger log = LoggerFactory.getLogger(LocoBufferUsbAdapter.class.getName());
}

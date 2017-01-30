package jmri.jmrix.can.adapters.gridconnect.can2usbino.serialdriver;

import gnu.io.SerialPort;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.adapters.gridconnect.GcSerialDriverAdapter;
import jmri.jmrix.can.adapters.gridconnect.GcTrafficController;
import jmri.jmrix.can.adapters.gridconnect.can2usbino.GridConnectDoubledMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements SerialPortAdapter for GridConnect adapters.
 * <P>
 * This connects a CAN-USB CAN adapter via a serial com port. Normally
 * controlled by the SerialDriverFrame class.
 * <P>
 *
 * @author	Andrew Crosland Copyright (C) 2008
 * @author	Bob Jacobsen Copyright (C) 2009, 2012
 * 
 */
public class SerialDriverAdapter extends GcSerialDriverAdapter implements jmri.jmrix.SerialPortAdapter {

    public SerialDriverAdapter() {
        super();
        mBaudRate = "230,400";
    }

    /**
     * Get an array of valid baud rates.
     */
    public String[] validBaudRates() {
        return new String[]{"57,600", "115,200", "230,400", "250,000", "288,000", "333,333", "460,800"};
    }

    /**
     * And the corresponding values.
     */
    public int[] validBaudValues() {
        return new int[]{57600, 115200, 230400, 250000, 288000, 333333, 460800};
    }

    public String openPort(String portName, String appName) {
        try {
            String retval = super.openPort(portName, appName);
            activeSerialPort.setSerialPortParams(activeSerialPort.getBaudRate(), SerialPort.DATABITS_8, SerialPort.STOPBITS_2, SerialPort.PARITY_NONE);
            activeSerialPort.setFlowControlMode(SerialPort.FLOWCONTROL_XONXOFF_OUT);
            activeSerialPort.setFlowControlMode(SerialPort.FLOWCONTROL_XONXOFF_IN);
            return retval;
        } catch (gnu.io.UnsupportedCommOperationException e) {
            log.error("error configuring port", e);
            return null;
        }
    }

    protected GcTrafficController makeGcTrafficController() {
        return new GcTrafficController() {
            public AbstractMRMessage encodeForHardware(CanMessage m) {
                //log.debug("Encoding for hardware");
                return new GridConnectDoubledMessage(m);
            }
        };
    }

    private final static Logger log = LoggerFactory.getLogger(SerialDriverAdapter.class.getName());

}

package jmri.util;

import purejavacomm.SerialPort;
import purejavacomm.UnsupportedCommOperationException;

/**
 * Common utility methods for working with Serial Ports.
 * <p>
 * This is a libary of functions for handling specific operations related to
 * serial ports.
 * <p>
 * In some cases, these routines use may use a JavaComm 3 or later method,
 * falling back to JavaComm 2 if necessary.
 *
 * @author Paul Bender Copyright 2007
 */
public class SerialUtil {

    static public void setSerialPortParams(SerialPort activeSerialPort, int baud, int databits, int stopbits, int parity)
            throws UnsupportedCommOperationException {
        /* 
         * First try once to work around bug, then do again for real
         * see http://wiki.gb.nrao.edu/bin/view/Pennarray/JavaComm3
         */
        try {
            activeSerialPort.setSerialPortParams(baud, databits, stopbits, parity);
        } catch (UnsupportedCommOperationException et) {
            // Work around Sun Comm bug
        }
        activeSerialPort.setSerialPortParams(baud, databits, stopbits, parity);
    }
}

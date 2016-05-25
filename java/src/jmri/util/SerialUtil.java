// SocketUtil.java
package jmri.util;

import gnu.io.SerialPort;

/**
 * Common utility methods for working with Serial Ports.
 * <P>
 * This is a libary of functions for handling specific operations related to
 * serial ports.
 * <P>
 * In some cases, these routines use may use a JavaComm 3 or later method,
 * falling back to JavaComm 2 if necessary.
 *
 * @author Paul Bender Copyright 2007
 * @version $Revision$
 */
public class SerialUtil {

    static public void setSerialPortParams(SerialPort activeSerialPort, int baud, int databits, int stopbits, int parity)
            throws gnu.io.UnsupportedCommOperationException {
        /* 
         * First try once to work around bug, then do again for real
         * see http://wiki.gb.nrao.edu/bin/view/Pennarray/JavaComm3
         */
        try {
            activeSerialPort.setSerialPortParams(baud, databits, stopbits, parity);
        } catch (Exception et) {
            // Work around Sun Comm bug
        }
        activeSerialPort.setSerialPortParams(baud, databits, stopbits, parity);
    }
}

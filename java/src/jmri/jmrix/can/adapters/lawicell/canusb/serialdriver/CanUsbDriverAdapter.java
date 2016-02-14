// CanUsbDriverAdapter.java
package jmri.jmrix.can.adapters.lawicell.canusb.serialdriver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements SerialPortAdapter for the CAN-USB.
 * <P>
 * This connects a CAN-USB CAN adapter via a serial com port. Normally
 * controlled by the SerialDriverFrame class.
 * <P>
 *
 * @author	Andrew Crosland Copyright (C) 2008
 * @author	Bob Jacobsen Copyright (C) 2008, 2010
 * @version	$Revision$
 */
public class CanUsbDriverAdapter
        extends jmri.jmrix.can.adapters.lawicell.SerialDriverAdapter
        implements jmri.jmrix.SerialPortAdapter {

    /**
     * Get an array of valid baud rates.
     */
    public String[] validBaudRates() {
        return new String[]{"57,600", "115,200", "230,400", "250,000", "333,333", "460,800", "500,000"};
    }

    /**
     * And the corresponding values.
     */
    public int[] validBaudValues() {
        return new int[]{57600, 115200, 230400, 250000, 333333, 460800, 500000};
    }

    private final static Logger log = LoggerFactory.getLogger(CanUsbDriverAdapter.class.getName());

}

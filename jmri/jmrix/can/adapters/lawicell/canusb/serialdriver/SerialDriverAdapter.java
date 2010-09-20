// SerialDriverAdapter.java

package jmri.jmrix.can.adapters.lawicell.canusb.serialdriver;

/**
 * Implements SerialPortAdapter for the CAN-USB.
 * <P>
 * This connects a CAN-USB CAN adapter via a serial com port.
 * Normally controlled by the SerialDriverFrame class.
 * <P>
 *
 * @author			Andrew Crosland Copyright (C) 2008
 * @author			Bob Jacobsen Copyright (C) 2008, 2010
 * @version			$Revision: 1.5 $
 */
public class SerialDriverAdapter 
        extends jmri.jmrix.can.adapters.lawicell.SerialDriverAdapter 
        implements jmri.jmrix.SerialPortAdapter {

    /**
     * Get an array of valid baud rates.
     */
    public String[] validBaudRates() {
        return new String[]{"57,600", "115,200", "250,000", "333,333", "460,800", "500,000"};
    }
    
    /**
     * And the corresponding values.
     */
    public int[] validBaudValues() {
        return new int[]{57600, 115200, 250000, 333333, 460800, 500000};
    }
        
    static public SerialDriverAdapter instance() {
        if (mInstance == null) mInstance = new SerialDriverAdapter();
        return mInstance;
    }
    static SerialDriverAdapter mInstance = null;

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SerialDriverAdapter.class.getName());

}

// SerialDriverAdapter.java

package jmri.jmrix.can.adapters.gridconnect.canusb.serialdriver;

import jmri.jmrix.can.adapters.gridconnect.GcSerialDriverAdapter;

/**
 * Implements SerialPortAdapter for the CAN-USB.
 * <P>
 * This connects a CAN-USB CAN adapter via a serial com port.
 * Normally controlled by the SerialDriverFrame class.
 * <P>
 *
 * @author			Andrew Crosland Copyright (C) 2008
 * @version			$Revision: 1.1 $
 */
public class SerialDriverAdapter extends GcSerialDriverAdapter  implements jmri.jmrix.SerialPortAdapter {

    protected void setActive() { 
        jmri.jmrix.can.adapters.gridconnect.canusb.ActiveFlag.setActive();
    }
    
    /**
     * Get an array of valid baud rates.
     */
    public String[] validBaudRates() {
        return validSpeeds;
    }
    
    /**
     * And the corresponding values.
     */
    public int[] validBaudValues() {
        return validSpeedValues;
    }
    
    protected String [] validSpeeds = new String[]{"460,800"};
    protected int [] validSpeedValues = new int[]{460800};
    
    static public SerialDriverAdapter instance() {
        if (mInstance == null) mInstance = new SerialDriverAdapter();
        return mInstance;
    }
    static SerialDriverAdapter mInstance = null;

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SerialDriverAdapter.class.getName());

}

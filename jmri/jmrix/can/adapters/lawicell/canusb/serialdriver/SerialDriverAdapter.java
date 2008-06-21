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
 * @author			Bob Jacobsen Copyright (C) 2008
 * @version			$Revision: 1.1 $
 */
public class SerialDriverAdapter 
        extends jmri.jmrix.can.adapters.lawicell.SerialDriverAdapter 
        implements jmri.jmrix.SerialPortAdapter {

    protected void setActive() { 
        jmri.jmrix.can.adapters.lawicell.canusb.ActiveFlag.setActive();
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
    
    protected String [] validSpeeds = new String[]{"57,600"};
    protected int [] validSpeedValues = new int[]{57600};
    
    static public SerialDriverAdapter instance() {
        if (mInstance == null) mInstance = new SerialDriverAdapter();
        return mInstance;
    }
    static SerialDriverAdapter mInstance = null;

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SerialDriverAdapter.class.getName());

}

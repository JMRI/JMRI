// NceUSB.java

package jmri.jmrix.nce;

/**
 * USB -> Cab bus adapter:
 * When used with PowerCab V1.28 - 6.3.0
 * When used with SB3 V1.28 - 6.3.1 (No program track on an SB3)
 * When used with PH-Pro or PH-10 - 6.3.2 (limited set of features available
 * through cab bus)
 * 
 * @author Daniel Boudreau Copyright (C) 2007
 * @version     $Revision: 1.1 $
 */
public class NceUSB  {
	
	/**
	 * Default when a NCE USB isn't selected in user system preferences
	 */
	static public final int USB_SYSTEM_NONE = 0;
	
	/**
	 * Create commands compatible with a NCE USB connected to a PowerCab
	 */
	static public final int USB_SYSTEM_POWERCAB = 1;
	
	/**
	 * Create commands compatible with a NCE USB connected to a Smart Booster
	 */
	static public final int USB_SYSTEM_SB3 = 2;
	
	/**
	 * Create commands compatible with a NCE USB connected to a PowerHouse
	 */
	static public final int USB_SYSTEM_POWERHOUSE = 4;
	
	static int usbSystem = USB_SYSTEM_NONE;
	static boolean usbSystemSet = false;
	
    /**
     * Set the type of system the NCE USB is connected to
     * @param val
     */
	
	static public void setUsbSystem(int val) {
    	usbSystem = val;
        if (usbSystemSet) {
            log.error("setUsbSystem called more than once");
            new Exception().printStackTrace();
        }
        usbSystemSet = true;
    }
	
	static public int getUsbSystem() {return usbSystem;}
	

    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NceUSB.class.getName());

}


/* @(#)NceUSB.java */







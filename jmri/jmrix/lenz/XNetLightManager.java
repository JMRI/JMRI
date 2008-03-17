// XNetLightManager.java

package jmri.jmrix.lenz;

import jmri.AbstractLightManager;
import jmri.Light;

/**
 * Implement light manager for XPressNet systems
 * <P>
 * System names are "XLnnnnn", where nnnnn is the bit number without padding.
 * <P>
 * Based in part on SerialLightManager.java
 *
 * @author	Paul Bender Copyright (C) 2008
 * @version	$Revision: 1.1 $
 */
public class XNetLightManager extends AbstractLightManager {

    public XNetLightManager() {
        _instance = this;
    }

    /**
     *  Returns the system letter for Loconet
     */
    public char systemLetter() { return 'X'; }
    
    /**
     * Method to create a new Light based on the system name
     * Returns null if the system name is not in a valid format
     * Assumes calling method has checked that a Light with this
     * system name does not already exist
     */
    public Light createNewLight(String systemName, String userName) {
        Light lgt = null;
		// check if the output bit is available
		int bitNum = getBitFromSystemName(systemName);
		if (bitNum == 0) return (null);
        // Normalize the systemName
		String sName = "XL"+bitNum;   // removes any leading zeros
		// make the new Light object
		lgt = new XNetLight(sName,userName); 
        return lgt;
    }    
	
    /**
     *  Get the bit address from the system name 
     */
	public int getBitFromSystemName (String systemName) {
        // validate the system Name leader characters
        if ( (systemName.charAt(0) != 'X') || (systemName.charAt(1) != 'L') ) {
            // here if an illegal loconet light system name 
            log.error("illegal character in header field of loconet light system name: "+systemName);
            return (0);
        }
	// name must be in the XLnnnnn format
        int num = 0;
		try {
			num = Integer.valueOf(systemName.substring(2)).intValue();
		}
		catch (Exception e) {
			log.error("illegal character in number field of system name: "+systemName);
			return (0);
		}
		if (num<=0) {
			log.error("invalid XPressNet light system name: "+systemName);
			return (0);
        }
		else if (num>1024) {
			log.error("bit number out of range in XPressNet light system name: "+systemName);
			return (0);
        }
        return (num);
    }	
	
    /**
     * Public method to validate system name format
     *   returns 'true' if system name has a valid format, else returns 'false'
     */
    public boolean validSystemNameFormat(String systemName) {
		return (getBitFromSystemName(systemName)!=0);
    }

    /**
     * Public method to validate system name for configuration
     *   returns 'true' if system name has a valid meaning in current 
     *   configuration, else returns 'false'
     *   for now, this method always returns 'true'; it is needed for the 
     *   Abstract Light class
     */
    public boolean validSystemNameConfig(String systemName) {
        return (true);
    }
    
    /** 
     * Allow access to XNetLightManager
     */
    static public XNetLightManager instance() {
        if (_instance == null) _instance = new XNetLightManager();
        return _instance;
    }
    static XNetLightManager _instance = null;

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(XNetLightManager.class.getName());

}

/* @(#)XNetLightManager.java */

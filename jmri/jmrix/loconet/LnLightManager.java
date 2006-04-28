// LnLightManager.java

package jmri.jmrix.loconet;

import jmri.AbstractLightManager;
import jmri.Light;

/**
 * Implement light manager for loconet systems
 * <P>
 * System names are "LLnnnnn", where nnnnn is the bit number without padding.
 * <P>
 * Based in part on SerialLightManager.java
 *
 * @author	Dave Duchamp Copyright (C) 2006
 * @version	$Revision: 1.1 $
 */
public class LnLightManager extends AbstractLightManager {

    public LnLightManager() {
        _instance = this;
    }

    /**
     *  Returns the system letter for Loconet
     */
    public char systemLetter() { return 'L'; }
    
    /**
     * Method to create a new Light based on the system name
     * Returns null if the system name is not in a valid format
	 * Assumes calling method has checked that a Light with this
     *    system name does not already exist
     */
    public Light createNewLight(String systemName, String userName) {
        Light lgt = null;
		// check if the output bit is available
		int bitNum = getBitFromSystemName(systemName);
		if (bitNum == 0) return (null);
        // Normalize the systemName
		String sName = "LL"+bitNum;   // removes any leading zeros
		// make the new Light object
		lgt = new LnLight(sName,userName); 
        return lgt;
    }    
	
    /**
     *  Get the bit address from the system name 
	 */
	public int getBitFromSystemName (String systemName) {
        // validate the system Name leader characters
        if ( (systemName.charAt(0) != 'L') || (systemName.charAt(1) != 'L') ) {
            // here if an illegal loconet light system name 
            log.error("illegal character in header field of loconet light system name: "+systemName);
            return (0);
        }
		// name must be in the LLnnnnn format
        int num = 0;
		try {
			num = Integer.valueOf(systemName.substring(2)).intValue();
		}
		catch (Exception e) {
			log.error("illegal character in number field of system name: "+systemName);
			return (0);
		}
		if (num<=0) {
			log.error("invalid loconet light system name: "+systemName);
			return (0);
        }
		else if (num>4096) {
			log.error("bit number out of range in loconet light system name: "+systemName);
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
     *   returns 'true' if system name has a valid meaning in current configuration, 
     *      else returns 'false'
	 *   for now, this method always returns 'true'; it is needed for the Abstract Light class
     */
    public boolean validSystemNameConfig(String systemName) {
        return (true);
    }
    
    /** 
     * Allow access to LnLightManager
     */
    static public LnLightManager instance() {
        if (_instance == null) _instance = new LnLightManager();
        return _instance;
    }
    static LnLightManager _instance = null;

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LnLightManager.class.getName());

}

/* @(#)LnLightManager.java */

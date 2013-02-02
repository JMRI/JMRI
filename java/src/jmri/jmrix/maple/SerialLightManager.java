// SerialLightManager.java

package jmri.jmrix.maple;

import org.apache.log4j.Logger;
import jmri.managers.AbstractLightManager;
import jmri.Light;

/**
 * Implement light manager for serial systems
 * <P>
 * System names are "KLnnn", where nnn is the bit number without padding.
 * <P>
 * Based in part on SerialTurnoutManager.java
 *
 * @author  Bob Jacobsen Copyright (C) 2008
 * @author	Dave Duchamp Copyright (C) 2004, 2010
 * @version	$Revision$
 */
public class SerialLightManager extends AbstractLightManager {

    public SerialLightManager() {
    	
    }

    /**
     *  Returns the system letter
     */
    public String getSystemPrefix() { return "K"; }
    
    /**
     * Method to create a new Light based on the system name
     * Returns null if the system name is not in a valid format or
     *    if the system name does not correspond to a configured
     *    digital output bit
     * Assumes calling method has checked that a Light with this
     *    system name does not already exist
     */
    public Light createNewLight(String systemName, String userName) {
        Light lgt = null;
		// check if the output bit is available
		int bitNum = SerialAddress.getBitFromSystemName(systemName);
		if (bitNum == 0) return (null);
		String conflict = "";
		conflict = SerialAddress.isOutputBitFree(bitNum);
		if ( conflict != "" ) {
			log.error("Assignment conflict with "+conflict+".  Light not created.");
			notifyLightCreationError(conflict,bitNum);
			return (null);
		}
        // Validate the systemName
		String sysName = SerialAddress.normalizeSystemName(systemName);
		if (sysName=="") {
			log.error("error when normalizing system name "+systemName);
			return null;
		}
        if ( SerialAddress.validSystemNameFormat(systemName,'L') ) {
            lgt = new SerialLight(sysName,userName); 
            if (!SerialAddress.validSystemNameConfig(sysName,'L')) {
                log.warn("Light system Name '"+sysName+"' does not refer to configured hardware.");
				javax.swing.JOptionPane.showMessageDialog(null,"WARNING - The Light just added, "+sysName+
					", refers to an unconfigured output bit.","Configuration Warning",
						javax.swing.JOptionPane.INFORMATION_MESSAGE,null);
           }
        }
        else {
            log.error("Invalid Light system Name format: "+systemName);
        }
        return lgt;
    }    

    /**
     * Public method to notify user of Light creation error.
     */
	public void notifyLightCreationError(String conflict,int bitNum) {
		javax.swing.JOptionPane.showMessageDialog(null,"The output bit, "+bitNum+
			", is currently assigned to "+conflict+". Light cannot be created as "+
					"you specified.","Assignment Conflict",
						javax.swing.JOptionPane.INFORMATION_MESSAGE,null);	
	}
	
    /**
     * Public method to validate system name format
     *   returns 'true' if system name has a valid format, else returns 'false'
     */
    public boolean validSystemNameFormat(String systemName) {
        return (SerialAddress.validSystemNameFormat(systemName,'L'));
	}

    /**
     * Public method to validate system name for configuration
     *   returns 'true' if system name has a valid meaning in current configuration, 
     *      else returns 'false'
     */
    public boolean validSystemNameConfig(String systemName) {
        return (SerialAddress.validSystemNameConfig(systemName,'L'));
    }
    
    /**
     * Public method to normalize a system name
     * <P>
     * Returns a normalized system name if system name has a valid format, 
     *      else returns "".
     */
    public String normalizeSystemName(String systemName) {
        return (SerialAddress.normalizeSystemName(systemName));
    }

    /** 
     * Allow access to SerialLightManager
     */
    static public SerialLightManager instance() {
        if (_instance == null) _instance = new SerialLightManager();
        return _instance;
    }
    static SerialLightManager _instance = null;

    static Logger log = Logger.getLogger(SerialLightManager.class.getName());

}

/* @(#)SerialLighttManager.java */

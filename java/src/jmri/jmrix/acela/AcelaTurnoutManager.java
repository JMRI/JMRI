// AcelaTurnoutManager.java

package jmri.jmrix.acela;

import org.apache.log4j.Logger;
import jmri.managers.AbstractTurnoutManager;
import jmri.Turnout;

/**
 * Implement light manager for Acela systems
 * <P>
 * System names are "ALnnn", where nnn is the bit number without padding.
 * <P>
 * Based in part on AcelaTurnoutManager.java
 *
 * @author	Dave Duchamp Copyright (C) 2004
 * @version	$Revision$
 *
 * @author	Bob Coleman Copyright (C) 2008
 *              Based on CMRI serial example, modified to establish Acela support. 
 */
public class AcelaTurnoutManager extends AbstractTurnoutManager {

    public AcelaTurnoutManager() {
    	
    }

    /**
     *  Returns the system letter for Acela
     */
    public String getSystemPrefix() { return "A"; }
    
    /**
     * Method to create a new Light based on the system name
     * Returns null if the system name is not in a valid format
     * Assumes calling method has checked that a Light with this
     *    system name does not already exist
     */
    public Turnout createNewTurnout(String systemName, String userName) {
        Turnout trn = null;
	// check if the output bit is available
	int nAddress = -1;
	nAddress = AcelaAddress.getNodeAddressFromSystemName(systemName);
	if (nAddress == -1) return (null);
	int bitNum = AcelaAddress.getBitFromSystemName(systemName);
	if (bitNum == -1) return (null);
	
// Bob C: Fix this up		
/*
		conflict = AcelaAddress.isOutputBitFree(nAddress,bitNum);
		if ( conflict != "" ) {
			log.error("Assignment conflict with "+conflict+".  Light not created.");
			notifyLightCreationError(conflict,bitNum);
			return (null);
		}
*/

        // Validate the systemName
        if ( AcelaAddress.validSystemNameFormat(systemName,'T') ) {
            trn = new AcelaTurnout(systemName,userName); 
            if (!AcelaAddress.validSystemNameConfig(systemName,'T')) {
                log.warn("Turnout system Name does not refer to configured hardware: "
                                                            +systemName);
            }
        } else {
            log.error("Invalid Turnout system Name format: "+systemName);
        }
        return trn;
    }    

    /**
     * Public method to notify user of Light creation error.
     */
	public void notifyTurnoutCreationError(String conflict,int bitNum) {
		javax.swing.JOptionPane.showMessageDialog(null,"The output bit, "+bitNum+
			", is currently assigned to "+conflict+". Turnout cannot be created as "+
					"you specified.","Acela Assignment Conflict",
						javax.swing.JOptionPane.INFORMATION_MESSAGE,null);	
	}
	
    /**
     * Public method to validate system name format
     *   returns 'true' if system name has a valid format, else returns 'false'
     */
    public boolean validSystemNameFormat(String systemName) {
        return (AcelaAddress.validSystemNameFormat(systemName,'T'));
        }

    /**
     * Public method to validate system name for configuration
     *   returns 'true' if system name has a valid meaning in current configuration, 
     *      else returns 'false'
     */
    public boolean validSystemNameConfig(String systemName) {
        return (AcelaAddress.validSystemNameConfig(systemName,'T'));
    }
    
    /**
     * Public method to normalize a system name
     * <P>
     * Returns a normalized system name if system name has a valid format, 
     *      else returns "".
     */
    public String normalizeSystemName(String systemName) {
        return (AcelaAddress.normalizeSystemName(systemName));
    }
    
    /**
     * Public method to convert system name to its alternate format
     * <P>
     * Returns a normalized system name if system name is valid and has a 
     *      valid alternate representation, else return "".
     */
    public String convertSystemNameToAlternate(String systemName) {
        return (AcelaAddress.convertSystemNameToAlternate(systemName));
    }
    
    /** 
     * Allow access to AcelaLightManager
     */
    static public AcelaTurnoutManager instance() {
        if (_instance == null) _instance = new AcelaTurnoutManager();
        return _instance;
    }
    static AcelaTurnoutManager _instance = null;

    static Logger log = Logger.getLogger(AcelaTurnoutManager.class.getName());
}

/* @(#)AcelaTurnoutManager.java */

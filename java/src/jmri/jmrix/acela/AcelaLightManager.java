package jmri.jmrix.acela;

import jmri.Light;
import jmri.managers.AbstractLightManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement light manager for Acela systems
 * <P>
 * System names are "ALnnn", where nnn is the bit number without padding.
 * <P>
 * Based in part on AcelaTurnoutManager.java
 *
 * @author	Dave Duchamp Copyright (C) 2004
 *
 * @author	Bob Coleman Copyright (C) 2007, 2008 Based on CMRI serial example,
 * modified to establish Acela support.
 */
public class AcelaLightManager extends AbstractLightManager {

    /**
     *
     */
    private static final long serialVersionUID = 854966186258234895L;

    public AcelaLightManager() {

    }

    /**
     * Returns the system letter for Acela
     */
    public String getSystemPrefix() {
        return "A";
    }

    /**
     * Method to create a new Light based on the system name Returns null if the
     * system name is not in a valid format Assumes calling method has checked
     * that a Light with this system name does not already exist
     */
    public Light createNewLight(String systemName, String userName) {
        Light lgt = null;
        // check if the output bit is available
        int nAddress = -1;
        nAddress = AcelaAddress.getNodeAddressFromSystemName(systemName);
        if (nAddress == -1) {
            return (null);
        }
        int bitNum = AcelaAddress.getBitFromSystemName(systemName);
        if (bitNum == -1) {
            return (null);
        }

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
        if (AcelaAddress.validSystemNameFormat(systemName, 'L')) {
            lgt = new AcelaLight(systemName, userName);
            if (!AcelaAddress.validSystemNameConfig(systemName, 'L')) {
                log.warn("Light system Name does not refer to configured hardware: "
                        + systemName);
            }
        } else {
            log.error("Invalid Light system Name format: " + systemName);
        }
        return lgt;
    }

    /**
     * Public method to notify user of Light creation error.
     */
    public void notifyLightCreationError(String conflict, int bitNum) {
        javax.swing.JOptionPane.showMessageDialog(null, "The output bit, " + bitNum
                + ", is currently assigned to " + conflict + ". Light cannot be created as "
                + "you specified.", "Acela Assignment Conflict",
                javax.swing.JOptionPane.INFORMATION_MESSAGE, null);
    }

    /**
     * Public method to validate system name format returns 'true' if system
     * name has a valid format, else returns 'false'
     */
    public boolean validSystemNameFormat(String systemName) {
        return (AcelaAddress.validSystemNameFormat(systemName, 'L'));
    }

    /**
     * Public method to validate system name for configuration returns 'true' if
     * system name has a valid meaning in current configuration, else returns
     * 'false'
     */
    public boolean validSystemNameConfig(String systemName) {
        return (AcelaAddress.validSystemNameConfig(systemName, 'L'));
    }

    /**
     * Public method to normalize a system name
     * <P>
     * Returns a normalized system name if system name has a valid format, else
     * returns "".
     */
    public String normalizeSystemName(String systemName) {
        return (AcelaAddress.normalizeSystemName(systemName));
    }

    /**
     * Public method to convert system name to its alternate format
     * <P>
     * Returns a normalized system name if system name is valid and has a valid
     * alternate representation, else return "".
     */
    public String convertSystemNameToAlternate(String systemName) {
        return (AcelaAddress.convertSystemNameToAlternate(systemName));
    }

    /**
     * Allow access to AcelaLightManager
     */
    static public AcelaLightManager instance() {
        if (_instance == null) {
            _instance = new AcelaLightManager();
        }
        return _instance;
    }
    static AcelaLightManager _instance = null;

    private final static Logger log = LoggerFactory.getLogger(AcelaLightManager.class.getName());
}

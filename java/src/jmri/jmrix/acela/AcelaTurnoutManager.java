package jmri.jmrix.acela;

import jmri.Turnout;
import jmri.managers.AbstractTurnoutManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement turnout manager for Acela systems
 * <P>
 * System names are "ATnnn", where nnn is the bit number without padding.
 *
 * @author Dave Duchamp Copyright (C) 2004
 * @author Bob Coleman Copyright (C) 2008 Based on CMRI serial example, modified
 * to establish Acela support.
 */
public class AcelaTurnoutManager extends AbstractTurnoutManager {
 
    AcelaSystemConnectionMemo _memo = null;

    public AcelaTurnoutManager(AcelaSystemConnectionMemo memo) {
       _memo = memo;
    }

    /**
     * Returns the system letter for Acela
     */
    @Override
    public String getSystemPrefix() {
        return _memo.getSystemPrefix();
    }

    /**
     * Method to create a new Turnout based on the system name.
     * <p>
     * Assumes calling method has checked that a Turnout with this
     * system name does not already exist.
     *
     * @return null if the system name is not in a valid format
     */
    @Override
    public Turnout createNewTurnout(String systemName, String userName) {
        Turnout trn = null;
        // check if the output bit is available
        int nAddress = -1;
        nAddress = AcelaAddress.getNodeAddressFromSystemName(systemName,_memo);
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
         log.error("Assignment conflict with "+conflict+".  Turnout not created.");
         notifyTurnoutCreationError(conflict,bitNum);
         return (null);
         }
         */
        // Validate the systemName
        if (AcelaAddress.validSystemNameFormat(systemName, 'T', getSystemPrefix())) {
            trn = new AcelaTurnout(systemName, userName,_memo);
            if (!AcelaAddress.validSystemNameConfig(systemName, 'T',_memo)) {
                log.warn("Turnout system Name does not refer to configured hardware: "
                        + systemName);
            }
        } else {
            log.error("Invalid Turnout system Name format: " + systemName);
        }
        return trn;
    }

    /**
     * Public method to notify user of Turnout creation error.
     */
    public void notifyTurnoutCreationError(String conflict, int bitNum) {
        javax.swing.JOptionPane.showMessageDialog(null, "The output bit, " + bitNum
                + ", is currently assigned to " + conflict + ". Turnout cannot be created as "
                + "you specified.", "Acela Assignment Conflict",
                javax.swing.JOptionPane.INFORMATION_MESSAGE, null);
    }

    /**
     * Public method to validate system name format returns 'true' if system
     * name has a valid format, else returns 'false'
     */
    public boolean validSystemNameFormat(String systemName) {
        return (AcelaAddress.validSystemNameFormat(systemName, 'T', getSystemPrefix()));
    }

    /**
     * Public method to validate system name for configuration returns 'true' if
     * system name has a valid meaning in current configuration, else returns
     * 'false'
     */
    public boolean validSystemNameConfig(String systemName) {
        return (AcelaAddress.validSystemNameConfig(systemName, 'T',_memo));
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

    @Override
    public boolean allowMultipleAdditions(String systemName) {
        return true;
    }

    /**
     * Allow access to AcelaTurnoutManager
     * @deprecated JMRI Since 4.4 instance() shouldn't be used, convert to JMRI multi-system support structure
     */
    @Deprecated
    static public AcelaTurnoutManager instance() {
        return null;
    }

    private final static Logger log = LoggerFactory.getLogger(AcelaTurnoutManager.class.getName());

}

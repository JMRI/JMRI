package jmri.jmrix.tmcc;

import jmri.JmriException;
import jmri.Turnout;
import jmri.managers.AbstractTurnoutManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement turnout manager for TMCC serial systems
 * <P>
 * System names are "TTnnn", where nnn is the turnout number without padding.
 *
 * @author	Bob Jacobsen Copyright (C) 2003, 2006
 */
public class SerialTurnoutManager extends AbstractTurnoutManager {

    public SerialTurnoutManager() {

    }

    public String getSystemPrefix() {
        return "T";
    }

    public Turnout createNewTurnout(String systemName, String userName) {
        // validate the system name, and normalize it
        String sName = SerialAddress.normalizeSystemName(systemName);
        if (sName.equals("")) {
            // system name is not valid
            return null;
        }
        // does this turnout already exist
        Turnout t = getBySystemName(sName);
        if (t != null) {
            return null;
        }
        // check under alternate name
        String altName = SerialAddress.convertSystemNameToAlternate(sName);
        t = getBySystemName(altName);
        if (t != null) {
            return null;
        }
        // create the turnout
        int addr = Integer.valueOf(systemName.substring(2)).intValue();
        t = new SerialTurnout(addr);
        t.setUserName(userName);

        // does system name correspond to configured hardware
        if (!SerialAddress.validSystemNameConfig(sName, 'T')) {
            // system name does not correspond to configured hardware
            log.warn("Turnout '" + sName + "' refers to an undefined Serial Node.");
        }
        return t;
    }

    /**
     * @deprecated JMRI Since 4.4 instance() shouldn't be used, convert to JMRI multi-system support structure
     */
    @Deprecated
    static public SerialTurnoutManager instance() {
        if (_instance == null) {
            _instance = new SerialTurnoutManager();
        }
        return _instance;
    }
    /**
     * @deprecated JMRI Since 4.4 instance() shouldn't be used, convert to JMRI multi-system support structure
     */
    @Deprecated
    static SerialTurnoutManager _instance = null;

    //Turnout address format is more than a simple number.
    //public boolean allowMultipleAdditions(String systemName) { return true;  }
    /**
     * A method that creates an array of systems names to allow bulk creation of
     * turnouts.
     */
    //further work needs to be done on how to format a number of CMRI turnout, therefore this method will only return one entry.
    public String[] formatRangeOfAddresses(String start, int numberToAdd, String prefix) {
        numberToAdd = 1;
        String range[] = new String[numberToAdd];
        for (int x = 0; x < numberToAdd; x++) {
            range[x] = prefix + "T" + start;
        }
        return range;
    }

    public String createSystemName(String curAddress, String prefix) throws JmriException {
        String tmpSName;

        if (curAddress.contains(":")) {
            //Address format passed is in the form node:address
            int seperator = curAddress.indexOf(":");
            try {
                nAddress = Integer.valueOf(curAddress.substring(0, seperator)).intValue();
                bitNum = Integer.valueOf(curAddress.substring(seperator + 1)).intValue();
            } catch (NumberFormatException ex) {
                throw new JmriException("Unable to convert " + curAddress + " to a valid Hardware Address");
            }
            tmpSName = SerialAddress.makeSystemName("T", nAddress, bitNum);
        } else {
            tmpSName = prefix + "T" + curAddress;
            try {
                bitNum = SerialAddress.getBitFromSystemName(tmpSName);
                nAddress = SerialAddress.getNodeAddressFromSystemName(tmpSName);
            } catch (NumberFormatException ex) {
                throw new JmriException("Unable to convert " + curAddress + " to a valid Hardware Address");
            }
        }
        return (tmpSName);
    }

    int bitNum = 0;
    int nAddress = 0;

    public String getNextValidAddress(String curAddress, String prefix) {

        String tmpSName = "";
        try {
            tmpSName = createSystemName(curAddress, prefix);
        } catch (JmriException ex) {
            log.error("Unable to convert " + curAddress + " Hardware Address to a number");
            jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                    showErrorMessage("Error", "Unable to convert " + curAddress + " to a valid Hardware Address", "" + ex, "", true, false);
            return null;
        }

        //If the hardware address past does not already exist then this can
        //be considered the next valid address.
        Turnout t = getBySystemName(tmpSName);
        if (t == null) {
            int seperator = tmpSName.lastIndexOf("T") + 1;
            curAddress = tmpSName.substring(seperator);
            return curAddress;
        }

        //The Number of Output Bits of the previous turnout will help determine the next
        //valid address.
        bitNum = bitNum + t.getNumberOutputBits();
        //Check to determine if the systemName is in use, return null if it is,
        //otherwise return the next valid address.
        tmpSName = SerialAddress.makeSystemName("T", nAddress, bitNum);
        t = getBySystemName(tmpSName);
        if (t != null) {
            for (int x = 1; x < 10; x++) {
                bitNum = bitNum + t.getNumberOutputBits();
                //This should increment " + bitNum
                tmpSName = SerialAddress.makeSystemName("T", nAddress, bitNum);
                t = getBySystemName(tmpSName);
                if (t == null) {
                    int seperator = tmpSName.lastIndexOf("T") + 1;
                    curAddress = tmpSName.substring(seperator);
                    return curAddress;
                }
            }
            return null;
        } else {
            int seperator = tmpSName.lastIndexOf("T") + 1;
            curAddress = tmpSName.substring(seperator);
            return curAddress;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(SerialTurnoutManager.class.getName());

}

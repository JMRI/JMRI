package jmri.jmrix.grapevine;

import jmri.JmriException;
import jmri.Turnout;
import jmri.managers.AbstractTurnoutManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement turnout manager for Grapevine systems
 * <P>
 * System names are "GTnnn", where nnn is the turnout number without padding.
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2006, 2007, 2008
  */
public class SerialTurnoutManager extends AbstractTurnoutManager {

    GrapevineSystemConnectionMemo memo = null;

    public SerialTurnoutManager(GrapevineSystemConnectionMemo _memo) {
       memo = _memo;
    }

    @Override
    public String getSystemPrefix() {
        return memo.getSystemPrefix();
    }

    @Override
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
        t = new SerialTurnout(sName, userName,memo);

        // does system name correspond to configured hardware
        if (!SerialAddress.validSystemNameConfig(sName, 'T',memo.getTrafficController())) {
            // system name does not correspond to configured hardware
            log.warn("Turnout '" + sName + "' refers to an undefined Serial Node.");
        }
        return t;
    }

    @Deprecated
    static public SerialTurnoutManager instance() {
        return null;
    }

    @Override
    public boolean allowMultipleAdditions(String systemName) {
        return false; //Turnout address format is more than a simple number.
    }

    @Override
    public String createSystemName(String curAddress, String prefix) throws JmriException {
        String tmpSName = prefix + "T" + curAddress;

        if (curAddress.contains(":")) {
            //Address format passed is in the form of node:cardOutput or node:card:address
            int seperator = curAddress.indexOf(":");
            try {
                nNode = Integer.valueOf(curAddress.substring(0, seperator)).intValue();
                int nxSeperator = curAddress.indexOf(":", seperator + 1);
                if (nxSeperator == -1) {
                    //Address has been entered in the format node:cardOutput
                    bitNum = Integer.valueOf(curAddress.substring(seperator + 1)).intValue();
                } else {
                    //Address has been entered in the format node:card:output
                    nCard = Integer.valueOf(curAddress.substring(seperator + 1, nxSeperator)).intValue() * 100;
                    bitNum = Integer.valueOf(curAddress.substring(nxSeperator + 1)).intValue();
                }
            } catch (NumberFormatException ex) {
                log.error("Unable to convert " + curAddress + " Hardware Address to a number");
                throw new JmriException("Hardware Address passed should be a number");
            }
            tmpSName = prefix + "T" + nNode + (nCard + bitNum);
        } else {
            bitNum = SerialAddress.getBitFromSystemName(tmpSName);
            nNode = SerialAddress.getNodeAddressFromSystemName(tmpSName);
            tmpSName = prefix + "T" + nNode + bitNum;
        }
        return (tmpSName);
    }

    int nCard = 0;
    int bitNum = 0;
    int nNode = 0;

    /**
     * A method that returns the next valid free turnout hardware address.
     */
    @Override
    public String getNextValidAddress(String curAddress, String prefix) throws JmriException {

        String tmpSName = "";
        try {
            tmpSName = createSystemName(curAddress, prefix);
        } catch (JmriException ex) {
            throw ex;
        }

        //If the hardware address passed does not already exist then this can
        //be considered the next valid address.
        Turnout t = getBySystemName(tmpSName);
        if (t == null) {
            return Integer.toString(nNode) + Integer.toString((nCard + bitNum));
            //return ""+nNode+(nCard+bitNum);
        }

        //The Number of Output Bits of the previous turnout will help determine the next
        //valid address.
        bitNum = bitNum + t.getNumberOutputBits();
        //Check to determine if the systemName is in use, return null if it is,
        //otherwise return the next valid address.
        tmpSName = prefix + "T" + nNode + (nCard + bitNum);
        t = getBySystemName(tmpSName);
        if (t != null) {
            for (int x = 1; x < 10; x++) {
                bitNum = bitNum + t.getNumberOutputBits();
                tmpSName = prefix + "T" + nNode + (nCard + bitNum);
                t = getBySystemName(tmpSName);
                if (t == null) {
                    return Integer.toString(nNode) + Integer.toString((nCard + bitNum));
                }
                //return ""+nNode+(nCard+bitNum);
            }
            return null;
        } else {
            return Integer.toString(nNode) + Integer.toString((nCard + bitNum));
        }
    }


    /**
     * Public method to validate system name format.
     *
     * @return 'true' if system name has a valid format,
     * else returns 'false'
     */
    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        return (SerialAddress.validSystemNameFormat(systemName, 'T'));
    }

    /**
     * Public method to normalize a system name.
     *
     * @return a normalized system name if system name has a valid format, else
     * returns ""
     */
    @Override
    public String normalizeSystemName(String systemName) {
        return (SerialAddress.normalizeSystemName(systemName));
    }

    /**
     * Provide a manager-specific tooltip for the Add new item beantable pane.
     */
    @Override
    public String getEntryToolTip() {
        String entryToolTip = Bundle.getMessage("AddOutputEntryToolTip");
        return entryToolTip;
    }

    private final static Logger log = LoggerFactory.getLogger(SerialTurnoutManager.class);

}



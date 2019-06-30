package jmri.jmrix.tmcc;

import jmri.JmriException;
import jmri.Turnout;
import jmri.managers.AbstractTurnoutManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement turnout manager for TMCC serial systems.
 * <p>
 * System names are "TTnnn", where T is the user configurable system prefix,
 * nnn is the turnout number without padding.
 *
 * @author	Bob Jacobsen Copyright (C) 2003, 2006
 */
public class SerialTurnoutManager extends AbstractTurnoutManager implements SerialListener {

    TmccSystemConnectionMemo _memo = null;
    private String prefix = "T"; // default
    private SerialTrafficController trafficController = null;

    public SerialTurnoutManager() {
        log.debug("TMCC TurnoutManager null");
    }

    public SerialTurnoutManager(TmccSystemConnectionMemo memo) {
        _memo = memo;
        prefix = memo.getSystemPrefix();
        // connect to the TrafficManager
        trafficController = memo.getTrafficController();
        // listen for turnout creation
        trafficController.addSerialListener(this);
        log.debug("TMCC TurnoutManager prefix={}", prefix);
    }

    @Override
    public String getSystemPrefix() {
        return prefix;
    }

    @Override
    public Turnout createNewTurnout(String systemName, String userName) {
        // validate the system name, and normalize it
        log.debug("Start createNewTurnout, prefix = {} memo ={}", prefix, _memo.getUserName());
        String sName = SerialAddress.normalizeSystemName(systemName, prefix);
        if (sName.equals("")) {
            // system name is not valid
            log.debug("System Name not valid");
            return null;
        }
        // does this turnout already exist?
        Turnout t = getBySystemName(sName);
        if (t != null) {
            log.debug("Turnout already exists");
            return null;
        }
        // check under alternate name - not supported on TMCC
        // create the turnout
        log.debug("new SerialTurnout with addr = {}", systemName.substring(prefix.length() + 1));
        int addr = Integer.parseInt(systemName.substring(prefix.length() + 1)); // this won't work for B-names
        t = new SerialTurnout(prefix, addr, _memo);
        t.setUserName(userName);

        // does system name correspond to configured hardware?
        if (!SerialAddress.validSystemNameConfig(sName, 'T', prefix)) {
            // system name does not correspond to configured hardware
            log.warn("Turnout '{}' refers to an undefined Serial Node.", sName);
        }
        return t;
    }

    /**
     * Listeners for messages from the command station.
     */
    @Override
    public void message(SerialMessage m) {
        log.debug("message received unexpectedly: {}", m.toString());
    }

    // Listen for status changes from TMCC system
    @Override
    public void reply(SerialReply r) {
        // There isn't anything meaningful coming back at this time.
        log.debug("reply received unexpectedly: {}", r.toString());
    }

    // Turnout address format is more than a simple number.
    @Override
    public boolean allowMultipleAdditions(String systemName) {
        return true;
    }

    /**
     * @return current instance of connection
     * @deprecated JMRI Since 4.4 instance() shouldn't be used, convert to JMRI multi-system support structure
     */
    @Deprecated
    static public SerialTurnoutManager instance() {
        log.warn("deprecated instance() call for TMCC SerialTurnoutManager");
        return null;
    }
    /**
     * @deprecated JMRI Since 4.4 instance() shouldn't be used, convert to JMRI multi-system support structure
     */
    @Deprecated
    static SerialTurnoutManager _instance = null;

    @Override
    public String createSystemName(String curAddress, String prefix) throws JmriException {
        String tmpSName;

        if (curAddress.contains(":")) {
            // Address format passed is in the form node:address (not used in TMCC)
            int seperator = curAddress.indexOf(":");
            try {
                nAddress = Integer.parseInt(curAddress.substring(0, seperator));
                bitNum = Integer.parseInt(curAddress.substring(seperator + 1));
            } catch (NumberFormatException ex) {
                throw new JmriException("Unable to convert " + curAddress + " to a valid Hardware Address");
            }
            tmpSName = SerialAddress.makeSystemName("T", nAddress, bitNum, prefix); // T for Turnout
        } else {
            tmpSName = prefix + "T" + curAddress;
            try {
                bitNum = SerialAddress.getBitFromSystemName(tmpSName, prefix);
                nAddress = SerialAddress.getNodeAddressFromSystemName(tmpSName, prefix);
            } catch (NumberFormatException ex) {
                throw new JmriException("Unable to convert " + curAddress + " to a valid Hardware Address");
            }
        }
        return (tmpSName);
    }

    int bitNum = 0;
    int nAddress = 0;

    @Override
    public String getNextValidAddress(String curAddress, String prefix) {

        String tmpSName = "";
        try {
            tmpSName = createSystemName(curAddress, prefix);
        } catch (JmriException ex) {
            log.error("Unable to convert {} Hardware Address to a number", curAddress);
            jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                    showErrorMessage(Bundle.getMessage("ErrorTitle"),
                            Bundle.getMessage("ErrorConvertNumberX", curAddress), "" + ex, "", true, false);
            return null;
        }

        // If the hardware address passed does not already exist then this can
        // be considered the next valid address.
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
        tmpSName = SerialAddress.makeSystemName("T", nAddress, bitNum, prefix);
        t = getBySystemName(tmpSName);
        if (t != null) {
            for (int x = 1; x < 10; x++) {
                bitNum = bitNum + t.getNumberOutputBits();
                //This should increment " + bitNum
                tmpSName = SerialAddress.makeSystemName("T", nAddress, bitNum, prefix);
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

    /**
     * Public method to validate system name format.
     *
     * @return 'true' if system name has a valid format, else return 'false'
     */
    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        return (SerialAddress.validSystemNameFormat(systemName, 'T', prefix));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEntryToolTip() {
        return Bundle.getMessage("AddOutputEntryToolTip");
    }

    private final static Logger log = LoggerFactory.getLogger(SerialTurnoutManager.class);

}

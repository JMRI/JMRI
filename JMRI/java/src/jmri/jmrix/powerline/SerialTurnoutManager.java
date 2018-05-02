package jmri.jmrix.powerline;

import jmri.Turnout;
import jmri.managers.AbstractTurnoutManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement turnout manager for powerline systems
 * <P>
 * System names are "PTnnn", where nnn is the turnout number without padding.
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2006, 2007, 2008 Converted to
 * multiple connection
 * @author Ken Cameron Copyright (C) 2011
  */
public class SerialTurnoutManager extends AbstractTurnoutManager {

    SerialTrafficController tc = null;

    public SerialTurnoutManager(SerialTrafficController tc) {
        super();
        this.tc = tc;
    }

    @Override
    public String getSystemPrefix() {
        return tc.getAdapterMemo().getSystemPrefix();
    }

    @Override
    public boolean allowMultipleAdditions(String systemName) {
        return false;
    }

    @Override
    public String getNextValidAddress(String curAddress, String prefix) {

        //If the hardware address passed does not already exist then this can
        //be considered the next valid address.
        Turnout s = getBySystemName(prefix + typeLetter() + curAddress);
        if (s == null) {
            return curAddress;
        }

        // This bit deals with handling the curAddress, and how to get the next address.
        int iName = 0;
        // Address starts with a single letter called a House Code.
        String houseCode = curAddress.substring(0, 1);
        try {
            iName = Integer.parseInt(curAddress.substring(1));
        } catch (NumberFormatException ex) {
            log.error("Unable to convert {} Hardware Address to a number", curAddress);
            jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                    showErrorMessage(Bundle.getMessage("ErrorTitle"),
                            Bundle.getMessage("ErrorConvertNumberX", curAddress), "" + ex, "", true, false);
            return null;
        }

        // Check to determine if the systemName is in use, return null if it is,
        // otherwise return the next valid address.
        s = getBySystemName(prefix + typeLetter() + curAddress);
        if (s != null) {
            for (int x = 1; x < 10; x++) {
                iName++;
                s = getBySystemName(prefix + typeLetter() + houseCode + (iName));
                if (s == null) {
                    return houseCode + iName;
                }
            }
            return null;
        } else {
            return houseCode + iName;
        }
    }

    @Override
    public Turnout createNewTurnout(String systemName, String userName) {
        // validate the system name, and normalize it
        String sName = tc.getAdapterMemo().getSerialAddress().normalizeSystemName(systemName);
        if (sName.equals("")) {
            // system name is not valid
            return null;
        }
        // does this turnout already exist
        Turnout t = getBySystemName(sName);
        if (t != null) {
            return null;
        }

        // create the turnout
        t = new SerialTurnout(sName, tc, userName);

        // does system name correspond to configured hardware
        if (!tc.getAdapterMemo().getSerialAddress().validSystemNameConfig(sName, 'T')) {
            // system name does not correspond to configured hardware
            log.warn("Turnout '{}' refers to an undefined Serial Node.", sName);
        }
        return t;
    }

    /**
     * Public method to validate system name format
     *
     * @return 'true' if system name has a valid format, else returns 'false'
     */
    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        return (tc.getAdapterMemo().getSerialAddress().validSystemNameFormat(systemName, 'T'));
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



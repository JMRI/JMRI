package jmri.jmrix.secsi;

import jmri.Turnout;
import jmri.managers.AbstractTurnoutManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement turnout manager for SECSI systems
 * <P>
 * System names are "VTnnn", where nnn is the turnout number without padding.
 *
 * @author	Bob Jacobsen Copyright (C) 2003, 2006, 2007
  */
public class SerialTurnoutManager extends AbstractTurnoutManager {

    private SecsiSystemConnectionMemo memo = null;

    public SerialTurnoutManager(SecsiSystemConnectionMemo _memo) {
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
        if (!SerialAddress.validSystemNameConfig(sName, 'T', memo.getTrafficController())) {
            // system name does not correspond to configured hardware
            log.warn("Turnout '" + sName + "' refers to an undefined Serial Node.");
        }
        return t;
    }

    /**
     * Public method to validate system name format.
     * @return 'true' if system name has a valid format, else returns 'false'
     */
    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        return (SerialAddress.validSystemNameFormat(systemName, 'T'));
    }

    @Deprecated
    static public SerialTurnoutManager instance() {
        return null;
    }

    private final static Logger log = LoggerFactory.getLogger(SerialTurnoutManager.class);

}

package jmri.jmrix.easydcc;

import jmri.Turnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement turnout manager for EasyDcc systems.
 * <p>
 * System names are "ETnnn", where nnn is the turnout number without padding.
 *
 * @author Bob Jacobsen Copyright (C) 2001
  */
public class EasyDccTurnoutManager extends jmri.managers.AbstractTurnoutManager {

    EasyDccSystemConnectionMemo _memo = null;
    private String prefix = "E";
    public final static int MAX_ACC_DECODER_ADDRESS = 511;

    public EasyDccTurnoutManager() {

    }

    public EasyDccTurnoutManager(EasyDccSystemConnectionMemo memo) {
        _memo = memo;
        prefix = memo.getSystemPrefix();
    }

    @Override
    public String getSystemPrefix() {
        return prefix;
    }

    @Override
    public Turnout createNewTurnout(String systemName, String userName) {
        Turnout t;
        int addr = Integer.valueOf(systemName.substring(prefix.length() + 1)).intValue();
        t = new EasyDccTurnout(addr, getSystemPrefix());
        t.setUserName(userName);

        return t;
    }

    /**
     * Get the bit address from the system name.
     */
    public int getBitFromSystemName(String systemName) {
        // validate the system Name leader characters
        if ((!systemName.startsWith(prefix + typeLetter()))) {
            // here if an illegal EasyDCC turnout system name
            log.error("illegal character in header field of EasyDCC turnout system name: {} prefix {} type {}",
                    systemName, getSystemPrefix(), typeLetter());
            return (0);
        }
        // name must be in the ETnnnnn format (E is user configurable)
        int num = 0;
        try {
            num = Integer.valueOf(systemName.substring(
                    getSystemPrefix().length() + 1, systemName.length())).intValue();
        } catch (Exception e) {
            log.debug("illegal character in number field of system name: {}", systemName);
            return (0);
        }
        if (num <= 0) {
            log.debug("invalid EasyDCC turnout system name: {}", systemName);
            return (0);
        } else if (num > MAX_ACC_DECODER_ADDRESS) {
            log.debug("bit number out of range in EasyDCC turnout system name: {}", systemName);
            return (0);
        }
        return (num);
    }

    /**
     * Public method to validate system name format.
     *
     * @return VALID if system name has a valid format, else return INVALID
     */
    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        return (getBitFromSystemName(systemName) != 0) ? NameValidity.VALID : NameValidity.INVALID;
    }

    /**
     * Provide a manager-specific tooltip for the Add new item beantable pane.
     */
    @Override
    public String getEntryToolTip() {
        String entryToolTip = Bundle.getMessage("AddOutputEntryToolTip");
        return entryToolTip;
    }

    static public EasyDccTurnoutManager instance() {
        if (_instance == null) {
            _instance = new EasyDccTurnoutManager();
        }
        return _instance;
    }
    static EasyDccTurnoutManager _instance = null;

    private final static Logger log = LoggerFactory.getLogger(EasyDccTurnoutManager.class);

}

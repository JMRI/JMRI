package jmri.jmrix.sprog;

import jmri.Turnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement turnout manager for Sprog systems.
 * <p>
 * System names are "STnnn", where S is the user configurable system prefix,
 * nnn is the turnout number without padding.
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 */
public class SprogTurnoutManager extends jmri.managers.AbstractTurnoutManager {

    SprogSystemConnectionMemo _memo = null;

    public SprogTurnoutManager(SprogSystemConnectionMemo memo) {
        _memo = memo;
    }

    @Override
    public String getSystemPrefix() {
        return _memo.getSystemPrefix();
    }

    // Sprog-specific methods

    @Override
    public Turnout createNewTurnout(String systemName, String userName) {
        int addr = Integer.parseInt(systemName.substring(getSystemPrefix().length() + 1)); // multi char prefix
        Turnout t;
        if (_memo.getSprogMode() == SprogConstants.SprogMode.OPS ) {
            t = new SprogCSTurnout(addr, _memo);
        } else {
            t = new SprogTurnout(addr, _memo);
        }
        t.setUserName(userName);
        return t;
    }

    /**
     * Get the bit address from the system name.
     */
    public int getBitFromSystemName(String systemName) {
        // validate the System Name leader characters
        if (!systemName.startsWith(getSystemPrefix() + "T")) {
            // here if an illegal sprog turnout system name
            log.error("illegal character in header field of sprog turnout system name: {}", systemName);
            return (0);
        }
        // name must be in the STnnnnn format (S is user configurable)
        int num = 0;
        try {
            num = Integer.parseInt(systemName.substring(getSystemPrefix().length() + 1));
        } catch (Exception e) {
            log.debug("invalid character in number field of system name: {}", systemName);
            return (0);
        }
        if (num <= 0) {
            log.debug("invalid sprog turnout system name: {}", systemName);
            return (0);
        } else if (num > SprogConstants.MAX_ACC_DECODER_JMRI_ADDR) { // undocumented for SPROG, higher causes error in NMRA Acc Packet
            log.debug("bit number out of range in sprog turnout system name: {}", systemName);
            return (0);
        }
        return (num);
    }

    /**
     * Public method to validate system name format.
     *
     * @return 'true' if system name has a valid format, else returns 'false'
     */
    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        return (getBitFromSystemName(systemName) != 0) ? NameValidity.VALID : NameValidity.INVALID;
    }

    @Override
    public boolean allowMultipleAdditions(String systemName) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEntryToolTip() {
        return Bundle.getMessage("AddOutputEntryToolTip");
    }

    /**
     * @deprecated JMRI Since 4.4 instance() shouldn't be used; convert to JMRI multi-system support structure
     */
    @Deprecated
    static public SprogTurnoutManager instance() {
        return null;
    }

    private final static Logger log = LoggerFactory.getLogger(SprogTurnoutManager.class);

}

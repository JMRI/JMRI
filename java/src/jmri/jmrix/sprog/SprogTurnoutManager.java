package jmri.jmrix.sprog;

import java.util.Locale;
import jmri.Turnout;

/**
 * Implement turnout manager for Sprog systems.
 * <p>
 * System names are "STnnn", where S is the user configurable system prefix,
 * nnn is the turnout number without padding.
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 */
public class SprogTurnoutManager extends jmri.managers.AbstractTurnoutManager {

    public SprogTurnoutManager(SprogSystemConnectionMemo memo) {
        super(memo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SprogSystemConnectionMemo getMemo() {
        return (SprogSystemConnectionMemo) memo;
    }

    // Sprog-specific methods

    @Override
    public Turnout createNewTurnout(String systemName, String userName) {
        int addr = Integer.parseInt(systemName.substring(getSystemPrefix().length() + 1)); // multi char prefix
        Turnout t;
        if (getMemo().getSprogMode() == SprogConstants.SprogMode.OPS ) {
            t = new SprogCSTurnout(addr, getMemo());
        } else {
            t = new SprogTurnout(addr, getMemo());
        }
        t.setUserName(userName);
        return t;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        return (getBitFromSystemName(systemName) != 0) ? NameValidity.VALID : NameValidity.INVALID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String validateSystemNameFormat(String systemName, Locale locale) {
        return validateIntegerSystemNameFormat(systemName, 1, SprogConstants.MAX_ACC_DECODER_JMRI_ADDR, locale);
    }

    /**
     * Get the bit address from the system name.
     * @param systemName a valid LocoNet-based Turnout System Name
     * @return the turnout number extracted from the system name
     */
    public int getBitFromSystemName(String systemName) {
        try {
            validateSystemNameFormat(systemName, Locale.getDefault());
        } catch (IllegalArgumentException ex) {
            return 0;
        }
        return Integer.parseInt(systemName.substring(getSystemNamePrefix().length()));
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

}

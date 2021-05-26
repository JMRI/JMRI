package jmri.jmrix.pi;

import javax.annotation.Nonnull;
import jmri.Turnout;

import jmri.JmriException;

/**
 * Implement Pi turnout manager.
 * <p>
 * System names are "PTnnn", where P is the user configurable system prefix,
 * nnn is the turnout number without padding.
 *
 * @author   Paul Bender Copyright (C) 2015
 */
public class RaspberryPiTurnoutManager extends jmri.managers.AbstractTurnoutManager {

    // ctor has to register for RaspberryPi events
    public RaspberryPiTurnoutManager(RaspberryPiSystemConnectionMemo memo) {
        super(memo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public RaspberryPiSystemConnectionMemo getMemo() {
        return (RaspberryPiSystemConnectionMemo) memo;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    protected Turnout createNewTurnout(@Nonnull String systemName, String userName) throws IllegalArgumentException {
        return new RaspberryPiTurnout(systemName, userName);
    }
    
    /**
     * Require address portion of SystemName to either start with ":" or be numberic.
     * {@inheritDoc} 
     */
    @Nonnull
    @Override
    public String createSystemName(@Nonnull String curAddress, @Nonnull String prefix) throws JmriException {
        if (curAddress.substring(0,1).equals (":")) {
            return prefix + typeLetter() + curAddress;
        } else {
            return prefix + typeLetter() + checkNumeric(curAddress);
        }
    }
    
    /**
     * Validates to either ":xxx..." (:MCP23017:xxx) or Integer Format 0-999 with valid prefix.
     * eg. PT0 to PT999, PT:MCP23017:1:32:0
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public String validateSystemNameFormat(@Nonnull String name, @Nonnull java.util.Locale locale) throws jmri.NamedBean.BadSystemNameException, IllegalArgumentException {
        if (name.length() < 3) {
            throw new IllegalArgumentException();
        }
        if (name.substring (2,3).equals (":")) {
            return name;
        } else {
            return this.validateIntegerSystemNameFormat(name, 0, 999, locale);
        }
    }
    
    /**
     * Use an updated tool tip to account for extended pins.
     *  {@inheritDoc}
     */
    @Override
    public String getEntryToolTip() {
        return Bundle.getMessage("AddOutputEntryToolTip");
    }
}

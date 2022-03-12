package jmri.jmrix.powerline;

import java.util.Locale;
import javax.annotation.Nonnull;

import jmri.JmriException;
import jmri.Turnout;
import jmri.managers.AbstractTurnoutManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement turnout manager for Powerline systems.
 * <p>
 * System names are "PTnnn", where P is the user configurable system prefix,
 * nnn is the turnout number without padding.
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2006, 2007, 2008 Converted to
 * multiple connection
 * @author Ken Cameron Copyright (C) 2011
 */
public class SerialTurnoutManager extends AbstractTurnoutManager {

    private SerialTrafficController tc = null;

    public SerialTurnoutManager(SerialTrafficController tc) {
        super(tc.getAdapterMemo());
        this.tc = tc;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public SerialSystemConnectionMemo getMemo() {
        return (SerialSystemConnectionMemo) memo;
    }

    @Override
    public boolean allowMultipleAdditions(@Nonnull String systemName) {
        return false;
    }

    /**
     * TODO : Get this method working then enable multiple additions
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public String getNextValidAddress(@Nonnull String curAddress, @Nonnull String prefix, boolean ignoreInitialExisting) throws JmriException {

        //If the hardware address passed does not already exist then this can
        //be considered the next valid address.
        Turnout s = getBySystemName(prefix + typeLetter() + curAddress);
        if (s == null && !ignoreInitialExisting) {
            return curAddress;
        }

        // This bit deals with handling the curAddress, and how to get the next address.
        int iName = 0;
        // Address starts with a single letter called a House Code.
        String houseCode = curAddress.substring(0, 1);
        try {
            iName = Integer.parseInt(curAddress.substring(1));
        } catch (NumberFormatException ex) {
            throw new JmriException(Bundle.getMessage("ErrorConvertNumberX",curAddress));
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
            throw new JmriException(Bundle.getMessage("InvalidNextValidTenInUse",getBeanTypeHandled(true),curAddress,houseCode + (iName)));
        } else {
            return houseCode + iName;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    protected Turnout createNewTurnout(@Nonnull String systemName, String userName) throws IllegalArgumentException {
        // validate the system name, and normalize it
        String sName = tc.getAdapterMemo().getSerialAddress().normalizeSystemName(systemName);
        if (sName.isEmpty()) {
            // system name is not valid
            throw new IllegalArgumentException("Cannot create Turnout System Name from " + systemName);
        }
        // does this turnout already exist
        Turnout t = getBySystemName(sName);
        if (t != null) {
            return t;
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
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public String validateSystemNameFormat(@Nonnull String name, @Nonnull Locale locale) {
        return tc.getAdapterMemo().getSerialAddress().validateSystemNameFormat(name, typeLetter(), locale);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NameValidity validSystemNameFormat(@Nonnull String systemName) {
        return tc.getAdapterMemo().getSerialAddress().validSystemNameFormat(systemName, typeLetter());
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

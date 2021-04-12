package jmri.jmrix.oaktree;

import java.util.Locale;
import javax.annotation.Nonnull;
import jmri.Turnout;
import jmri.managers.AbstractTurnoutManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement turnout manager for Oak Tree systems.
 * <p>
 * System names are "OTnnn", where O is the user configurable system prefix,
 * nnn is the turnout number without padding.
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2006
 */
public class SerialTurnoutManager extends AbstractTurnoutManager {

    public SerialTurnoutManager(OakTreeSystemConnectionMemo memo) {
        super(memo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public OakTreeSystemConnectionMemo getMemo() {
        return (OakTreeSystemConnectionMemo) memo;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    protected Turnout createNewTurnout(@Nonnull String systemName, String userName) throws IllegalArgumentException {
        // validate the system name, and normalize it
        String sName = SerialAddress.normalizeSystemName(systemName, getSystemPrefix());
        if (sName.isEmpty()) {
            // system name is not valid
            throw new IllegalArgumentException("Cannot create System Name from " + systemName);
        }
        // does this turnout already exist
        Turnout t = getBySystemName(sName);
        if (t != null) {
            return t;
        }
        // check under alternate name
        String altName = SerialAddress.convertSystemNameToAlternate(sName, getSystemPrefix());
        t = getBySystemName(altName);
        if (t != null) {
            return t;
        }
        // create the turnout
        t = new SerialTurnout(sName, userName, getMemo());

        // does system name correspond to configured hardware
        if (!SerialAddress.validSystemNameConfig(sName, 'T', getMemo())) {
            // system name does not correspond to configured hardware
            log.warn("Turnout '{}' refers to an undefined Serial Node", sName);
        }
        return t;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public String validateSystemNameFormat(@Nonnull String systemName, @Nonnull Locale locale) {
        return SerialAddress.validateSystemNameFormat(systemName, getSystemNamePrefix(), locale);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NameValidity validSystemNameFormat(@Nonnull String systemName) {
        return (SerialAddress.validSystemNameFormat(systemName, typeLetter(), getSystemPrefix()));
    }

    @Override
    public boolean allowMultipleAdditions(@Nonnull String systemName) {
        return true;
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

package jmri.jmrix.pi;

import javax.annotation.Nonnull;
import jmri.Turnout;

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
        Turnout t = new RaspberryPiTurnout(systemName, userName);
        return t;
    }
    
    /**
     * Validates to Integer Format 0-999 with valid prefix.
     * eg. PT0 to PT999
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public String validateSystemNameFormat(@Nonnull String name, @Nonnull java.util.Locale locale) throws jmri.NamedBean.BadSystemNameException {
        return this.validateIntegerSystemNameFormat(name, 0, 999, locale);
    }

}

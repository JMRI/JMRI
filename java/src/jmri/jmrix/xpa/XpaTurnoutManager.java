package jmri.jmrix.xpa;

import javax.annotation.Nonnull;
import jmri.Turnout;

/**
 * Implement turnout manager for Xpa+Modem connections to XpressNet Based
 * systems.
 * <p>
 * System names are "PTnnn", where P is the user configurable system prefix,
 * nnn is the turnout number without padding.
 *
 * @author Paul Bender Copyright (C) 2004,2016
 */
public class XpaTurnoutManager extends jmri.managers.AbstractTurnoutManager {

    public XpaTurnoutManager(XpaSystemConnectionMemo memo) {
         super(memo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public XpaSystemConnectionMemo getMemo() {
        return (XpaSystemConnectionMemo) memo;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    protected Turnout createNewTurnout(@Nonnull String systemName, String userName) throws IllegalArgumentException {
        int addr;
        try {
            addr = Integer.parseInt(systemName.substring(getSystemPrefix().length() + 1));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Failed to convert systemName '"+systemName+"' to a Turnout address");
        }
        Turnout t = new XpaTurnout(addr, getMemo());
        t.setUserName(userName);
        return t;
    }

    @Override
    public boolean allowMultipleAdditions(@Nonnull String systemName) {
        return true;
    }
    
    /**
     * Validates to only numeric.
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public String validateSystemNameFormat(@Nonnull String name, @Nonnull java.util.Locale locale) throws jmri.NamedBean.BadSystemNameException {
        return validateSystemNameFormatOnlyNumeric(name,locale);
    }

}

package jmri.jmrix.jmriclient;

import javax.annotation.Nonnull;
import jmri.Turnout;

/**
 * Implement turnout manager for JMRIClient systems
 * <p>
 * System names are "prefixnnn", where prefix is the system prefix and nnn is
 * the turnout number without padding.
 *
 * @author Paul Bender Copyright (C) 2010
 */
public class JMRIClientTurnoutManager extends jmri.managers.AbstractTurnoutManager {

    public JMRIClientTurnoutManager(JMRIClientSystemConnectionMemo memo) {
        super(memo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public JMRIClientSystemConnectionMemo getMemo() {
        return (JMRIClientSystemConnectionMemo) memo;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    protected Turnout createNewTurnout(@Nonnull String systemName, String userName) throws IllegalArgumentException {
        int addr;
        try {
            addr = Integer.parseInt(systemName.substring(getSystemNamePrefix().length()));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Failed to convert systemName '"+systemName+"' to a Turnout.");
        }
        Turnout t = new JMRIClientTurnout(addr, getMemo());
        t.setUserName(userName);
        return t;
    }

    /*
     * JMRIClient Turnouts can take arbitrary names to match the names used
     * on the server.
     */
    @Override
    public String createSystemName(@Nonnull String curAddress, @Nonnull String prefix) throws jmri.JmriException {
        return prefix + typeLetter() + curAddress;
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

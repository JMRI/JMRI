package jmri.jmrix.marklin;

import javax.annotation.Nonnull;
import jmri.Turnout;

/**
 * Implement turnout manager for Marklin systems.
 * <p>
 * Based on work by Bob Jacobsen
 *
 * @author Kevin Dickerson Copyright (C) 2012
 */
public class MarklinTurnoutManager extends jmri.managers.AbstractTurnoutManager {

    public MarklinTurnoutManager(MarklinSystemConnectionMemo memo) {
        super(memo);
        tc = memo.getTrafficController();
    }

    private final MarklinTrafficController tc;

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public MarklinSystemConnectionMemo getMemo() {
        return (MarklinSystemConnectionMemo) memo;
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
            log.error("Failed to convert systemName {} to a turnout address", systemName);
            throw new IllegalArgumentException("failed to convert systemName '"+systemName+"' to a Turnout address");
        }
        Turnout t = new MarklinTurnout(addr, getSystemPrefix(), tc);
        t.setUserName(userName);
        return t;
    }

    @Override
    public boolean allowMultipleAdditions(@Nonnull String systemName) {
        return true;
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MarklinTurnoutManager.class);

}

package jmri.jmrix.marklin;

import javax.annotation.Nonnull;
import jmri.Turnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    MarklinTrafficController tc;

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public MarklinSystemConnectionMemo getMemo() {
        return (MarklinSystemConnectionMemo) memo;
    }

    @Override
    public Turnout createNewTurnout(@Nonnull String systemName, String userName) {
        int addr;
        try {
            addr = Integer.parseInt(systemName.substring(getSystemPrefix().length() + 1));
        } catch (java.lang.NumberFormatException e) {
            log.error("failed to convert systemName {} to a turnout address", systemName);
            return null;
        }
        Turnout t = new MarklinTurnout(addr, getSystemPrefix(), tc);
        t.setUserName(userName);
        return t;
    }

    @Override
    public boolean allowMultipleAdditions(@Nonnull String systemName) {
        return true;
    }

    boolean noWarnDelete = false;

    private final static Logger log = LoggerFactory.getLogger(MarklinTurnoutManager.class);

}

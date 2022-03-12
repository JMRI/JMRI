package jmri.jmrix.mrc;

import javax.annotation.Nonnull;
import jmri.Turnout;

/**
 * New MRC TurnoutManager
 * <p>
 * System names are "PTnnn", where P is the user configurable system prefix,
 * nnn is the turnout number without padding.
 *
 * @author Paul Bender Copyright (C) 2004
 * @author Martin Wade Copyright (C) 2014
 */
public class MrcTurnoutManager extends jmri.managers.AbstractTurnoutManager {

    public MrcTurnoutManager(MrcSystemConnectionMemo memo) {
        super(memo);
        this.tc = memo.getMrcTrafficController();
    }

    MrcTrafficController tc = null;

    @Override
    @Nonnull
    public MrcSystemConnectionMemo getMemo() {
        return (MrcSystemConnectionMemo) memo;
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
        Turnout t = new MrcTurnout(addr, tc, getSystemPrefix());
        t.setUserName(userName);
        return t;
    }

    @Override
    public boolean allowMultipleAdditions(@Nonnull String systemName) {
        return true;
    }

}

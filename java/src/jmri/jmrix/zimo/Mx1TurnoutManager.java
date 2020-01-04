package jmri.jmrix.zimo;

import javax.annotation.Nonnull;
import jmri.Turnout;

/**
 * Implement turnout manager for Mx1 Turnouts.
 * <p>
 * System names are "ZTnnn", where Z is the user configurable system prefix,
 * nnn is the turnout number without padding.
 *
 * @author	Kevin Dickerson (C) 2014
 */
public class Mx1TurnoutManager extends jmri.managers.AbstractTurnoutManager {

    public Mx1TurnoutManager(Mx1SystemConnectionMemo memo) {
        super(memo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public Mx1SystemConnectionMemo getMemo() {
        return (Mx1SystemConnectionMemo) memo;
    }

    @Override
    public Turnout createNewTurnout(@Nonnull String systemName, String userName) {
        int addr = Integer.parseInt(systemName.substring(getSystemPrefix().length() + 1));
        Turnout t = new Mx1Turnout(addr, getMemo().getMx1TrafficController(), getSystemPrefix());
        t.setUserName(userName);
        return t;
    }

    @Override
    public boolean allowMultipleAdditions(@Nonnull String systemName) {
        return true;
    }

}

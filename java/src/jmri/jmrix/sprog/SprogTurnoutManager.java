package jmri.jmrix.sprog;

import jmri.Turnout;

/**
 * Implement turnout manager for Sprog systems.
 * <P>
 * System names are "STnnn", where nnn is the turnout number without padding.
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 * @version	$Revision$
 */
public class SprogTurnoutManager extends jmri.managers.AbstractTurnoutManager {

    SprogSystemConnectionMemo _memo = null;
    public SprogTurnoutManager(SprogSystemConnectionMemo memo) {
        _memo = memo;
    }

    public String getSystemPrefix() {
        return _memo.getSystemPrefix();
    }

    // Sprog-specific methods
    public Turnout createNewTurnout(String systemName, String userName) {
        int addr = Integer.valueOf(systemName.substring(2)).intValue();
        Turnout t;
        if (jmri.jmrix.sprog.ActiveFlagCS.isActive()) {
            t = new SprogCSTurnout(addr,_memo);
        } else {
            t = new SprogTurnout(addr,_memo);
        }
        t.setUserName(userName);
        return t;
    }

    /**
     * @deprecated JMRI Since 4.4 instance() shouldn't be used, convert to JMRI multi-system support structure
     */
    @Deprecated
    static public SprogTurnoutManager instance() {
        return null;
    }

}

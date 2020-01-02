package jmri.jmrix.xpa;

import jmri.Turnout;

/**
 * Implement turnout manager for Xpa+Modem connections to XpressNet Based
 * systems.
 * <p>
 * System names are "PTnnn", where P is the user configurable system prefix,
 * nnn is the turnout number without padding.
 *
 * @author	Paul Bender Copyright (C) 2004,2016
 */
public class XpaTurnoutManager extends jmri.managers.AbstractTurnoutManager {

    public XpaTurnoutManager(XpaSystemConnectionMemo memo) {
         super(memo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XpaSystemConnectionMemo getMemo() {
        return (XpaSystemConnectionMemo) memo;
    }

    // Xpa-specific methods
    @Override
    public Turnout createNewTurnout(String systemName, String userName) {
        int addr = Integer.parseInt(systemName.substring(getSystemPrefix().length() + 1));
        Turnout t = new XpaTurnout(addr, getMemo());
        t.setUserName(userName);
        return t;
    }

    @Override
    public boolean allowMultipleAdditions(String systemName) {
        return true;
    }

}

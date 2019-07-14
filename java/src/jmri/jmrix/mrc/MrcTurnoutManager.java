package jmri.jmrix.mrc;

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

    public MrcTurnoutManager(MrcTrafficController tc, String prefix) {
        super();
        this.prefix = prefix;
        this.tc = tc;
    }

    String prefix = "";
    MrcTrafficController tc = null;

    @Override
    public String getSystemPrefix() {
        return prefix;
    }

    @Override
    public Turnout createNewTurnout(String systemName, String userName) {
        int addr = Integer.parseInt(systemName.substring(getSystemPrefix().length() + 1));
        Turnout t = new MrcTurnout(addr, tc, getSystemPrefix());
        t.setUserName(userName);
        return t;
    }

    @Override
    public boolean allowMultipleAdditions(String systemName) {
        return true;
    }

}

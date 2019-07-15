package jmri.jmrix.dcc;

import jmri.Turnout;

/**
 * Implement turnout manager for DCC-only systems.
 * <p>
 * System names are "BTnnn", where B is the user configurable system prefix,
 * nnn is the turnout number without padding.
 *
 * @author Bob Jacobsen Copyright (C) 2014
 */
public class DccTurnoutManager extends jmri.managers.AbstractTurnoutManager {

    public DccTurnoutManager() {
    }

    @Override
    public String getSystemPrefix() {
        return "B";
    }

    @Override
    public Turnout createNewTurnout(String systemName, String userName) {
        Turnout t;
        int addr = Integer.parseInt(systemName.substring(2)); // fixed length prefix, so (2) is OK here
        t = new DccTurnout(addr);
        t.setUserName(userName);

        return t;
    }

}

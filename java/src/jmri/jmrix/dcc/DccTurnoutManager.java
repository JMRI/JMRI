// EasyDccTurnoutManager.java
package jmri.jmrix.dcc;

import jmri.Turnout;

/**
 * Implement turnout manager for DCC-only systems
 * <P>
 * System names are "BTnnn", where nnn is the turnout number without padding.
 *
 * @author	Bob Jacobsen Copyright (C) 2014
 * @version	$Revision$
 */
public class DccTurnoutManager extends jmri.managers.AbstractTurnoutManager {

    public DccTurnoutManager() {
    }

    public String getSystemPrefix() {
        return "B";
    }

    public Turnout createNewTurnout(String systemName, String userName) {
        Turnout t;
        int addr = Integer.valueOf(systemName.substring(2)).intValue();
        t = new DccTurnout(addr);
        t.setUserName(userName);

        return t;
    }

    static public DccTurnoutManager instance() {
        if (_instance == null) {
            _instance = new DccTurnoutManager();
        }
        return _instance;
    }
    static DccTurnoutManager _instance = null;

}

/* @(#)DccTurnoutManager.java */

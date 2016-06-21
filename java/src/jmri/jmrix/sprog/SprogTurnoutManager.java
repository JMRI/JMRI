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

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
    // Ignore FindBugs warnings as there can only be one instance at present
    public SprogTurnoutManager() {
        _instance = this;
    }

    public String getSystemPrefix() {
        return "S";
    }

    // Sprog-specific methods
    public Turnout createNewTurnout(String systemName, String userName) {
        int addr = Integer.valueOf(systemName.substring(2)).intValue();
        Turnout t;
        if (jmri.jmrix.sprog.ActiveFlagCS.isActive()) {
            t = new SprogCSTurnout(addr);
        } else {
            t = new SprogTurnout(addr);
        }
        t.setUserName(userName);
        return t;
    }

    static public SprogTurnoutManager instance() {
        if (_instance == null) {
            _instance = new SprogTurnoutManager();
        }
        return _instance;
    }
    static SprogTurnoutManager _instance = null;

}

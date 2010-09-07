// NceTurnoutManager.java

package jmri.jmrix.nce;

import jmri.Turnout;

/**
 * Implement turnout manager for NCE systems.
 * <P>
 * System names are "NTnnn", where nnn is the turnout number without padding.
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 * @version	$Revision: 1.15 $
 */
public class NceTurnoutManager extends jmri.managers.AbstractTurnoutManager {

    public NceTurnoutManager() {
    }

    public String getSystemPrefix() { return "N"; }

    public Turnout createNewTurnout(String systemName, String userName) {
        int addr = Integer.valueOf(systemName.substring(2)).intValue();
        Turnout t = new NceTurnout(addr);
        t.setUserName(userName);

        return t;
    }

    static public NceTurnoutManager instance() {
        if (_instance == null) _instance = new NceTurnoutManager();
        return _instance;
    }
    static NceTurnoutManager _instance = null;

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(NceTurnoutManager.class.getName());
}

/* @(#)NceTurnoutManager.java */

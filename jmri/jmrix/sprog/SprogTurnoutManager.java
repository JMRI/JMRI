// SprogTurnoutManager.java

package jmri.jmrix.sprog;

import jmri.JmriException;
import jmri.Turnout;

/**
 * Implement turnout manager for Sprog systems
 * <P>
 * System names are "STnnn", where nnn is the turnout number without padding.
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 * @version	$Revision: 1.6 $
 */
public class SprogTurnoutManager extends jmri.AbstractTurnoutManager {

    public SprogTurnoutManager() {
        _instance = this;
    }

    public char systemLetter() { return 'S'; }

    // Sprog-specific methods

    public Turnout createNewTurnout(String systemName, String userName) {
        int addr = Integer.valueOf(systemName.substring(2)).intValue();
        Turnout t = new SprogTurnout(addr);
        t.setUserName(userName);
        return t;
    }

    static public SprogTurnoutManager instance() {
        if (_instance == null) _instance = new SprogTurnoutManager();
        return _instance;
    }
    static SprogTurnoutManager _instance = null;

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SprogTurnoutManager.class.getName());

}

/* @(#)SprogTurnoutManager.java */

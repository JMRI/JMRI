// EasyDccTurnoutManager.java

package jmri.jmrix.easydcc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.Turnout;

/**
 * Implement turnout manager for EasyDcc systems
 * <P>
 * System names are "ETnnn", where nnn is the turnout number without padding.
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 * @version	$Revision$
 */

public class EasyDccTurnoutManager extends jmri.managers.AbstractTurnoutManager {

    public EasyDccTurnoutManager() {

    }

    public String getSystemPrefix() { return "E"; }

    public Turnout createNewTurnout(String systemName, String userName) {
        Turnout t;
        int addr = Integer.valueOf(systemName.substring(2)).intValue();
        t = new EasyDccTurnout(addr);
        t.setUserName(userName);

        return t;
    }

    static public EasyDccTurnoutManager instance() {
        if (_instance == null) _instance = new EasyDccTurnoutManager();
        return _instance;
    }
    static EasyDccTurnoutManager _instance = null;

    static Logger log = LoggerFactory.getLogger(EasyDccTurnoutManager.class.getName());

}

/* @(#)EasyDccTurnoutManager.java */

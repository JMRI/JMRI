// XpaTurnoutManager.java

package jmri.jmrix.xpa;

import jmri.JmriException;
import jmri.Turnout;

/**
 * Implement turnout manager for Xpa+Modem connections to XPressNet Based 
 * systems.
 * <P>
 * System names are "PTnnn", where nnn is the turnout number without padding.
 *
 * @author	Paul Bender Copyright (C) 2004
 * @version	$Revision: 1.1 $
 */
public class XpaTurnoutManager extends jmri.AbstractTurnoutManager {

    public XpaTurnoutManager() {
        _instance = this;
    }

    public char systemLetter() { return 'P'; }

    // Xpa-specific methods

    public Turnout createNewTurnout(String systemName, String userName) {
        int addr = Integer.valueOf(systemName.substring(2)).intValue();
        Turnout t = new XpaTurnout(addr);
        t.setUserName(userName);
        return t;
    }

    static public XpaTurnoutManager instance() {
        if (_instance == null) _instance = new XpaTurnoutManager();
        return _instance;
    }
    static XpaTurnoutManager _instance = null;

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(XpaTurnoutManager.class.getName());

}

/* @(#)XpaTurnoutManager.java */

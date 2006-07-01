// NceTurnoutManager.java

package jmri.jmrix.nce;

import jmri.JmriException;
import jmri.Turnout;

/**
 * Implement turnout manager for NCE systems
 * <P>
 * System names are "NTnnn", where nnn is the turnout number without padding.
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 * @version	$Revision: 1.8 $
 */
public class NceTurnoutManager extends jmri.AbstractTurnoutManager {

    public NceTurnoutManager() {
        _instance = this;
    }

    public char systemLetter() { return 'N'; }

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

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NceTurnoutManager.class.getName());
}

/* @(#)NceTurnoutManager.java */

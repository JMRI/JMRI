// SerialTurnoutManager.java

package jmri.jmrix.cmri.serial;

import jmri.AbstractTurnoutManager;
import jmri.Turnout;

/**
 * Implement turnout manager for CMRI serial systems
 * <P>
 * System names are "CTnnn", where nnn is the turnout number without padding.
 *
 * @author	Bob Jacobsen Copyright (C) 2003
 * @version	$Revision: 1.5 $
 */
public class SerialTurnoutManager extends AbstractTurnoutManager {

    public SerialTurnoutManager() {
        _instance = this;
    }

    public char systemLetter() { return 'C'; }

    public Turnout createNewTurnout(String systemName, String userName) {
        Turnout t;
        int addr = Integer.valueOf(systemName.substring(2)).intValue();
        t = new SerialTurnout(addr);
        t.setUserName(userName);

        t.addPropertyChangeListener(this);

        return t;
    }

    static public SerialTurnoutManager instance() {
        if (_instance == null) _instance = new SerialTurnoutManager();
        return _instance;
    }
    static SerialTurnoutManager _instance = null;

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SerialTurnoutManager.class.getName());

}

/* @(#)SerialTurnoutManager.java */

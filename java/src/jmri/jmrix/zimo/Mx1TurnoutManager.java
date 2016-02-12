// MxTurnoutManager.java
package jmri.jmrix.zimo;

import jmri.Turnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * New Mx1 Turnout Manager
 * <P>
 * System names are "ZTnnn", where nnn is the turnout number without padding.
 *
 * @author	Kevin Dickerson (C) 2014
 * @version	$Revision: 22821 $
 */
public class Mx1TurnoutManager extends jmri.managers.AbstractTurnoutManager {

    public Mx1TurnoutManager(Mx1TrafficController tc, String prefix) {
        super();
        this.prefix = prefix;
        this.tc = tc;
    }

    String prefix = "";
    Mx1TrafficController tc = null;

    public String getSystemPrefix() {
        return prefix;
    }

    public Turnout createNewTurnout(String systemName, String userName) {
        int addr = Integer.valueOf(systemName.substring(getSystemPrefix().length() + 1)).intValue();
        Turnout t = new Mx1Turnout(addr, tc, getSystemPrefix());
        t.setUserName(userName);
        return t;
    }

    private final static Logger log = LoggerFactory.getLogger(Mx1TurnoutManager.class.getName());

}

/* @(#)Mx1TurnoutManager.java */

// TamsTurnoutManager.java
package jmri.jmrix.tams;

import jmri.Turnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement turnout manager for Tams systems.
 * <P>
 *
 * Based on work by Bob Jacobsen
 *
 * @author	Kevin Dickerson Copyright (C) 2012
 * @version	$Revision: 19646 $
 */
public class TamsTurnoutManager extends jmri.managers.AbstractTurnoutManager {

    public TamsTurnoutManager(TamsSystemConnectionMemo memo) {

        adaptermemo = memo;
        prefix = adaptermemo.getSystemPrefix();
        tc = adaptermemo.getTrafficController();
    }

    TamsTrafficController tc;
    TamsSystemConnectionMemo adaptermemo;

    String prefix;

    public String getSystemPrefix() {
        return prefix;
    }

    public Turnout createNewTurnout(String systemName, String userName) {
        int addr;
        try {
            addr = Integer.valueOf(systemName.substring(getSystemPrefix().length() + 1)).intValue();
        } catch (java.lang.NumberFormatException e) {
            log.error("failed to convert systemName " + systemName + " to a turnout address");
            return null;
        }
        Turnout t = new TamsTurnout(addr, getSystemPrefix(), tc);
        t.setUserName(userName);
        return t;
    }

    boolean noWarnDelete = false;

    private final static Logger log = LoggerFactory.getLogger(TamsTurnoutManager.class.getName());
}

/* @(#)TamsTurnoutManager.java */

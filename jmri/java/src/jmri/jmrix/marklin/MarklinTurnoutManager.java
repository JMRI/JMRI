// MarklinTurnoutManager.java

package jmri.jmrix.marklin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.Turnout;

/**
 * Implement turnout manager for Marklin systems.
 * <P>
 *
 * Based on work by Bob Jacobsen
 * @author	Kevin Dickerson  Copyright (C) 2012
 * @version	$Revision: 19646 $
 */
public class MarklinTurnoutManager extends jmri.managers.AbstractTurnoutManager {

    public MarklinTurnoutManager(MarklinSystemConnectionMemo memo) {

        adaptermemo=memo;
        prefix = adaptermemo.getSystemPrefix();
        tc = adaptermemo.getTrafficController();
    }
    
    MarklinTrafficController tc;
    MarklinSystemConnectionMemo adaptermemo;
    
    String prefix;
    
    public String getSystemPrefix() { return prefix; }

    public Turnout createNewTurnout(String systemName, String userName) {
        int addr;
        try {
            addr = Integer.valueOf(systemName.substring(getSystemPrefix().length()+1)).intValue();
        } catch (java.lang.NumberFormatException e){
            log.error("failed to convert systemName " + systemName + " to a turnout address");
            return null;
        }
        Turnout t = new MarklinTurnout(addr, getSystemPrefix(), tc);
        t.setUserName(userName);
        return t;
    }
    
    boolean noWarnDelete = false;

    static Logger log = LoggerFactory.getLogger(MarklinTurnoutManager.class.getName());
}

/* @(#)MarklinTurnoutManager.java */

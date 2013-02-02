// JMRIClientTurnoutManager.java

package jmri.jmrix.jmriclient;

import org.apache.log4j.Logger;
import jmri.Turnout;

/**
 * Implement turnout manager for JMRIClient systems
 * <P>
 * System names are "prefixnnn", where prefix is the system prefix and
 * nnn is the turnout number without padding.
 *
 * @author	Paul Bender Copyright (C) 2010
 * @version	$Revision$
 */

public class JMRIClientTurnoutManager extends jmri.managers.AbstractTurnoutManager {

    private JMRIClientSystemConnectionMemo memo=null;
    private String prefix = null;

    public JMRIClientTurnoutManager(JMRIClientSystemConnectionMemo memo) {
        this.memo=memo;
        this.prefix=memo.getSystemPrefix();
    }

    public String getSystemPrefix() { return prefix; }

    public Turnout createNewTurnout(String systemName, String userName) {
        Turnout t;
        int addr = Integer.valueOf(systemName.substring(prefix.length()+1)).intValue();
        t = new JMRIClientTurnout(addr,memo);
        t.setUserName(userName);
        return t;
    }

    static Logger log = Logger.getLogger(JMRIClientTurnoutManager.class.getName());

}

/* @(#)JMRIClientTurnoutManager.java */

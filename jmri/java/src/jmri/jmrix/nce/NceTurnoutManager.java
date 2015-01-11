// NceTurnoutManager.java

package jmri.jmrix.nce;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.Turnout;
import jmri.jmrix.nce.NceListener;

/**
 * Implement turnout manager for NCE systems.
 * <P>
 * System names are "NTnnn", where nnn is the turnout number without padding.
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 * @version	$Revision$
 */
public class NceTurnoutManager extends jmri.managers.AbstractTurnoutManager implements NceListener {

    public NceTurnoutManager(NceTrafficController tc, String prefix) {
    	super();
    	this.prefix = prefix;
    	this.tc = tc;
    }

    String prefix = "";
    NceTrafficController tc = null;
    
    public String getSystemPrefix() { return prefix; }

    public Turnout createNewTurnout(String systemName, String userName) {
        int addr = Integer.valueOf(systemName.substring(getSystemPrefix().length()+1)).intValue();
        Turnout t = new NceTurnout(tc, getSystemPrefix(), addr);
        t.setUserName(userName);

        return t;
    }
    
    public void reply(NceReply r) {
    	
    }
    
    public void message(NceMessage m) {
    	
    }
    
    static Logger log = LoggerFactory.getLogger(NceTurnoutManager.class.getName());
}

/* @(#)NceTurnoutManager.java */

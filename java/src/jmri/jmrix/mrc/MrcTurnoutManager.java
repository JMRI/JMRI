// MrcTurnoutManager.java

package jmri.jmrix.mrc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.Turnout;

/**
 * New MRC Turnout Manager
 * <P>
 * System names are "PTnnn", where nnn is the turnout number without padding.
 *
 * @author	Paul Bender Copyright (C) 2004
 * @author      Martin Wade Copyright (C) 2014
 * @version	$Revision: 22821 $
 */
public class MrcTurnoutManager extends jmri.managers.AbstractTurnoutManager {

    public MrcTurnoutManager(MrcTrafficController tc, String prefix) {
    	super();
    	this.prefix = prefix;
    	this.tc = tc;
    }
    
    String prefix = "";
    MrcTrafficController tc = null;

    public String getSystemPrefix() { return prefix; }

    public Turnout createNewTurnout(String systemName, String userName) {
        int addr = Integer.valueOf(systemName.substring(getSystemPrefix().length()+1)).intValue();
        Turnout t = new MrcTurnout(addr, tc, getSystemPrefix());
        t.setUserName(userName);
        return t;
    }
    
    static Logger log = LoggerFactory.getLogger(MrcTurnoutManager.class.getName());

}

/* @(#)MrcTurnoutManager.java */

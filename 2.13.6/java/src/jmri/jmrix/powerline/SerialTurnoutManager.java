// SerialTurnoutManager.java

package jmri.jmrix.powerline;

import jmri.managers.AbstractTurnoutManager;
import jmri.Turnout;

/**
 * Implement turnout manager for powerline systems
 * <P>
 * System names are "PTnnn", where nnn is the turnout number without padding.
 *
 * @author	Bob Jacobsen Copyright (C) 2003, 2006, 2007, 2008
 * Converted to multiple connection
 * @author kcameron Copyright (C) 2011
 * @version	$Revision$
 */
public class SerialTurnoutManager extends AbstractTurnoutManager {

	SerialTrafficController tc = null;
	
    public SerialTurnoutManager(SerialTrafficController tc) {
    	super();
    	this.tc = tc;
    }

    public String getSystemPrefix() { return tc.getAdapterMemo().getSystemPrefix(); }

    public Turnout createNewTurnout(String systemName, String userName) {
        // validate the system name, and normalize it
        String sName = tc.getAdapterMemo().getSerialAddress().normalizeSystemName(systemName);
        if (sName=="") {
            // system name is not valid
            return null;
        }
        // does this turnout already exist
        Turnout t = getBySystemName(sName);
        if (t!=null) {
            return null;
        }

        // create the turnout
        t = new SerialTurnout(sName, tc, userName);
        
        // does system name correspond to configured hardware
        if ( !tc.getAdapterMemo().getSerialAddress().validSystemNameConfig(sName,'T') ) {
            // system name does not correspond to configured hardware
            log.warn("Turnout '"+sName+"' refers to an undefined Serial Node.");
        }
        return t;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SerialTurnoutManager.class.getName());

}

/* @(#)SerialTurnoutManager.java */

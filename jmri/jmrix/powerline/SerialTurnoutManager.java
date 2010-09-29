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
 * @version	$Revision: 1.7 $
 */
public class SerialTurnoutManager extends AbstractTurnoutManager {

    public SerialTurnoutManager() {
    }

    public String getSystemPrefix() { return "P"; }

    public Turnout createNewTurnout(String systemName, String userName) {
        // validate the system name, and normalize it
        String sName = SerialAddress.normalizeSystemName(systemName);
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
        t = new SerialTurnout(sName,userName);
        
        // does system name correspond to configured hardware
        if ( !SerialAddress.validSystemNameConfig(sName,'T') ) {
            // system name does not correspond to configured hardware
            log.warn("Turnout '"+sName+"' refers to an undefined Serial Node.");
        }
        return t;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SerialTurnoutManager.class.getName());

}

/* @(#)SerialTurnoutManager.java */

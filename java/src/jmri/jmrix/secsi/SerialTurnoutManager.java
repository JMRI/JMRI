// SerialTurnoutManager.java

package jmri.jmrix.secsi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.managers.AbstractTurnoutManager;
import jmri.Turnout;

/**
 * Implement turnout manager for SECSI systems
 * <P>
 * System names are "VTnnn", where nnn is the turnout number without padding.
 *
 * @author	Bob Jacobsen Copyright (C) 2003, 2006, 2007
 * @version	$Revision$
 */
public class SerialTurnoutManager extends AbstractTurnoutManager {

    public SerialTurnoutManager() {
    	
    }

    public String getSystemPrefix() { return "V"; }

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
        // check under alternate name
        String altName = SerialAddress.convertSystemNameToAlternate(sName);
        t = getBySystemName(altName);
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

    static public SerialTurnoutManager instance() {
        if (_instance == null) _instance = new SerialTurnoutManager();
        return _instance;
    }
    static SerialTurnoutManager _instance = null;

    static Logger log = LoggerFactory.getLogger(SerialTurnoutManager.class.getName());

}

/* @(#)SerialTurnoutManager.java */

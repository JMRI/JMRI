// SerialTurnoutManager.java

package jmri.jmrix.powerline;

import org.apache.log4j.Logger;
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
    
public boolean allowMultipleAdditions(String systemName) { return false;  }

public String getNextValidAddress(String curAddress, String prefix){
    
    //If the hardware address past does not already exist then this can
    //be considered the next valid address.
    Turnout s = getBySystemName(prefix + typeLetter() + curAddress);
    if(s==null){
        return curAddress;
    }
    
    // This bit deals with handling the curAddress, and how to get the next address.
    int iName = 0;
    //Address starts with a single letter called a house code.
    String houseCode = curAddress.substring(0,1);
    try {
        iName = Integer.parseInt(curAddress.substring(1));
    } catch (NumberFormatException ex) {
        log.error("Unable to convert " + curAddress + " Hardware Address to a number");
        jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                            showInfoMessage("Error","Unable to convert " + curAddress + " to a valid Hardware Address",""+ex, "",true, false, org.apache.log4j.Level.ERROR);
        return null;
    }
    
    //Check to determine if the systemName is in use, return null if it is,
    //otherwise return the next valid address.
    s = getBySystemName(prefix+typeLetter()+curAddress);
    if(s!=null){
        for(int x = 1; x<10; x++){
            iName++;
            s = getBySystemName(prefix+typeLetter()+houseCode+(iName));
            if(s==null)
                return houseCode+iName;
        }
        return null;
    } else {
        return houseCode+iName;
    }
}

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

    static Logger log = Logger.getLogger(SerialTurnoutManager.class.getName());

}

/* @(#)SerialTurnoutManager.java */

// CbusTurnoutManager.java

package jmri.jmrix.can.cbus;

import jmri.*;
import jmri.managers.AbstractTurnoutManager;

/**
 * CAN CBUS implementation of a TurnoutManager.
 * <p>
 * Turnouts must be manually created.
 *
 * @author			Bob Jacobsen Copyright (C) 2008
 * @version			$Revision$
 * @since 2.3.1
 */
public class CbusTurnoutManager extends AbstractTurnoutManager {
	
    public String getSystemPrefix() { return "M"; }

    /**
     * Internal method to invoke the factory, after all the
     * logic for returning an existing method has been invoked.
     * @return never null
     */
    protected Turnout createNewTurnout(String systemName, String userName) {
        if (userName!=null) 
            return new CbusTurnout(systemName, userName);
        else 
            return new CbusTurnout(systemName);
    }
    
    public boolean allowMultipleAdditions() { return false;  }
    
   /**
    * A method that creates an array of systems names to allow bulk
    * creation of turnouts.
    */
    //further work needs to be done on how to format a number of CMRI turnout, therefore this method will only return one entry.
    public String[] formatRangeOfAddresses(String start, int numberToAdd, String prefix){
        numberToAdd = 1;
        String range[] = new String[numberToAdd];
        for (int x = 0; x < numberToAdd; x++){
            range[x] = prefix+"T"+start;
        }
        return range;
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CbusTurnoutManager.class.getName());
}

/* @(#)CbusTurnoutManager.java */

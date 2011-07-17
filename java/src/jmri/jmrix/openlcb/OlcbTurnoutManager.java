// OlcbTurnoutManager.java

package jmri.jmrix.openlcb;

import jmri.*;
import jmri.managers.AbstractTurnoutManager;

/**
 * OpenLCB implementation of a TurnoutManager.
 * <p>
 * Turnouts must be manually created.
 *
 * @author			Bob Jacobsen Copyright (C) 2008, 2010
 * @version			$Revision: 1.1 $
 * @since 2.3.1
 */
public class OlcbTurnoutManager extends AbstractTurnoutManager {
	
    public String getSystemPrefix() { return "M"; }

    /**
     * Internal method to invoke the factory, after all the
     * logic for returning an existing method has been invoked.
     * @return never null
     */
    protected Turnout createNewTurnout(String systemName, String userName) {
        if (userName!=null) 
            return new OlcbTurnout(systemName, userName);
        else 
            return new OlcbTurnout(systemName);
    }
    
    public boolean allowMultipleAdditions() { return false;  }
    
   /**
    * A method that creates an array of systems names to allow bulk
    * creation of turnouts.
    */
    //further work needs to be done on how to format a number of turnouts, therefore this method will only return one entry.
    public String[] formatRangeOfAddresses(String start, int numberToAdd, String prefix){
        numberToAdd = 1;
        String range[] = new String[numberToAdd];
        for (int x = 0; x < numberToAdd; x++){
            range[x] = prefix+"T"+start;
        }
        return range;
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(OlcbTurnoutManager.class.getName());
}

/* @(#)OlcbTurnoutManager.java */

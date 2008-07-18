// CbusTurnoutManager.java

package jmri.jmrix.can.cbus;

import jmri.*;
import jmri.jmrix.can.*;

/**
 * CAN CBUS implementation of a TurnoutManager.
 * <p>
 * Turnouts must be manually created.
 *
 * @author			Bob Jacobsen Copyright (C) 2008
 * @version			$Revision: 1.1 $
 * @since 2.3.1
 */
public class CbusTurnoutManager extends AbstractTurnoutManager {
	
    public char systemLetter() { return 'M'; }

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
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(CbusTurnoutManager.class.getName());
}

/* @(#)CbusTurnoutManager.java */

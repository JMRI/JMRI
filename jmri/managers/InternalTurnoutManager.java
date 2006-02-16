// InternalTurnoutManager.java

package jmri.managers;

import jmri.Turnout;
import jmri.AbstractTurnout;
import jmri.AbstractTurnoutManager;

/**
 * Implement a turnout manager for "Internal" (virtual) turnouts.
 *
 * @author			Bob Jacobsen Copyright (C) 2006
 * @version			$Revision: 1.1 $
 */
public class InternalTurnoutManager extends AbstractTurnoutManager {

    /**
     * Create and return an internal (no layout connection) turnout
     */
    protected Turnout createNewTurnout(String systemName, String userName) {
        return new AbstractTurnout(systemName, userName){
            protected void forwardCommandChangeToLayout(int s) {}
        };
    }
    
    public char systemLetter() { return 'I'; }
    
    /*
     * Turnout operation support. Internal turnouts don't need retries.
     */
    
    public String[] getValidOperationTypes() { return new String[]{"NoFeedback"}; }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(InternalTurnoutManager.class.getName());
}

/* @(#)InternalTurnoutManager.java */

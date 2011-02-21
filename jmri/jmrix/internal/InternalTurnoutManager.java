// InternalTurnoutManager.java

package jmri.jmrix.internal;

/**
 * Implement a turnout manager for "Internal" (virtual) turnouts.
 *
 * @author			Bob Jacobsen Copyright (C) 2006
 * @version			$Revision: 1.1 $
 */
public class InternalTurnoutManager extends jmri.managers.InternalTurnoutManager {

    public InternalTurnoutManager(String prefix){
        super();
        this.prefix = prefix;
    }
        
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(InternalTurnoutManager.class.getName());
}

/* @(#)InternalTurnoutManager.java */

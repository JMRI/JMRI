// InternalTurnoutManager.java

package jmri.jmrix.internal;

import org.apache.log4j.Logger;

/**
 * Implement a turnout manager for "Internal" (virtual) turnouts.
 *
 * @author			Bob Jacobsen Copyright (C) 2006
 * @version			$Revision$
 */
@edu.umd.cs.findbugs.annotations.SuppressWarnings(value="NM_SAME_SIMPLE_NAME_AS_SUPERCLASS", justification="name assigned historically")
public class InternalTurnoutManager extends jmri.managers.InternalTurnoutManager {

    public InternalTurnoutManager(String prefix){
        super();
        this.prefix = prefix;
    }
        
    static Logger log = Logger.getLogger(InternalTurnoutManager.class.getName());
}

/* @(#)InternalTurnoutManager.java */

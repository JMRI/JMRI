// ActiveFlag.java

package jmri.jmrix.dcc4pc;

import org.apache.log4j.Logger;

/**
 * Provide a flag to indicate that the subsystem provided by
 * this package is active.
 * <P>
 * This is a very light-weight class, carrying only the flag,
 * so as to limit the number of unneeded class loadings.
 *
 * @author		Bob Jacobsen  Copyright (C) 2003
 * @version             $Revision: 17977 $
 */
abstract public class ActiveFlag {

    static private boolean flag = false;
    static public void setActive() {
        flag = true;
    }
    static public boolean isActive() {
        return flag;
    }

    static Logger log = Logger.getLogger(ActiveFlag.class.getName());

}


/* @(#)AbstractMRReply.java */

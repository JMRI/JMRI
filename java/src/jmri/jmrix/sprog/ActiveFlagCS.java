// ActiveFlag.java

package jmri.jmrix.sprog;

import org.apache.log4j.Logger;

/**
 * Provide a flag to indicate that the subsystem provided by
 * this package is active.
 * <P>
 * This is a very light-weight class, carrying only the flag,
 * so as to limit the number of unneeded class loading.
 *
 * @author		Andrew Crosland  Copyright (C) 2006
 * @version             $Revision$
 */
abstract public class ActiveFlagCS {

    static private boolean flag = false;
    static public void setActive() {
        flag = true;
    }
    static public boolean isActive() {
        return flag;
    }

    static Logger log = Logger.getLogger(ActiveFlagCS.class.getName());

}


/* @(#)ActiveFlagCS.java */

// ActiveFlag.java
package jmri.jmrix.dccpp;

/**
 * Provide a flag to indicate that the system provided by this package is
 * active.
 * <P>
 * This is a very light-weight class, carrying only the flag, so as to limit the
 * number of unneeded class loadings.
 *
 * @author	Bob Jacobsen Copyright (C) 2003
 * @author	Mark Underwood Copyright (C) 2015
 * @version $Revision$
 *
 * Based on jmri.jmrix.lenz.ActiveFlag
 */
abstract public class ActiveFlag {

    static private boolean flag = false;

    static public void setActive() {
        flag = true;
    }

    static public boolean isActive() {
        return flag;
    }
}


/* @(#)AbstractMRReply.java */

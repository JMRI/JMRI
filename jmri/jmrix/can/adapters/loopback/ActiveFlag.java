// ActiveFlag.java

package jmri.jmrix.can.adapters.loopback;

/**
 * Provide a flag to indicate that the system provided by
 * this package is active.
 * <P>
 * This is a very light-weight class, carrying only the flag,
 * so as to limit the number of unneeded class loadings.
 *
 * @author		Bob Jacobsen  Copyright (C) 2003, 2008
 * @author      Andrew Crosland 2008
 * @version     $Revision: 1.1 $
 */
abstract public class ActiveFlag {

    static private boolean flag = false;
    static public void setActive() {
        flag = true;
    }
    static public boolean isActive() {
        return flag;
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ActiveFlag.class.getName());
}

/* @(#)ActiveFlag.java */

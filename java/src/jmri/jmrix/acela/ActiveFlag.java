// ActiveFlag.java
package jmri.jmrix.acela;

/**
 * Provide a flag to indicate that the system provided by this package is
 * active.
 * <P>
 * This is a very light-weight class, carrying only the flag, so as to limit the
 * number of unneeded class loading.
 *
 * @author	Bob Jacobsen Copyright (C) 2003
 * @deprecated since 4.5.1
 *
 * @author	Bob Coleman, Copyright (C) 2007, 2008 Based on mrc example, modified
 * to establish Acela support.
 */
@Deprecated
abstract public class ActiveFlag {

    static private boolean flag = false;

    static public void setActive() {
        flag = true;
    }

    static public boolean isActive() {
        return flag;
    }
}

/* @(#)ActiveFlag.java */

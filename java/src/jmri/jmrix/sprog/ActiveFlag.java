// ActiveFlag.java
package jmri.jmrix.sprog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide a flag to indicate that the subsystem provided by this package is
 * active.
 * <P>
 * This is a very light-weight class, carrying only the flag, so as to limit the
 * number of unneeded class loadings.
 *
 * @author	Bob Jacobsen Copyright (C) 2003
 * @version $Revision$
 */
abstract public class ActiveFlag {

    static private boolean flag = false;

    static public void setActive() {
        flag = true;
    }

    static public boolean isActive() {
        return flag;
    }

    private final static Logger log = LoggerFactory.getLogger(ActiveFlag.class.getName());

}


/* @(#)AbstractMRReply.java */

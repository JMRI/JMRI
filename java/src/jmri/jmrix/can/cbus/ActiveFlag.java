package jmri.jmrix.can.cbus;

/**
 * Provide a flag to indicate that the CBUS support is active.
 * <P>
 * This is a very light-weight class, carrying only the flag, so as to limit the
 * number of unneeded class loadings.
 *
 * @author	Bob Jacobsen Copyright (C) 2003, 2008, 2009
 * @author Andrew Crosland 2008
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

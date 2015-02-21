// JmriException.java
package jmri;

/**
 * Base for JMRI-specific exceptions. No functionality, just used to confirm
 * type-safety.
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2008, 2010
 * @version	$Revision$
 */
public class JmriException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 2804039803790645735L;

    public JmriException(String s, Throwable t) {
        super(s, t);
    }

    public JmriException(String s) {
        super(s);
    }

    public JmriException() {
    }

}

/* @(#)JmriException.java */

// Mx1MessageException.java
package jmri.jmrix.zimo;

import jmri.JmriException;

/**
 * Zimo-specfic exceptions.
 *
 * @author	Bob Jacobsen Copyright (C) 2002
 * @version	$Revision$
 *
 * Adapted by Sip Bosch for use with zimo Mx-1
 */
public class Mx1MessageException extends JmriException {

    /**
     *
     */
    private static final long serialVersionUID = -1112383031035600569L;

    public Mx1MessageException(String s) {
        super(s);
    }

    public Mx1MessageException() {
    }
}


/* @(#)Mx1MessageException.java */

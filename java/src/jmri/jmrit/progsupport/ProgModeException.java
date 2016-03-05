// ProgModeException.java
package jmri.jmrit.progsupport;

import jmri.ProgrammerException;

/**
 * Represents an attempt to use an unsupported mode or option while programming.
 * This is a configuration failure, not an operational failure
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2008
 * @version	$Revision$
 */
public class ProgModeException extends ProgrammerException {

    /**
     *
     */
    private static final long serialVersionUID = -8621463030237961954L;

    public ProgModeException(String s) {
        super(s);
    }

    public ProgModeException() {
    }

}

/* @(#)ProgModeException.java */

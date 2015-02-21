// LocoNetMessageException.java
package jmri.jmrix.loconet;

import jmri.JmriException;

/**
 * Exception to indicate a problem assembling a LocoNet message.
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2008
 * @version	$Revision$
 */
public class LocoNetMessageException extends JmriException {

    /**
     *
     */
    private static final long serialVersionUID = -6472332226397111753L;

    public LocoNetMessageException(String s) {
        super(s);
    }

    public LocoNetMessageException() {
    }
}


/* @(#)LocoNetMessageException.java */

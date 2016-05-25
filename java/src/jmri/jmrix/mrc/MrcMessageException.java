// MrcMessageException.java
package jmri.jmrix.mrc;

import jmri.JmriException;

/**
 * Exception to indicate a problem assembling a Mrc message.
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2008
 * @version	$Revision: 17977 $
 */
public class MrcMessageException extends JmriException {

    /**
     *
     */
    private static final long serialVersionUID = -2182935834694457386L;

    public MrcMessageException(String s) {
        super(s);
    }

    public MrcMessageException() {
    }
}


/* @(#)MrcMessageException.java */

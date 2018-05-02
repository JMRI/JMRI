package jmri.jmrix.mrc;

import jmri.JmriException;

/**
 * Exception to indicate a problem assembling a Mrc message.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2008
 */
public class MrcMessageException extends JmriException {

    public MrcMessageException(String s) {
        super(s);
    }

    public MrcMessageException() {
    }
}

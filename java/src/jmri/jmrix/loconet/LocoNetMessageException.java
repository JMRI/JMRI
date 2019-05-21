package jmri.jmrix.loconet;

import jmri.JmriException;

/**
 * Exception to indicate a problem assembling a LocoNet message.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2008
 */
public class LocoNetMessageException extends JmriException {
    // serialVersionUID used by jmrix.loconet.locormi, please do not remove
    private static final long serialVersionUID = -6472332226397111753L;
    
    public LocoNetMessageException(String s) {
        super(s);
    }

    public LocoNetMessageException() {
    }
}

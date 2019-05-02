package jmri.jmrix.dccpp;

import jmri.JmriException;

/**
 * @author Bob Jacobsen Copyright (C) 2002
 * @author Mark Underwood Copyright (C) 2015
 */
public class DCCppMessageException extends JmriException {

    public DCCppMessageException(String s) {
        super(s);
    }

    public DCCppMessageException() {
    }

}

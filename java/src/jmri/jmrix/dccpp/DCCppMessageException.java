package jmri.jmrix.dccpp;

import jmri.JmriException;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * @author Bob Jacobsen Copyright (C) 2002
 * @author Mark Underwood Copyright (C) 2015
 */
@API(status = EXPERIMENTAL)
public class DCCppMessageException extends JmriException {

    public DCCppMessageException(String s) {
        super(s);
    }

    public DCCppMessageException() {
    }

}

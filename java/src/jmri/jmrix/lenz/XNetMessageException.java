package jmri.jmrix.lenz;

import jmri.JmriException;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * @author Bob Jacobsen Copyright (C) 2002
 */
@API(status = EXPERIMENTAL)
public class XNetMessageException extends JmriException {

    public XNetMessageException(String s) {
        super(s);
    }

    public XNetMessageException() {
    }

}

package jmri.jmrix.mrc;

import jmri.JmriException;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Exception to indicate a problem assembling a Mrc message.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2008
 */
@API(status = EXPERIMENTAL)
public class MrcMessageException extends JmriException {

    public MrcMessageException(String s) {
        super(s);
    }

    public MrcMessageException() {
    }
}

package jmri.jmrix.lenz;

import jmri.JmriException;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * @author Bob Jacobsen Copyright (C) 2001
 */
@API(status = EXPERIMENTAL)
public class XNetException extends JmriException {

    public XNetException(String m) {
        super(m);
    }

}

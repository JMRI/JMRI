package jmri;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Base for JMRI-specific exceptions. No functionality, just used to confirm
 * type-safety.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2008, 2010
 */
@API(status = STABLE)
public class JmriException extends Exception {

    public JmriException(String s, Throwable t) {
        super(s, t);
    }

    public JmriException(String s) {
        super(s);
    }

    public JmriException(Throwable t) {
        super(t);
    }

    public JmriException() {
    }

}

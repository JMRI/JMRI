package jmri.jmrix.mrc;

import jmri.JmriException;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * MRC-specific exception
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
@API(status = EXPERIMENTAL)
public class MrcException extends JmriException {

    public MrcException(String m) {
        super(m);
    }

}

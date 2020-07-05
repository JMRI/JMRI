package jmri.jmrix.loconet;

import jmri.JmriException;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * LocoNet-specific exception
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
@API(status = EXPERIMENTAL)
public class LocoNetException extends JmriException {
    // serialVersionUID used by jmrix.loconet.locormi, please do not remove
    private static final long serialVersionUID = -7412254026659440390L;
    
    public LocoNetException(String m) {
        super(m);
    }

}

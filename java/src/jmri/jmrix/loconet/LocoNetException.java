package jmri.jmrix.loconet;

import jmri.JmriException;

/**
 * LocoNet-specific exception
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public class LocoNetException extends JmriException {
    // serialVersionUID used by jmrix.loconet.locormi, please do not remove
    private static final long serialVersionUID = -7412254026659440390L;
    
    public LocoNetException(String m) {
        super(m);
    }

}

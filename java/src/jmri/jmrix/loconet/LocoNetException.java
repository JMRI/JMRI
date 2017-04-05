package jmri.jmrix.loconet;

import jmri.JmriException;

/**
 * LocoNet-specific exception
 *
 * @author Bob Jacobsen Copyright (C) 2001
  */
public class LocoNetException extends JmriException {

    public LocoNetException(String m) {
        super(m);
    }

}

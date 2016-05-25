// LocoNetException.java
package jmri.jmrix.loconet;

import jmri.JmriException;

/**
 * LocoNet-specific exception
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 * @version	$Revision$
 */
public class LocoNetException extends JmriException {

    /**
     *
     */
    private static final long serialVersionUID = -7412254026659440390L;

    public LocoNetException(String m) {
        super(m);
    }

}


/* @(#)LocoNetException.java */

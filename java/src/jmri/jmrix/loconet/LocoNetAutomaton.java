// LocoNetAutomaton.java

package jmri.jmrix.loconet;

import org.apache.log4j.Logger;

/**
 * Extend the Automat support to include convenient access to LocoNet messages.
 *
 * Deprecated because it can't do multiple connections
 *
 * @author	Bob Jacobsen    Copyright (C) 2003, 2010
 * @version     $Revision$
 * @deprecated 2.9.4
 */
@Deprecated
abstract public class LocoNetAutomaton extends jmri.jmrit.automat.AbstractAutomaton {

    public LocoNetAutomaton() {}

    protected void sendMessage(LocoNetMessage m) {
        LnTrafficController.instance().sendLocoNetMessage(m);
    }

    // initialize logging
    static Logger log = Logger.getLogger(LocoNetAutomaton.class.getName());
}

/* @(#)LocoNetAutomaton.java */

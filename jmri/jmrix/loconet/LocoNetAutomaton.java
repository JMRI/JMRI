// LocoNetAutomaton.java

package jmri.jmrix.loconet;

/**
 * Extend the Automat support to include convenient access to LocoNet messages.
 *
 * @author	Bob Jacobsen    Copyright (C) 2003
 * @version     $Revision: 1.1 $
 */
abstract public class LocoNetAutomaton extends jmri.jmrit.automat.AbstractAutomaton {

    public LocoNetAutomaton() {}

    protected void sendMessage(LocoNetMessage m) {
        LnTrafficController.instance().sendLocoNetMessage(m);
    }

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LocoNetAutomaton.class.getName());
}

/* @(#)LocoNetAutomaton.java */

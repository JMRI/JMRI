// NceComponentFactory.java
package jmri.jmrix.nce.swing;

import jmri.jmrix.nce.NceSystemConnectionMemo;

/**
 * Provide access to Swing components for the Nce subsystem.
 *
 * @author	Bob Jacobsen Copyright (C) 2010
 * @author	kcameron Copyright (C) 2010
 * @version $Revision$
 */
public class NceComponentFactory extends jmri.jmrix.swing.ComponentFactory {

    public NceComponentFactory(NceSystemConnectionMemo memo) {
        this.memo = memo;
    }

    NceSystemConnectionMemo memo;

    /**
     * Provide a menu with all items attached to this system connection
     */
    public javax.swing.JMenu getMenu() {
        if (memo.getDisabled()) {
            return null;
        }
        return new NceMenu(memo);
    }
}


/* @(#)LnComponentFactory.java */

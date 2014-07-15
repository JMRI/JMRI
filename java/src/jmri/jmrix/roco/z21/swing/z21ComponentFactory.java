// z21ComponentFactory.java

package jmri.jmrix.roco.z21.swing;

import jmri.jmrix.roco.z21.z21SystemConnectionMemo;

/**
 * Provide access to Swing components for the Roco Z21 subsystem.
 *
 * @author		Paul Bender   Copyright (C) 2014
 * @version             $Revision$
 */
public class z21ComponentFactory extends jmri.jmrix.swing.ComponentFactory {

    public z21ComponentFactory(z21SystemConnectionMemo memo) {
        this.memo = memo;
    }
    
    z21SystemConnectionMemo memo;
    
    /**
     * Provide a menu with all items attached to this system connection
     */
    public javax.swing.JMenu getMenu() {
        if (memo.getDisabled()) return null;
        return new z21Menu(memo);
    }
}

/* @(#)z21ComponentFactory.java */

// ComponentFactory.java

package jmri.jmrix.loconet.swing;

import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;

/**
 * Provide access to Swing components for the LocoNet subsystem.
 *
 * @author		Bob Jacobsen  Copyright (C) 2010
 * @version             $Revision: 1.2 $
 * @since 2.9.4
 */
public class ComponentFactory extends jmri.jmrix.swing.ComponentFactory {

    public ComponentFactory(LocoNetSystemConnectionMemo memo) {
        this.memo = memo;
    }
    
    LocoNetSystemConnectionMemo memo;
    
    /**
     * Provide a menu with all items attached to this system connection
     */
    public javax.swing.JMenu getMenu() {
        if (memo.getDisabled()) return null;
        return new LocoNetMenu(memo);
    }
}


/* @(#)ComponentFactory.java */

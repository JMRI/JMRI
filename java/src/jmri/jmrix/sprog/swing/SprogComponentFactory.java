package jmri.jmrix.sprog.swing;

import jmri.jmrix.sprog.SprogSystemConnectionMemo;
import jmri.jmrix.sprog.SPROGMenu;
import jmri.jmrix.sprog.SPROGCSMenu;
import jmri.jmrix.sprog.SprogConstants.SprogMode;

/**
 * Provide access to Swing components for the Sprog subsystem.
 *
 * @author	Bob Jacobsen Copyright (C) 2010
 * @author	Paul Bender Copyright (C) 2010
 * @since 3.5.1
 */
public class SprogComponentFactory extends jmri.jmrix.swing.ComponentFactory {

    public SprogComponentFactory(SprogSystemConnectionMemo memo) {
        this.memo = memo;
    }

    SprogSystemConnectionMemo memo;

    /**
     * Provide a menu with all items attached to this system connection
     */
    public javax.swing.JMenu getMenu() {
        if (memo.getDisabled()) {
            return null;
        }
        if(memo.getSprogMode() == SprogMode.SERVICE) {
            return new SPROGMenu(memo);
        } else {  // must be command station mode.
            return new SPROGCSMenu(memo);
        }
    }
}

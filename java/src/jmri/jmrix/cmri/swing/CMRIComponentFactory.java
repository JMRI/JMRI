package jmri.jmrix.cmri.swing;

import jmri.jmrix.cmri.CMRISystemConnectionMemo;
import jmri.jmrix.cmri.CMRIMenu;

/**
 * Provide access to Swing components for the CMRI subsystem.
 *
 * @author	Bob Jacobsen Copyright (C) 2010
 * @author	Paul Bender Copyright (C) 2010
 * @since 3.5.1
 */
public class CMRIComponentFactory extends jmri.jmrix.swing.ComponentFactory {

    public CMRIComponentFactory(CMRISystemConnectionMemo memo) {
        this.memo = memo;
    }

    CMRISystemConnectionMemo memo;

    /**
     * Provide a menu with all items attached to this system connection
     */
    public javax.swing.JMenu getMenu() {
        if (memo.getDisabled()) {
            return null;
        }
        return new CMRIMenu(memo);
    }
}

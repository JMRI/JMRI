package jmri.jmrix.openlcb.swing;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.openlcb.OpenLcbMenu;

/**
 * Provide access to Swing components for the LocoNet subsystem.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 * @since 2.9.4
 */
public class OpenLcbComponentFactory extends jmri.jmrix.swing.ComponentFactory {

    public OpenLcbComponentFactory(CanSystemConnectionMemo memo) {
        this.memo = memo;
    }

    CanSystemConnectionMemo memo;

    /**
     * Provide a menu with all items attached to this system connection
     */
    @Override
    public javax.swing.JMenu getMenu() {
        if (memo.getDisabled()) {
            return null;
        }
        return new OpenLcbMenu(memo);
    }
}




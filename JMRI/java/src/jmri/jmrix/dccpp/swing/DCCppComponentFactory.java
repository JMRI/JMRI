package jmri.jmrix.dccpp.swing;

import jmri.jmrix.dccpp.DCCppSystemConnectionMemo;

/**
 * Provide access to Swing components for the DCC++ subsystem.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Paul Bender Copyright (C) 2010
 * @author      Mark Underwood Copyright (C) 2015
 * @since 4.2.2
 */
public class DCCppComponentFactory extends jmri.jmrix.swing.ComponentFactory {

    public DCCppComponentFactory(DCCppSystemConnectionMemo memo) {
        this.memo = memo;
    }

    DCCppSystemConnectionMemo memo;

    /**
     * Provide a menu with all items attached to this system connection
     */
    @Override
    public javax.swing.JMenu getMenu() {
        if (memo.getDisabled()) {
            return null;
        }
        return new DCCppMenu(memo); // TODO: Create this menu
    }
}




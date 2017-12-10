package jmri.jmrix.rps.swing;

import jmri.jmrix.rps.RpsSystemConnectionMemo;
import jmri.jmrix.rps.RpsMenu;

/**
 * Provide access to Swing components for the RPS subsystem.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Paul Bender Copyright (C) 2010
 * @since 2.11.1
 */
public class RpsComponentFactory extends jmri.jmrix.swing.ComponentFactory {

    public RpsComponentFactory(RpsSystemConnectionMemo memo) {
        this.memo = memo;
    }

    RpsSystemConnectionMemo memo;

    /**
     * Provide a menu with all items attached to this system connection.
     */
    @Override
    public javax.swing.JMenu getMenu() {
        if (memo.getDisabled()) {
            return null;
        }
        return new RpsMenu(memo);
    }
}


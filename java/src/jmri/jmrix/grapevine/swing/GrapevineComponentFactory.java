package jmri.jmrix.grapevine.swing;

import jmri.jmrix.grapevine.GrapevineSystemConnectionMemo;
import jmri.jmrix.grapevine.GrapevineMenu;

/**
 * Provide access to Swing components for the Grapevine subsystem.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Paul Bender Copyright (C) 2017
 * @since 4.9.6
 */
public class GrapevineComponentFactory extends jmri.jmrix.swing.ComponentFactory {

    public GrapevineComponentFactory(GrapevineSystemConnectionMemo memo) {
        this.memo = memo;
    }

    private GrapevineSystemConnectionMemo memo;

    /**
     * Provide a menu with all items attached to this system connection.
     */
    @Override
    public javax.swing.JMenu getMenu() {
        if (memo.getDisabled()) {
            return null;
        }
        return new GrapevineMenu(memo);
    }
}


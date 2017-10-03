package jmri.jmrix.lenz.swing;

import jmri.jmrix.lenz.XNetSystemConnectionMemo;

/**
 * Provide access to Swing components for the XpressNet subsystem.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Paul Bender Copyright (C) 2010
 * @since 2.11.1
 */
public class XNetComponentFactory extends jmri.jmrix.swing.ComponentFactory {

    public XNetComponentFactory(XNetSystemConnectionMemo memo) {
        this.memo = memo;
    }

    XNetSystemConnectionMemo memo;

    /**
     * Provide a menu with all items attached to this system connection.
     */
    @Override
    public javax.swing.JMenu getMenu() {
        if (memo.getDisabled()) {
            return null;
        }
        return new XNetMenu(memo);
    }
}


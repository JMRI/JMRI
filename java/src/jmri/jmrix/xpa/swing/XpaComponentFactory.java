package jmri.jmrix.xpa.swing;

import jmri.jmrix.xpa.XpaSystemConnectionMemo;

/**
 * Provide access to Swing components for the XPA subsystem.
 *
 * @author	Bob Jacobsen Copyright (C) 2010
 * @author	Paul Bender Copyright (C) 2010,2016
 * @since 4.3.6
 */
public class XpaComponentFactory extends jmri.jmrix.swing.ComponentFactory {

    public XpaComponentFactory(XpaSystemConnectionMemo memo) {
        this.memo = memo;
    }

    XpaSystemConnectionMemo memo;

    /**
     * Provide a menu with all items attached to this system connection
     */
    @Override
    public javax.swing.JMenu getMenu() {
        if (memo.getDisabled()) {
            return null;
        }
        return new XpaMenu(memo);
    }

}

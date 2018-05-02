package jmri.jmrix.oaktree.swing;

import jmri.jmrix.oaktree.OakTreeSystemConnectionMemo;
import jmri.jmrix.oaktree.OakTreeMenu;

/**
 * Provide access to Swing components for the Oaktree subsystem.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Paul Bender Copyright (C) 2017
 * @since 4.9.6
 */
public class OakTreeComponentFactory extends jmri.jmrix.swing.ComponentFactory {

    public OakTreeComponentFactory(OakTreeSystemConnectionMemo memo) {
        this.memo = memo;
    }

    OakTreeSystemConnectionMemo memo;

    /**
     * Provide a menu with all items attached to this system connection.
     */
    @Override
    public javax.swing.JMenu getMenu() {
        if (memo.getDisabled()) {
            return null;
        }
        return new OakTreeMenu(memo);
    }

}

package jmri.jmrix.direct.swing;

import jmri.jmrix.direct.DirectSystemConnectionMemo;
import jmri.jmrix.direct.DirectMenu;

/**
 * Provide access to Swing components for the Direct subsystem.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Paul Bender Copyright (C) 2017
 * @since 4.9.6
 */
public class DirectComponentFactory extends jmri.jmrix.swing.ComponentFactory {

    public DirectComponentFactory(DirectSystemConnectionMemo memo) {
        this.memo = memo;
    }

    DirectSystemConnectionMemo memo;

    /**
     * Provide a menu with all (in this case: no) items attached to this system connection.
     */
    @Override
    public javax.swing.JMenu getMenu() {
        if (memo.getDisabled()) {
            return null;
        }
        return new DirectMenu(memo);
    }

}

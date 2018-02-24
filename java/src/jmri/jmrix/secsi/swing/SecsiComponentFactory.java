package jmri.jmrix.secsi.swing;

import jmri.jmrix.secsi.SecsiSystemConnectionMemo;
import jmri.jmrix.secsi.SecsiMenu;

/**
 * Provide access to Swing components for the SECSI subsystem.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Paul Bender Copyright (C) 2010
 * @since 2.11.1
 */
public class SecsiComponentFactory extends jmri.jmrix.swing.ComponentFactory {

    public SecsiComponentFactory(SecsiSystemConnectionMemo memo) {
        this.memo = memo;
    }

    private SecsiSystemConnectionMemo memo = null;

    /**
     * Provide a menu with all items attached to this system connection.
     */
    @Override
    public javax.swing.JMenu getMenu() {
        if (memo.getDisabled()) {
            return null;
        }
        return new SecsiMenu(memo);
    }

}

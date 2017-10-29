package jmri.jmrix.bachrus.swing;

import jmri.jmrix.bachrus.SpeedoMenu;
import jmri.jmrix.bachrus.SpeedoSystemConnectionMemo;

/**
 * Provide access to Swing components for the Speedo subsystem.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Paul Bender Copyright (C) 2010
 * @since 3.5.1
 */
public class SpeedoComponentFactory extends jmri.jmrix.swing.ComponentFactory {

    public SpeedoComponentFactory(SpeedoSystemConnectionMemo memo) {
        this.memo = memo;
    }

    SpeedoSystemConnectionMemo memo;

    /**
     * Provide a menu with all items attached to this system connection
     */
    @Override
    public javax.swing.JMenu getMenu() {
        if (memo.getDisabled()) {
            return null;
        }
        return new SpeedoMenu(memo);
    }
}

package jmri.jmrix.acela.swing;

import jmri.jmrix.acela.AcelaMenu;
import jmri.jmrix.acela.AcelaSystemConnectionMemo;

/**
 * Provide access to Swing components for the Acela subsystem.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Paul Bender Copyright (C) 2010
 * @since 3.5.1
 */
public class AcelaComponentFactory extends jmri.jmrix.swing.ComponentFactory {

    public AcelaComponentFactory(AcelaSystemConnectionMemo memo) {
        this.memo = memo;
    }

    AcelaSystemConnectionMemo memo;

    /**
     * Provide a menu with all items attached to this system connection
     */
    @Override
    public javax.swing.JMenu getMenu() {
        if (memo.getDisabled()) {
            return null;
        }
        return new AcelaMenu(memo);
    }

}

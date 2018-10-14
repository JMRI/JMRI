package jmri.jmrix.easydcc.swing;

import jmri.jmrix.easydcc.EasyDccMenu;
import jmri.jmrix.easydcc.EasyDccSystemConnectionMemo;

/**
 * Provide access to Swing components for the EasyDCC subsystem.
 * Copied from Acela 2017
 *
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Paul Bender Copyright (C) 2010
 * @since 4.9.5
 */
public class EasyDccComponentFactory extends jmri.jmrix.swing.ComponentFactory {

    public EasyDccComponentFactory(EasyDccSystemConnectionMemo memo) {
        this.memo = memo;

    }

    private EasyDccSystemConnectionMemo memo;

    /**
     * Provide a menu with all items attached to this system connection
     */
    @Override
    public javax.swing.JMenu getMenu() {
        if (memo.getDisabled()) {
            return null;
        }
        return new EasyDccMenu(memo);
    }

}

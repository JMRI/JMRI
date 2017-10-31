package jmri.jmrix.tmcc.swing;

import jmri.jmrix.tmcc.TmccMenu;
import jmri.jmrix.tmcc.TmccSystemConnectionMemo;

/**
 * Provide access to Swing components for the TMCC subsystem.
 * Copied from EasyDCC 2017
 *
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Paul Bender Copyright (C) 2010
 * @since 4.9.6
 */
public class TmccComponentFactory extends jmri.jmrix.swing.ComponentFactory {

    public TmccComponentFactory(TmccSystemConnectionMemo memo) {
        this.memo = memo;

    }

    private TmccSystemConnectionMemo memo;

    /**
     * Provide a menu with all items attached to this system connection
     */
    @Override
    public javax.swing.JMenu getMenu() {
        if (memo.getDisabled()) {
            return null;
        }
        return new TmccMenu(memo);
    }

}

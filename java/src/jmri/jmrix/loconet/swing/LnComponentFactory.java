package jmri.jmrix.loconet.swing;

import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;

/**
 * Provide access to Swing components for the LocoNet subsystem.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 * @since 2.9.4
 */
public class LnComponentFactory extends jmri.jmrix.swing.ComponentFactory {

    public LnComponentFactory(LocoNetSystemConnectionMemo memo) {
        this.memo = memo;
    }

    LocoNetSystemConnectionMemo memo;

    /**
     * Provide a menu with all items attached to this system connection.
     */
    @Override
    public javax.swing.JMenu getMenu() {
        if (memo.getDisabled()) {
            return null;
        }
        return new LocoNetMenu(memo);
    }

}

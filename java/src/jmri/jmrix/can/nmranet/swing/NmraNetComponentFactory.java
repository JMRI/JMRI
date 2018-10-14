package jmri.jmrix.can.nmranet.swing;

import jmri.jmrix.can.CanSystemConnectionMemo;

/**
 * Provide access to Swing components for the LocoNet subsystem.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 * 
 * @since 2.99.2
 */
public class NmraNetComponentFactory extends jmri.jmrix.swing.ComponentFactory {

    public NmraNetComponentFactory(CanSystemConnectionMemo memo) {
        this.memo = memo;
    }

    CanSystemConnectionMemo memo;

    /**
     * Provide a menu with all items attached to this system connection
     */
    @Override
    public javax.swing.JMenu getMenu() {
        if (memo.getDisabled()) {
            return null;
        }
        return new NmraNetMenu(memo);
    }
}




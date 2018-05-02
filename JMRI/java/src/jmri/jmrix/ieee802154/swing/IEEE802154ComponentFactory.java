package jmri.jmrix.ieee802154.swing;

import jmri.jmrix.ieee802154.IEEE802154SystemConnectionMemo;

/**
 * Provide access to Swing components for the IEEE 802.15.4 subsystem.
 *
 * @author Paul Bender Copyright (C) 2014
 */
public class IEEE802154ComponentFactory extends jmri.jmrix.swing.ComponentFactory {

    public IEEE802154ComponentFactory(IEEE802154SystemConnectionMemo memo) {
        this.memo = memo;
    }

    IEEE802154SystemConnectionMemo memo;

    /**
     * Provide a menu with all items attached to this system connection
     */
    @Override
    public javax.swing.JMenu getMenu() {
        if (memo.getDisabled()) {
            return null;
        }
        return new IEEE802154Menu(memo);
    }
}




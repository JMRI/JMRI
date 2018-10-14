package jmri.jmrix.ieee802154.xbee.swing;

import jmri.jmrix.ieee802154.xbee.XBeeConnectionMemo;

/**
 * Provide access to Swing components for the XBee subsystem.
 *
 * @author Bob Jacobsen Copyright (C) 2014
 */
public class XBeeComponentFactory extends jmri.jmrix.swing.ComponentFactory {

    public XBeeComponentFactory(XBeeConnectionMemo memo) {
        this.memo = memo;
    }

    XBeeConnectionMemo memo;

    /**
     * Provide a menu with all items attached to this system connection
     */
    @Override
    public javax.swing.JMenu getMenu() {
        if (memo.getDisabled()) {
            return null;
        }
        return new XBeeMenu(memo);
    }
}



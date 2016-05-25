// PowerlineMenu.java
package jmri.jmrix.powerline.swing;

import java.util.ResourceBundle;
import javax.swing.JMenu;
import jmri.jmrix.powerline.SerialSystemConnectionMemo;

/**
 * Create a "Systems" menu containing the JMRI Powerline-specific tools.
 *
 * @author	Bob Jacobsen Copyright 2003, 2010 Copied from NCE Converted to
 * multiple connection
 * @author kcameron Copyright (C) 2011
 * @version $Revision$
 */
public class PowerlineMenu extends JMenu {

    /**
     *
     */
    private static final long serialVersionUID = 5098217284738165128L;

    /**
     * Create a Powerline menu. And loads the SerialSystemConnectionMemo to the
     * various actions. Actions will open new windows.
     */
    // Need to Sort out the Powerline server menu items;
    public PowerlineMenu(SerialSystemConnectionMemo memo) {
        super();

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.powerline.SystemBundle");

        // memo can not be null!
        if (memo == null) {
            new Exception().printStackTrace();
            return;
        }

        setText(memo.getUserName());

        jmri.util.swing.WindowInterface wi = new jmri.util.swing.sdi.JmriJFrameInterface();

        for (Item item : panelItems) {
            if (item == null) {
                add(new javax.swing.JSeparator());
            } else {
                PowerlineNamedPaneAction a = new PowerlineNamedPaneAction(rb.getString(item.name), wi, item.load, memo);
                add(a);
                a.setEnabled(item.enable.equals(ALL));
            }
        }

        // do we have a TrafficController?
        setEnabled(memo.getTrafficController() != null);	// disable menu, no connection, no tools!

        add(new javax.swing.JSeparator());
    }

    // Enable or disable menu items based on system connection
    private static final String ALL = "All Powerline connections";

    private Item[] panelItems = new Item[]{
        new Item("MenuItemCommandMonitor", "jmri.jmrix.powerline.swing.serialmon.SerialMonPane", ALL),
        new Item("MenuItemSendCommand", "jmri.jmrix.powerline.swing.packetgen.SerialPacketGenPane", ALL)
    };

    static class Item {

        Item(String name, String load, String enable) {
            this.name = name;
            this.load = load;
            this.enable = enable;
        }
        String name;
        String load;
        String enable;
    }
}

/* @(#)PowerlineMenu.java */

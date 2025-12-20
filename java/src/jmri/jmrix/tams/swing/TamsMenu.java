package jmri.jmrix.tams.swing;

import javax.swing.JMenu;
import jmri.jmrix.tams.TamsSystemConnectionMemo;

/**
 * Create a "Systems" menu containing the Tams-specific tools.
 *
 * Based on work by Bob Jacobsen
 * @author Kevin Dickerson Copyright (C) 2012
 */
public class TamsMenu extends JMenu {

    public TamsMenu(TamsSystemConnectionMemo memo) {
        super();

        if (memo != null) {
            setText(memo.getUserName());
        } else {
            setText(Bundle.getMessage("MenuTams"));
        }

        jmri.util.swing.WindowInterface wi = new jmri.util.swing.sdi.JmriJFrameInterface();

        for (Item item : panelItems) {
            if (item == null) {
                add(new javax.swing.JSeparator());
            } else {
                add(new TamsNamedPaneAction(Bundle.getMessage(item.name), wi, item.load, memo));
            }
        }

    }

    Item[] panelItems = new Item[]{
        new Item("MenuItemTamsMonitor", "jmri.jmrix.tams.swing.monitor.TamsMonPane"),
        new Item("MenuItemSendPacket", "jmri.jmrix.tams.swing.packetgen.PacketGenPanel"),
        new Item("MenuItemInfo", "jmri.jmrix.tams.swing.statusframe.StatusPanel"),
        new Item("MenuItemLocoData", "jmri.jmrix.tams.swing.locodatabase.LocoDataPane"),};

    static class Item {

        Item(String name, String load) {
            this.name = name;
            this.load = load;
        }

        String name;
        String load;
    }

    // private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TamsMenu.class);

}

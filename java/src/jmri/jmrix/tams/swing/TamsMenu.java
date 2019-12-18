package jmri.jmrix.tams.swing;

import java.util.ResourceBundle;
import javax.swing.JMenu;
import jmri.jmrix.tams.TamsSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create a "Systems" menu containing the Tams-specific tools.
 *
 * Based on work by Bob Jacobsen
 * @author	Kevin Dickerson Copyright (C) 2012
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

        if (jmri.InstanceManager.getNullableDefault(jmri.jmrit.beantable.ListedTableFrame.class) == null) {
            try {
                new jmri.jmrit.beantable.ListedTableFrame();
            } catch (java.lang.NullPointerException ex) {
                log.error("Unable to register Tams table");
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

    private final static Logger log = LoggerFactory.getLogger(TamsMenu.class);

}

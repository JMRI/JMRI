package jmri.jmrix.marklin.swing;

import javax.swing.JMenu;
import jmri.jmrix.marklin.MarklinSystemConnectionMemo;

/**
 * Create a "Systems" menu containing the JMRI Marklin-specific tools.
 *
 * @author Kevin Dickerson
 */
public class MarklinMenu extends JMenu {

    public MarklinMenu(MarklinSystemConnectionMemo memo) {
        super();

        if (memo != null) {
            setText(memo.getUserName());
        } else {
            setText(Bundle.getMessage("MenuMarklin"));
        }

        jmri.util.swing.WindowInterface wi = new jmri.util.swing.sdi.JmriJFrameInterface();

        for (Item item : panelItems) {
            if (item == null) {
                add(new javax.swing.JSeparator());
            } else {
                add(new MarklinNamedPaneAction(Bundle.getMessage(item.name), wi, item.load, memo));
            }
        }
        
        // Add separator and MCAN BOOT menu item
        if (memo != null) {
            add(new javax.swing.JSeparator());
            add(new MarklinSendBootAction(Bundle.getMessage("MenuItemSendMCanBoot"), memo));
        }

    }

    private static final Item[] panelItems = new Item[]{
        new Item("MenuItemMarklinMonitor", "jmri.jmrix.marklin.swing.monitor.MarklinMonPane"),
        new Item("MenuItemSendPacket", "jmri.jmrix.marklin.swing.packetgen.PacketGenPanel"),};

    private static class Item {

        Item(String name, String load) {
            this.name = name;
            this.load = load;
        }

        String name;
        String load;
    }

}

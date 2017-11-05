package jmri.jmrix.marklin.swing;

import javax.swing.JMenu;
import jmri.jmrix.marklin.MarklinSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create a "Systems" menu containing the Jmri Marklin-specific tools.
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

        if (jmri.InstanceManager.getNullableDefault(jmri.jmrit.beantable.ListedTableFrame.class) == null) {
            try {
                new jmri.jmrit.beantable.ListedTableFrame();
            } catch (java.lang.NullPointerException ex) {
                log.error("Unable to register Marklin table");
            }
        }

    }

    Item[] panelItems = new Item[]{
        new Item("MenuItemMarklinMonitor", "jmri.jmrix.marklin.swing.monitor.MarklinMonPane"),
        new Item("MenuItemSendPacket", "jmri.jmrix.marklin.swing.packetgen.PacketGenPanel"),};

    static class Item {

        Item(String name, String load) {
            this.name = name;
            this.load = load;
        }

        String name;
        String load;
    }

    private final static Logger log = LoggerFactory.getLogger(MarklinMenu.class);

}

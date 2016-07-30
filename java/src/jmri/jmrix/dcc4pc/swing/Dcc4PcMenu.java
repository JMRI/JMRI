package jmri.jmrix.dcc4pc.swing;

import java.util.ResourceBundle;
import javax.swing.JMenu;
import jmri.jmrix.dcc4pc.Dcc4PcSystemConnectionMemo;

/**
 *
 * @author Kevin Dickerson
 */
public class Dcc4PcMenu extends JMenu {

    public Dcc4PcMenu(Dcc4PcSystemConnectionMemo memo) {
        super();

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.dcc4pc.Dcc4PcBundle");
        String title;
        if (memo != null) {
            title = memo.getUserName();
        } else {
            title = rb.getString("MenuDcc4Pc");
        }

        setText(title);

        jmri.util.swing.WindowInterface wi = new jmri.util.swing.sdi.JmriJFrameInterface();

        for (Item item : panelItems) {
            if (item == null) {
                add(new javax.swing.JSeparator());
            } else {
                add(new Dcc4PcNamedPaneAction(rb.getString(item.name), wi, item.load, memo));
            }
        }

        if (jmri.InstanceManager.getOptionalDefault(jmri.jmrit.beantable.ListedTableFrame.class) == null) {
            new jmri.jmrit.beantable.ListedTableFrame();
        }
    }

    Item[] panelItems = new Item[]{
        new Item("MenuItemDcc4PcMonitor", "jmri.jmrix.dcc4pc.swing.monitor.Dcc4PcMonPane"),
        new Item("MenuItemSendPacket", "jmri.jmrix.dcc4pc.swing.packetgen.PacketGenPanel"),
        new Item("MenuItemInfo", "jmri.jmrix.dcc4pc.swing.StatusPanel"),
        new Item("MenuItemBoardList", "jmri.jmrix.dcc4pc.swing.boardlists.BoardListPanel")

    };

    static class Item {

        Item(String name, String load) {
            this.name = name;
            this.load = load;
        }

        String name;
        String load;
    }
}

package jmri.jmrix.marklin.swing;

import java.util.ResourceBundle;
import javax.swing.JMenu;
import jmri.jmrix.marklin.MarklinSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Kevin Dickerson
 */
public class MarklinMenu extends JMenu {

    /**
     *
     */
    private static final long serialVersionUID = -7952313409946046904L;

    public MarklinMenu(MarklinSystemConnectionMemo memo) {
        super();

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.marklin.MarklinBundle");
        String title;
        if (memo != null) {
            title = memo.getUserName();
        } else {
            title = rb.getString("MenuMarklin");
        }

        setText(title);

        jmri.util.swing.WindowInterface wi = new jmri.util.swing.sdi.JmriJFrameInterface();

        for (Item item : panelItems) {
            if (item == null) {
                add(new javax.swing.JSeparator());
            } else {
                add(new MarklinNamedPaneAction(rb.getString(item.name), wi, item.load, memo));
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

    private final static Logger log = LoggerFactory.getLogger(MarklinMenu.class.getName());
}

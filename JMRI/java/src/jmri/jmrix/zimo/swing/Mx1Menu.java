package jmri.jmrix.zimo.swing;

import javax.annotation.Nonnull;
import javax.swing.JMenu;
import jmri.jmrix.zimo.Mx1SystemConnectionMemo;

/**
 * Create a "Systems" menu containing the JMRI Zimo-specific tools.
 *
 * @author	Bob Jacobsen Copyright 2003, 2010 Copied from nce.swing
 * @author Ken Cameron 2014
 * @author Kevin Dickerson 2014
 */
public class Mx1Menu extends JMenu {

    /**
     * Create a Zimo menu.
     *
     * @param memo the connection to associate actions with
     */
    public Mx1Menu(@Nonnull Mx1SystemConnectionMemo memo) {
        super();

        setText(memo.getUserName());

        jmri.util.swing.WindowInterface wi = new jmri.util.swing.sdi.JmriJFrameInterface();

        for (Item item : panelItems) {
            if (item == null) {
                add(new javax.swing.JSeparator());
            } else {
                Mx1NamedPaneAction a = new Mx1NamedPaneAction(Bundle.getMessage(item.name), wi, item.load, memo);
                add(a);
            }
        }

        // do we have an Mx1TrafficController?
        setEnabled(memo.getMx1TrafficController() != null);	// disable menu, no connection, no tools!

        add(new javax.swing.JSeparator());
    }

    private Item[] panelItems = new Item[]{
        new Item("MenuItemCommandMonitor", "jmri.jmrix.zimo.swing.monitor.Mx1MonPanel"), // NOI18N
        new Item("MenuItemSendCommand", "jmri.jmrix.zimo.swing.packetgen.Mx1PacketGenPanel"), // NOI18N
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

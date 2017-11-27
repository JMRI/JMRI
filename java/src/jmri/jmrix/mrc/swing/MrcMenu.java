package jmri.jmrix.mrc.swing;

import javax.annotation.Nonnull;
import javax.swing.JMenu;
import jmri.jmrix.mrc.MrcSystemConnectionMemo;

/**
 * Create a "Systems" menu containing the JMRI MRC-specific tools.
 *
 * @author Bob Jacobsen Copyright 2003, 2010 Copied from nce.swing
 * @author Ken Cameron 2014
 * @author Kevin Dickerson 2014
 */
public class MrcMenu extends JMenu {

    /**
     * Create an MRC menu and load the MrcSystemConnectionMemo to the various
     * actions. Actions will open new windows.
     *
     * @param memo sytem connection memo
     */
    // Need to Sort out the MRC server menu items;
    public MrcMenu(@Nonnull MrcSystemConnectionMemo memo) {
        super();

        setText(memo.getUserName());

        jmri.util.swing.WindowInterface wi = new jmri.util.swing.sdi.JmriJFrameInterface();

        for (Item item : panelItems) {
            if (item == null) {
                add(new javax.swing.JSeparator());
            } else {
                MrcNamedPaneAction a = new MrcNamedPaneAction(Bundle.getMessage(item.name), wi, item.load, memo);
                add(a);
            }
        }

        // do we have an MrcTrafficController?
        setEnabled(memo.getMrcTrafficController() != null); // disable menu, no connection, no tools!

        add(new javax.swing.JSeparator());
    }

    private Item[] panelItems = new Item[]{
        new Item("MenuItemCommandMonitor", "jmri.jmrix.mrc.swing.monitor.MrcMonPanel"), //IN18N
        new Item("MenuItemSendCommand", "jmri.jmrix.mrc.swing.packetgen.MrcPacketGenPanel"), //IN18N
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

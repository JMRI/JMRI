// LocoNetMenu.java
package jmri.jmrix.loconet.swing;

import java.util.ResourceBundle;
import javax.swing.JMenu;
import jmri.jmrix.loconet.LocoNetBundle;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;

/**
 * Create a "Systems" menu containing the Jmri LocoNet-specific tools.
 *
 * @author	Bob Jacobsen Copyright 2003, 2010
 * @version $Revision$
 */
public class LocoNetMenu extends JMenu {

    /**
     *
     */
    private static final long serialVersionUID = 5699192434035288187L;

    /**
     * Create a LocoNet menu. Preloads the TrafficController to certain actions.
     * Actions will open new windows.
     */
    // Need to Sort out the Loconet server menu items;
    public LocoNetMenu(LocoNetSystemConnectionMemo memo) {
        super();

        ResourceBundle rb = LocoNetBundle.bundle();

        if (memo != null) {
            setText(memo.getUserName());
        } else {
            setText(rb.getString("MenuLocoNet"));
        }

        jmri.util.swing.WindowInterface wi = new jmri.util.swing.sdi.JmriJFrameInterface();

        for (Item item : panelItems) {
            if (item == null) {
                add(new javax.swing.JSeparator());
            } else {
                add(new LnNamedPaneAction(rb.getString(item.name), wi, item.load, memo));
            }
        }
        add(new javax.swing.JSeparator());
        add(new jmri.jmrix.loconet.locormi.LnMessageServerAction(rb.getString("MenuItemStartLocoNetServer")));
        add(new jmri.jmrix.loconet.loconetovertcp.ServerAction(rb.getString("MenuItemLocoNetOverTCPServer")));
    }

    Item[] panelItems = new Item[]{
        new Item("MenuItemLocoNetMonitor", "jmri.jmrix.loconet.locomon.LocoMonPane"),
        new Item("MenuItemSlotMonitor", "jmri.jmrix.loconet.slotmon.SlotMonPane"),
        new Item("MenuItemClockMon", "jmri.jmrix.loconet.clockmon.ClockMonPane"),
        new Item("MenuItemLocoStats", "jmri.jmrix.loconet.locostats.LocoStatsPanel"),
        null,
        new Item("MenuItemBDL16Programmer", "jmri.jmrix.loconet.bdl16.BDL16Panel"),
        new Item("MenuItemLocoIOProgrammer", "jmri.jmrix.loconet.locoio.LocoIOPanel"),
        new Item("MenuItemPM4Programmer", "jmri.jmrix.loconet.pm4.PM4Panel"),
        new Item("MenuItemSE8cProgrammer", "jmri.jmrix.loconet.se8.SE8Panel"),
        new Item("MenuItemDS64Programmer", "jmri.jmrix.loconet.ds64.DS64Panel"),
        new Item("MenuItemCmdStnConfig", "jmri.jmrix.loconet.cmdstnconfig.CmdStnConfigPane"),
        new Item("MenuItemSetID", "jmri.jmrix.loconet.locoid.LocoIdPanel"),
        new Item("MenuItemDuplex", "jmri.jmrix.loconet.duplexgroup.swing.DuplexGroupTabbedPanel"),
        //new Item("MenuItemStartLocoNetServer",  "jmri.jmrix.loconet.locormi.LnMessageServerPanel"),
        //new Item("MenuItemLocoNetOverTCPServer","jmri.jmrix.loconet.loconetovertcp.ServerPanel"),
        null,
        new Item("MenuItemThrottleMessages", "jmri.jmrix.loconet.swing.throttlemsg.MessagePanel"),
        new Item("MenuItemSendPacket", "jmri.jmrix.loconet.locogen.LocoGenPanel"),
        new Item("MenuItemPr3ModeSelect", "jmri.jmrix.loconet.pr3.swing.Pr3SelectPane"),
        null,
        new Item("MenuItemDownload", "jmri.jmrix.loconet.downloader.LoaderPane"),
        new Item("MenuItemSoundload", "jmri.jmrix.loconet.soundloader.LoaderPane"),
        new Item("MenuItemSoundEditor", "jmri.jmrix.loconet.soundloader.EditorPane")
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

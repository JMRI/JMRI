package jmri.jmrix.loconet.swing;

import javax.swing.JMenu;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;

/**
 * Create a "Systems" menu containing the Jmri LocoNet-specific tools.
 *
 * @author Bob Jacobsen Copyright 2003, 2010
 */
public class LocoNetMenu extends JMenu {

    /**
     * Create a LocoNet menu. Preloads the TrafficController to certain actions.
     * Actions will open new windows.
     */
    // Need to Sort out the Loconet server menu items;
    public LocoNetMenu(LocoNetSystemConnectionMemo memo) {
        super();

        if (memo != null) {
            setText(memo.getUserName());
        } else {
            setText(Bundle.getMessage("MenuLocoNet"));
        }

        jmri.util.swing.WindowInterface wi = new jmri.util.swing.sdi.JmriJFrameInterface();

        for (Item item : panelItems) {
            if (item == null) {
                add(new javax.swing.JSeparator());
            } else {
                add(new LnNamedPaneAction(Bundle.getMessage(item.name), wi, item.load, memo));
            }
        }
        add(new javax.swing.JSeparator());
        add(new jmri.jmrix.loconet.locormi.LnMessageServerAction(Bundle.getMessage("MenuItemStartLocoNetServer")));
        add(new jmri.jmrix.loconet.loconetovertcp.LnTcpServerAction(Bundle.getMessage("MenuItemLocoNetOverTCPServer")));
    }

    Item[] panelItems = new Item[]{
        new Item("MenuItemLocoNetMonitor", "jmri.jmrix.loconet.locomon.LocoMonPane"), // NOI18N
        new Item("MenuItemSlotMonitor", "jmri.jmrix.loconet.slotmon.SlotMonPane"), // NOI18N
        new Item("MenuItemClockMon", "jmri.jmrix.loconet.clockmon.ClockMonPane"), // NOI18N
        new Item("MenuItemLocoStats", "jmri.jmrix.loconet.locostats.swing.LocoStatsPanel"), // NOI18N
        null,
        new Item("MenuItemBDL16Programmer", "jmri.jmrix.loconet.bdl16.BDL16Panel"), // NOI18N
        new Item("MenuItemLocoIOProgrammer", "jmri.jmrix.loconet.locoio.LocoIOPanel"), // NOI18N
        new Item("MenuItemPM4Programmer", "jmri.jmrix.loconet.pm4.PM4Panel"), // NOI18N
        new Item("MenuItemSE8cProgrammer", "jmri.jmrix.loconet.se8.SE8Panel"), // NOI18N
        new Item("MenuItemDS64Programmer", "jmri.jmrix.loconet.ds64.Ds64TabbedPanel"), // NOI18N
        new Item("MenuItemCmdStnConfig", "jmri.jmrix.loconet.cmdstnconfig.CmdStnConfigPane"), // NOI18N
        new Item("MenuItemSetID", "jmri.jmrix.loconet.locoid.LocoIdPanel"), // NOI18N
        new Item("MenuItemDuplex", "jmri.jmrix.loconet.duplexgroup.swing.DuplexGroupTabbedPanel"), // NOI18N
        //new Item("MenuItemStartLocoNetServer",  "jmri.jmrix.loconet.locormi.LnMessageServerPanel"), // NOI18N
        //new Item("MenuItemLocoNetOverTCPServer","jmri.jmrix.loconet.loconetovertcp.ServerPanel"), // NOI18N
        null,
        new Item("MenuItemThrottleMessages", "jmri.jmrix.loconet.swing.throttlemsg.MessagePanel"), // NOI18N
        new Item("MenuItemSendPacket", "jmri.jmrix.loconet.locogen.LocoGenPanel"), // NOI18N
        new Item("MenuItemPr3ModeSelect", "jmri.jmrix.loconet.pr3.swing.Pr3SelectPane"), // NOI18N
        null,
        new Item("MenuItemDownload", "jmri.jmrix.loconet.downloader.LoaderPane"), // NOI18N
        new Item("MenuItemSoundload", "jmri.jmrix.loconet.soundloader.LoaderPane"), // NOI18N
        new Item("MenuItemSoundEditor", "jmri.jmrix.loconet.soundloader.EditorPane") // NOI18N
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

package jmri.jmrix.loconet.swing;

import javax.swing.JMenu;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.jmrix.loconet.LnCommandStationType;

/**
 * Create a "Systems" menu containing the Jmri LocoNet-specific tools.
 *
 * @author Bob Jacobsen Copyright 2003, 2010
 */
public class LocoNetMenu extends JMenu {

    /**
     * Create a LocoNet menu. Preloads the TrafficController to certain actions.
     * Actions will open new windows.
     *
     * @param memo      {@link jmri.jmrix.loconet.LocoNetSystemConnectionMemo} to
     *                  be used by this object
     */
    public LocoNetMenu(LocoNetSystemConnectionMemo memo) {
        super();

        LnCommandStationType cmdStation = null;
        if (memo != null) {
            setText(memo.getUserName());
            cmdStation = memo.getSlotManager().getCommandStationType();
        } else {
            setText(Bundle.getMessage("MenuLocoNet"));
        }

        jmri.util.swing.WindowInterface wi = new jmri.util.swing.sdi.JmriJFrameInterface();

        boolean isLocoNetInterface;
        if ((cmdStation == null) ||
                (!cmdStation.equals(LnCommandStationType.COMMAND_STATION_PR2_ALONE) &&
                !cmdStation.equals(LnCommandStationType.COMMAND_STATION_PR3_ALONE) &&
                !cmdStation.equals(LnCommandStationType.COMMAND_STATION_PR4_ALONE) &&
                !cmdStation.equals(LnCommandStationType.COMMAND_STATION_USB_DCS240_ALONE) &&
                !cmdStation.equals(LnCommandStationType.COMMAND_STATION_USB_DCS52_ALONE))
                ) {
            isLocoNetInterface = true;
        } else {
            isLocoNetInterface = false;
        }

        /*
         * A local variable to help prevent a leading JSeparator and sequential
         * JSeparators.
         */
        boolean lastWasSeparator = true;

        for (Item item : panelItems) {
            if (item == null) {
                if (!lastWasSeparator) {
                    add(new javax.swing.JSeparator());
                    lastWasSeparator = true;
                }
            } else {
                if ((item.interfaceOnly == false) ||
                        isLocoNetInterface) {
                    add(new LnNamedPaneAction(Bundle.getMessage(item.name), wi, item.load, memo));
                    lastWasSeparator = false;
                }
            }
        }

        if (isLocoNetInterface) {
            add(new javax.swing.JSeparator());
            add(new jmri.jmrix.loconet.locormi.LnMessageServerAction(Bundle.getMessage("MenuItemStartLocoNetServer")));
            add(new jmri.jmrix.loconet.loconetovertcp.LnTcpServerAction(Bundle.getMessage("MenuItemLocoNetOverTCPServer")));
        }
    }

    Item[] panelItems = new Item[]{
        new Item("MenuItemLocoNetMonitor", "jmri.jmrix.loconet.locomon.LocoMonPane", false), // NOI18N
        new Item("MenuItemSlotMonitor", "jmri.jmrix.loconet.slotmon.SlotMonPane", false), // NOI18N
        new Item("MenuItemClockMon", "jmri.jmrix.loconet.clockmon.ClockMonPane", true), // NOI18N
        new Item("MenuItemLocoStats", "jmri.jmrix.loconet.locostats.swing.LocoStatsPanel", false), // NOI18N
        null,
        new Item("MenuItemBDL16Programmer", "jmri.jmrix.loconet.bdl16.BDL16Panel", true), // NOI18N
        new Item("MenuItemLocoIOProgrammer", "jmri.jmrix.loconet.locoio.LocoIOPanel", true), // NOI18N
        new Item("MenuItemPM4Programmer", "jmri.jmrix.loconet.pm4.PM4Panel", true), // NOI18N
        new Item("MenuItemSE8cProgrammer", "jmri.jmrix.loconet.se8.SE8Panel", true), // NOI18N
        new Item("MenuItemDS64Programmer", "jmri.jmrix.loconet.ds64.Ds64TabbedPanel", true), // NOI18N
        new Item("MenuItemCmdStnConfig", "jmri.jmrix.loconet.cmdstnconfig.CmdStnConfigPane",true), // NOI18N
        new Item("MenuItemSetID", "jmri.jmrix.loconet.locoid.LocoIdPanel", true), // NOI18N
        new Item("MenuItemDuplex", "jmri.jmrix.loconet.duplexgroup.swing.DuplexGroupTabbedPanel", true), // NOI18N
        null,
        new Item("MenuItemThrottleMessages", "jmri.jmrix.loconet.swing.throttlemsg.MessagePanel", true), // NOI18N
        new Item("MenuItemSendPacket", "jmri.jmrix.loconet.locogen.LocoGenPanel", false), // NOI18N
        new Item("MenuItemPr3ModeSelect", "jmri.jmrix.loconet.pr3.swing.Pr3SelectPane", false), // NOI18N
        null,
        new Item("MenuItemDownload", "jmri.jmrix.loconet.downloader.LoaderPane", false), // NOI18N
        new Item("MenuItemSoundload", "jmri.jmrix.loconet.soundloader.LoaderPane", false), // NOI18N
        new Item("MenuItemSoundEditor", "jmri.jmrix.loconet.soundloader.EditorPane", false) // NOI18N
    };

    static class Item {

        Item(String name, String load, boolean interfaceOnly) {
            this.name = name;
            this.load = load;
            this.interfaceOnly = interfaceOnly;
        }
        String name;
        String load;
        boolean interfaceOnly;
    }

}

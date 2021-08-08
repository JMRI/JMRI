package jmri.jmrix.loconet.swing;

import java.util.Iterator;

import javax.swing.JMenu;

import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.jmrix.loconet.LnCommandStationType;
import jmri.jmrix.loconet.swing.spi.LnMenuItemExtension;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create a "Systems" menu containing the Jmri LocoNet-specific tools.
 *
 * @author Bob Jacobsen Copyright 2003, 2010
 * @author B. Milhaupt Copyright 2021
 */
public class LocoNetMenu extends JMenu {
    java.util.ArrayList<LnMenuItem> panelItems = new java.util.ArrayList<>();

    /**
     * Create a LocoNet menu. Preloads the TrafficController to certain actions.
     * Actions will open new windows.
     *
     * @param memo      {@link jmri.jmrix.loconet.LocoNetSystemConnectionMemo} to
     *                  be used by this object
     */
    public LocoNetMenu(LocoNetSystemConnectionMemo memo) {
        super();

        panelItems.add(new LnMenuItem("MenuItemLocoNetMonitor",
                "jmri.jmrix.loconet.locomon.LocoMonPane", false)); // NOI18N
        panelItems.add(new LnMenuItem("MenuItemSlotMonitor", "jmri.jmrix.loconet.slotmon.SlotMonPane", false)); // NOI18N
        panelItems.add(new LnMenuItem("MenuItemClockMon", "jmri.jmrix.loconet.clockmon.ClockMonPane", true)); // NOI18N
        panelItems.add(new LnMenuItem("MenuItemLocoStats", "jmri.jmrix.loconet.locostats.swing.LocoStatsPanel", false)); // NOI18N
        panelItems.add(null);
        panelItems.add(new LnMenuItem("MenuItemBDL16Programmer", "jmri.jmrix.loconet.bdl16.BDL16Panel", true)); // NOI18N
        panelItems.add(new LnMenuItem("MenuItemPM4Programmer", "jmri.jmrix.loconet.pm4.PM4Panel", true)); // NOI18N
        panelItems.add(new LnMenuItem("MenuItemSE8cProgrammer", "jmri.jmrix.loconet.se8.SE8Panel", true)); // NOI18N
        panelItems.add(new LnMenuItem("MenuItemDS64Programmer", "jmri.jmrix.loconet.ds64.Ds64TabbedPanel", true)); // NOI18N
        panelItems.add(new LnMenuItem("MenuItemCmdStnConfig", "jmri.jmrix.loconet.cmdstnconfig.CmdStnConfigPane",true)); // NOI18N
        panelItems.add(new LnMenuItem("MenuItemSetID", "jmri.jmrix.loconet.locoid.LocoIdPanel", true)); // NOI18N
        panelItems.add(new LnMenuItem("MenuItemDuplex", "jmri.jmrix.loconet.duplexgroup.swing.DuplexGroupTabbedPanel", true)); // NOI18N
        panelItems.add(null);
        panelItems.add(new LnMenuItem("MenuItemThrottleMessages", "jmri.jmrix.loconet.swing.throttlemsg.MessagePanel", true)); // NOI18N
        panelItems.add(new LnMenuItem("MenuItemSendPacket", "jmri.jmrix.loconet.locogen.LocoGenPanel", false)); // NOI18N
        panelItems.add(new LnMenuItem("MenuItemLncvProg", "jmri.jmrix.loconet.swing.lncvprog.LncvProgPane", true)); // NOI18N
        panelItems.add(new LnMenuItem("MenuItemPr3ModeSelect", "jmri.jmrix.loconet.pr3.swing.Pr3SelectPane", false)); // NOI18N
        panelItems.add(null);
        panelItems.add(new LnMenuItem("MenuItemDownload", "jmri.jmrix.loconet.downloader.LoaderPane", false)); // NOI18N
        panelItems.add(new LnMenuItem("MenuItemSoundload", "jmri.jmrix.loconet.soundloader.LoaderPane", false)); // NOI18N
        panelItems.add(new LnMenuItem("MenuItemSoundEditor", "jmri.jmrix.loconet.soundloader.EditorPane", false)); // NOI18N

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

        for (LnMenuItem item : panelItems) {
            if (item == null) {
                if (!lastWasSeparator) {
                    add(new javax.swing.JSeparator());
                    lastWasSeparator = true;
                }
            } else {
                if ((!item.getRequiresAccessToLocoNet()) ||
                        isLocoNetInterface) {
                    try {
                        add(new LnNamedPaneAction(Bundle.getMessage(item.getMenuItemName()), wi, item.getClassToLoad(), memo));
                    } catch (java.util.MissingResourceException e) {
                        add(new LnNamedPaneAction(item.getMenuItemName(), wi, item.getClassToLoad(), memo));

                    }
                    lastWasSeparator = false;
                }
            }
        }
        
        // Special-case for those menu item actions which do not include GUI functionality
        if (isLocoNetInterface) {
            add(new javax.swing.JSeparator());
            add(new jmri.jmrix.loconet.locormi.LnMessageServerAction(Bundle.getMessage("MenuItemStartLocoNetServer")));
            add(new jmri.jmrix.loconet.loconetovertcp.LnTcpServerAction(Bundle.getMessage("MenuItemLocoNetOverTCPServer")));
        }

        // Handle menu items from SPI "service providers"
        panelItems.clear();
        // add anything from SPI extensions, as appropriate
        Iterator<LnMenuItem> itemsIterator = LnMenuItemExtensionService.getInstance()
                .getExtensionLnMenuItems(java.util.Locale.getDefault()).iterator();

        boolean moreToAdd = false;
        while (itemsIterator != null && itemsIterator.hasNext()) {
            panelItems.add(itemsIterator.next());
            moreToAdd = true;
        }

        if (moreToAdd) {
            add(new javax.swing.JSeparator());
            lastWasSeparator = true;
            log.debug("Adding menu item(s) from SPI extension(s)");
        }

        for (LnMenuItem item : panelItems) {
            if (item == null) {
                if (!lastWasSeparator) {
                    add(new javax.swing.JSeparator());
                    lastWasSeparator = true;
                }
            } else {
                if ((!item.getRequiresAccessToLocoNet()) ||
                        isLocoNetInterface) {
                    add(new LnNamedPaneAction(item.getMenuItemName(), wi, item.getClassToLoad(), memo));
                    lastWasSeparator = false;
                }
            }
        }

    }
    private static final Logger log = LoggerFactory.getLogger(LocoNetMenu.class);

}
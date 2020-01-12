package jmri.jmrix.nce.swing;

import java.util.ResourceBundle;
import javax.annotation.Nonnull;
import javax.swing.JMenu;
import jmri.jmrix.nce.NceSystemConnectionMemo;
import jmri.jmrix.nce.NceTrafficController;

/**
 * Create a "Systems" menu containing the JMRI NCE-specific tools.
 *
 * @author Bob Jacobsen Copyright 2003, 2010 converted to multiple connection
 * @author kcameron Copyright 2010, 2013
 */
public class NceMenu extends JMenu {

    /**
     * Create an NCE menu and load the NceSystemConnectionMemo to the various
     * actions. Actions will open new windows.
     *
     * @param memo the system connection memo to associate menu items with
     */
    // TODO Need to Sort out the NCE server menu items
    public NceMenu(@Nonnull NceSystemConnectionMemo memo) {
        super();

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.JmrixSystemsBundle");

        setText(memo.getUserName());

        jmri.util.swing.WindowInterface wi = new jmri.util.swing.sdi.JmriJFrameInterface();

        for (Item item : panelItems) {
            if (item == null) {
                add(new javax.swing.JSeparator());
            } else {
                NceNamedPaneAction a = new NceNamedPaneAction(rb.getString(item.name), wi, item.load, memo);
                add(a);
                if ((item.enable & memo.getNceCmdGroups()) != 0) {
                    a.setEnabled(true);
                } else {
                    a.setEnabled(false);
                }
            }
        }

        // do we have an NceTrafficController?
        setEnabled(memo.getNceTrafficController() != null); // disable menu, no connection, no tools!

        add(new javax.swing.JSeparator());
    }

    private Item[] panelItems = new Item[]{
        new Item("MenuItemCommandMonitor", "jmri.jmrix.nce.ncemon.NceMonPanel", NceTrafficController.CMDS_ALL_SYS),
        new Item("MenuItemSendCommand", "jmri.jmrix.nce.packetgen.NcePacketGenPanel", NceTrafficController.CMDS_ALL_SYS),
        new Item("MenuItemMacroCommand", "jmri.jmrix.nce.macro.NceMacroGenPanel", NceTrafficController.CMDS_ALL_SYS),
        new Item("MenuItemMacroEdit", "jmri.jmrix.nce.macro.NceMacroEditPanel", NceTrafficController.CMDS_MEM),
        new Item("MenuItemConsistEdit", "jmri.jmrix.nce.consist.NceConsistEditPanel", NceTrafficController.CMDS_MEM),
        new Item("MenuItemTrackPacketMonitor", "jmri.jmrix.ncemonitor.NcePacketMonitorPanel", NceTrafficController.CMDS_ALL_SYS),
        new Item("MenuItemClockMon", "jmri.jmrix.nce.clockmon.ClockMonPanel", NceTrafficController.CMDS_CLOCK),
        new Item("MenuItemShowCabs", "jmri.jmrix.nce.cab.NceShowCabPanel", NceTrafficController.CMDS_MEM),
        new Item("MenuItemBoosterProg", "jmri.jmrix.nce.boosterprog.BoosterProgPanel", NceTrafficController.CMDS_NOT_USB),
        new Item("MenuItemUsbInt", "jmri.jmrix.nce.usbinterface.UsbInterfacePanel", NceTrafficController.CMDS_USB)
    };

    static class Item {

        Item(String name, String load, long enable) {
            this.name = name;
            this.load = load;
            this.enable = enable;
        }
        String name;
        String load;
        long enable;
    }

}

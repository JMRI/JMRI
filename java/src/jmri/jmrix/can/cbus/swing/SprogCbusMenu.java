package jmri.jmrix.can.cbus.swing;

import javax.swing.JMenu;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.ConfigurationManager;
import jmri.jmrix.can.swing.CanNamedPaneAction;

/**
 * Create a menu containing the Jmri CAN- and CBUS-specific tools
 *
 * @author Bob Jacobsen Copyright 2003, 2008, 2009
 * @author Andrew Crosland 2008, 2020
 */
public class SprogCbusMenu extends JMenu {

    Item[] panelItems;
    
    public SprogCbusMenu(CanSystemConnectionMemo memo) {
        super();

        if (memo != null) {
            setText(memo.getUserName());
        } else {
            setText(Bundle.getMessage("MenuItemCBUS"));
        }

        jmri.util.swing.WindowInterface wi = new jmri.util.swing.sdi.JmriJFrameInterface();
        
        if (memo != null) {
            panelItems = createPanelItems(memo);
        }
        
        for (Item item : panelItems) {
            if (item == null) {
                add(new javax.swing.JSeparator());
            } else {
                add(new CanNamedPaneAction(Bundle.getMessage(item.name), wi, item.load, memo));
            }
        }
        
        // Not a CanNamedPane
        add(new javax.swing.JSeparator());
        if (memo != null) {
            if (!memo.getProgModeSwitch().equals(ConfigurationManager.ProgModeSwitch.NONE)) {
                // Hardware supports programming mode switching
                add(new jmri.jmrix.can.cbus.swing.modeswitcher.SprogCbusModeSwitcherAction(Bundle.getMessage("MenuItemSPROGModeSwitcher"), memo));
            }
        }
        // Added tools that can normally only be found in PanelPro tools menu so that they are available from DP3
        add(new javax.swing.JSeparator());
        add(new jmri.jmrit.swing.meter.MeterAction());

    }

    private Item[] createPanelItems(CanSystemConnectionMemo memo) {
        if (memo.getSubProtocol().equals(ConfigurationManager.SubProtocol.CBUS)) {
            return new Item[]{
                new Item("MenuItemConsole", "jmri.jmrix.can.cbus.swing.console.CbusConsolePane"),
                new Item("MenuItemSendFrame", "jmri.jmrix.can.swing.send.CanSendPane"),
                new Item("MenuItemEventCapture", "jmri.jmrix.can.cbus.swing.configtool.ConfigToolPane"),
                new Item("MenuItemEventTable", "jmri.jmrix.can.cbus.swing.eventtable.CbusEventTablePane"),
                new Item("MenuItemNodeConfig", "jmri.jmrix.can.cbus.swing.nodeconfig.NodeConfigToolPane"),
                new Item("MenuItemCbusSlotMonitor", "jmri.jmrix.can.cbus.swing.cbusslotmonitor.CbusSlotMonitorPane"),
                new Item("MenuItemEvRequestMon", "jmri.jmrix.can.cbus.swing.eventrequestmonitor.CbusEventRequestTablePane"),
                new Item("MenuItemNetworkSim", "jmri.jmrix.can.cbus.swing.simulator.SimulatorPane"),
                new Item("MenuItemBootloader", "jmri.jmrix.can.cbus.swing.bootloader.CbusBootloaderPane")
            };
        } else {
            return new Item[]{
                new Item("MenuItemSPROGConsole", "jmri.jmrix.can.cbus.swing.console.CbusConsolePane"),
                new Item("MenuItemSendFrame", "jmri.jmrix.can.swing.send.CanSendPane"),
                new Item("MenuItemSPROGNodeConfig", "jmri.jmrix.can.cbus.swing.nodeconfig.NodeConfigToolPane"),
                new Item("MenuItemSPROGCbusSlotMonitor", "jmri.jmrix.can.cbus.swing.cbusslotmonitor.CbusSlotMonitorPane"),
                new Item("MenuItemSPROGBootloader", "jmri.jmrix.can.cbus.swing.bootloader.CbusBootloaderPane")
            };
        }
    }
    
    static class Item {

        Item(String name, String load) {
            this.name = name;
            this.load = load;
        }

        String name;
        String load;
    }

}

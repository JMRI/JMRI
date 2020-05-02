package jmri.jmrix.can.cbus.swing;

import javax.swing.JMenu;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.swing.CanNamedPaneAction;

/**
 * Create a menu containing the Jmri CAN- and CBUS-specific tools
 *
 * @author Bob Jacobsen Copyright 2003, 2008, 2009
 * @author Andrew Crosland 2008, 2020
 */
public class SprogCbusMenu extends JMenu {

    public SprogCbusMenu(CanSystemConnectionMemo memo) {
        super();

        if (memo != null) {
            setText(memo.getUserName());
        } else {
            setText(Bundle.getMessage("MenuItemCBUS"));
        }

        jmri.util.swing.WindowInterface wi = new jmri.util.swing.sdi.JmriJFrameInterface();

        for (Item item : panelItems) {
            if (item == null) {
                add(new javax.swing.JSeparator());
            } else {
                add(new CanNamedPaneAction(Bundle.getMessage(item.name), wi, item.load, memo));
            }
        }
        
        // Added tools that can normally only be found in PanelPro tools menu so that they are available from DP3
        add(new javax.swing.JSeparator());
        add(new jmri.jmrit.ampmeter.AmpMeterAction(Bundle.getMessage("MenuItemAmpMeter")));
        add(new jmri.jmrit.voltmeter.VoltMeterAction(Bundle.getMessage("MenuItemVoltMeter")));

    }

    Item[] panelItems = new Item[]{
        new Item("MenuItemSPROGConsole", "jmri.jmrix.can.cbus.swing.console.CbusConsolePane"),
        new Item("MenuItemSPROGNodeConfig", "jmri.jmrix.can.cbus.swing.nodeconfig.NodeConfigToolPane"),
        new Item("MenuItemSPROGCbusSlotMonitor", "jmri.jmrix.can.cbus.swing.cbusslotmonitor.CbusSlotMonitorPane"),
        new Item("MenuItemSPROGBootloader", "jmri.jmrix.can.cbus.swing.bootloader.CbusBootloaderPane")
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

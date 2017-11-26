package jmri.jmrix.can.nmranet.swing;

import javax.swing.JMenu;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.swing.CanNamedPaneAction;

/**
 * Create a menu containing the Jmri CAN- and CBUS-specific tools for NMRAnet
 *
 * @author Bob Jacobsen Copyright 2003, 2008, 2009
 * @author Andrew Crosland 2008
 */
public class NmraNetMenu extends JMenu {

    public NmraNetMenu(CanSystemConnectionMemo memo) {
        super();

        if (memo != null) {
            setText(memo.getUserName());
        } else {
            setText(Bundle.getMessage("NMRAnet"));
        }

        jmri.util.swing.WindowInterface wi = new jmri.util.swing.sdi.JmriJFrameInterface();

        for (Item item : panelItems) {
            if (item == null) {
                add(new javax.swing.JSeparator());
            } else {
                add(new CanNamedPaneAction(Bundle.getMessage(item.name), wi, item.load, memo));
            }
        }
    }

    Item[] panelItems = new Item[]{
        new Item("MenuItemConsole", "jmri.jmrix.can.swing.monitor.MonitorPane"),
        new Item("MenuItemSendFrame", "jmri.jmrix.can.swing.send.CanSendPane"),};

    static class Item {

        Item(String name, String load) {
            this.name = name;
            this.load = load;
        }

        String name;
        String load;
    }

}

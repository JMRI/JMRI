// CbusMenu.java
package jmri.jmrix.can.cbus.swing;

import java.util.ResourceBundle;
import javax.swing.JMenu;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.swing.CanNamedPaneAction;

/**
 * Create a menu containing the Jmri CAN- and CBUS-specific tools
 *
 * @author	Bob Jacobsen Copyright 2003, 2008, 2009
 * @author Andrew Crosland 2008
 * @version $Revision: 17977 $
 */
public class CbusMenu extends JMenu {

    /**
     *
     */
    private static final long serialVersionUID = -8901183972160758403L;

    public CbusMenu(CanSystemConnectionMemo memo) {
        super();

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.can.cbus.CbusBundle");

        String title;
        if (memo != null) {
            title = memo.getUserName();
        } else {
            title = rb.getString("MenuItemCBUS");
        }

        setText(title);

        jmri.util.swing.WindowInterface wi = new jmri.util.swing.sdi.JmriJFrameInterface();

        for (Item item : panelItems) {
            if (item == null) {
                add(new javax.swing.JSeparator());
            } else {
                add(new CanNamedPaneAction(rb.getString(item.name), wi, item.load, memo));
            }
        }

    }

    Item[] panelItems = new Item[]{
        new Item("MenuItemConsole", "jmri.jmrix.can.cbus.swing.console.CbusConsolePane"),
        new Item("MenuItemSendFrame", "jmri.jmrix.can.swing.send.CanSendPane"),
        new Item("MenuItemEventCapture", "jmri.jmrix.can.cbus.swing.configtool.ConfigToolPane"),
        new Item("MenuItemEventTable", "jmri.jmrix.can.cbus.swing.eventtable.CbusEventTablePane"),
        new Item("MenuItemNodeConfig", "jmri.jmrix.can.cbus.swing.nodeconfig.NodeConfigToolPane")
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

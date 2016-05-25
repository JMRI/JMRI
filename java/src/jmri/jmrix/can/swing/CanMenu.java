// CbusMenu.java
package jmri.jmrix.can.swing;

import java.util.ResourceBundle;
import javax.swing.JMenu;
import jmri.jmrix.can.CanSystemConnectionMemo;

/**
 * Create a menu containing the Jmri CAN- and CBUS-specific tools
 *
 * @author	Bob Jacobsen Copyright 2003, 2008, 2009
 * @author Andrew Crosland 2008
 * @version $Revision: 17977 $
 */
public class CanMenu extends JMenu {

    /**
     *
     */
    private static final long serialVersionUID = 892271699607148161L;

    public CanMenu(CanSystemConnectionMemo memo) {
        super();

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.JmrixSystemsBundle");

        String title;
        if (memo != null) {
            title = memo.getUserName();
        } else {
            title = rb.getString("MenuItemCAN");
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

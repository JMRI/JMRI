package jmri.jmrix.rfid.swing;

import java.util.ResourceBundle;
import javax.swing.JMenu;
import javax.swing.JSeparator;
import jmri.jmrix.rfid.RfidSystemConnectionMemo;
import jmri.util.swing.WindowInterface;
import jmri.util.swing.sdi.JmriJFrameInterface;

/**
 * Create a "Systems" menu containing the Jmri RFID-specific tools.
 *
 * @author	Bob Jacobsen Copyright 2003, 2006, 2007, 2008
 * @author Matthew Harris Copyright 2011
 * @since 2.11.4
 */
public class RfidMenu extends JMenu {

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public RfidMenu(RfidSystemConnectionMemo memo) {

        super();

        if (memo != null) {
            setText(memo.getUserName());
        } else {
            setText(Bundle.getMessage("MenuSystem"));
        }

        WindowInterface wi = new JmriJFrameInterface();

        for (Item item : panelItems) {
            if (item == null) {
                add(new JSeparator());
            } else {
                add(new RfidNamedPaneAction(item.name, wi, item.load, memo)); // NOI18N
            }
        }
    }

    Item[] panelItems = new Item[]{
        new Item(Bundle.getMessage("MonitorXTitle", "RFID"), "jmri.jmrix.rfid.swing.serialmon.SerialMonPane")
    };

    static class Item {

        String name;
        String load;

        Item(String name, String load) {
            this.name = name;
            this.load = load;
        }
    }

}

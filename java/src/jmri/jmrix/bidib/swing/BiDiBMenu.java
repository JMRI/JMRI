package jmri.jmrix.bidib.swing;

import javax.swing.JMenu;
import javax.swing.JSeparator;
import jmri.jmrix.bidib.BiDiBSystemConnectionMemo;
import jmri.util.swing.WindowInterface;
import jmri.util.swing.sdi.JmriJFrameInterface;

/**
 * Create a "Systems" menu containing the Jmri BiDiB-specific tools.
 *
 * @author  Bob Jacobsen Copyright 2003, 2006, 2007, 2008
 * @author Matthew Harris Copyright 2011
 * @since 2.11.4
 * @author Eckart Meyer Copyright (C) 2020-2023
 */
public class BiDiBMenu extends JMenu {

//    @SuppressWarnings("OverridableMethodCallInConstructor")
    public BiDiBMenu(BiDiBSystemConnectionMemo memo) {

        super();

        if (memo != null) {
            setText(memo.getUserName());
        } else {
            setText(Bundle.getMessage("MenuBiDiB"));
        }

        WindowInterface wi = new JmriJFrameInterface();

        for (Item item : panelItems) {
            if (item == null || memo == null) {
                add(new JSeparator());
            } else {
                add(new BiDiBNamedPaneAction(item.name, wi, item.load, memo)); // NOI18N
            }
        }
        add(new javax.swing.JSeparator());
        add(new jmri.jmrix.bidib.tcpserver.TcpServerAction(memo, Bundle.getMessage("MenuItemStartBiDiBOverTCPServer"), Bundle.getMessage("MenuItemStopBiDiBOverTCPServer")));
    }

    Item[] panelItems = new Item[]{
        new Item(Bundle.getMessage("BiDiBMonPaneTitle"), "jmri.jmrix.bidib.swing.mon.BiDiBMonPane")
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

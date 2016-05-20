// OpenLcbMenu.java

package jmri.jmrix.openlcb;
import jmri.jmrix.can.swing.CanNamedPaneAction;

import java.util.ResourceBundle;

import javax.swing.JMenu;

/**
 * Create a menu containing the JMRI OpenLCB-specific tools
 *
 * @author	Bob Jacobsen   Copyright 2010
 * @version     $Revision$
 */
public class OpenLcbMenu extends JMenu {
    public OpenLcbMenu(jmri.jmrix.can.CanSystemConnectionMemo memo) {
        super();
        
        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.JmrixSystemsBundle");
        
        String title;
        if (memo != null)
            title = memo.getUserName();
        else
            title = rb.getString("MenuItemCAN");
        
            setText(title);
        
        jmri.util.swing.WindowInterface wi = new jmri.util.swing.sdi.JmriJFrameInterface();
        
        for (Item item : panelItems) {
            if (item == null) {
                add(new javax.swing.JSeparator());
            } else {
                add(new CanNamedPaneAction( rb.getString(item.name), wi, item.load, memo));
            }
        }
    }
    
        Item[] panelItems = new Item[] {
            new Item("MenuItemTrafficMonitor", "jmri.jmrix.openlcb.swing.monitor.MonitorPane"),      // NOI18N
            new Item("MenuItemSendFrame",   "jmri.jmrix.openlcb.swing.send.OpenLcbCanSendPane"),     // NOI18N
            new Item("MenuItemConfigNodes", "jmri.jmrix.openlcb.swing.networktree.NetworkTreePane"), // NOI18N
            new Item("MenuItemStartHub", "jmri.jmrix.openlcb.swing.hub.HubPane"),                    // NOI18N
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



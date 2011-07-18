// WangrowMenu.java

package jmri.jmrix.wangrow;

import java.util.ResourceBundle;

import jmri.jmrix.nce.NceSystemConnectionMemo;
import jmri.jmrix.nce.swing.NceNamedPaneAction;

import javax.swing.JMenu;

/**
 * Create a "Systems" menu containing the Jmri NCE-specific tools.
 * <P>
 * Note that this is still using specific tools from the
 * {@link jmri.jmrix.nce} package.
 *
 * @author	Bob Jacobsen   Copyright 2003
 * @version     $Revision$
 */
public class WangrowMenu extends JMenu {
	
	NceSystemConnectionMemo memo = null;
	
    public WangrowMenu(NceSystemConnectionMemo m) {

        super();
        this.memo = m;

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.JmrixSystemsBundle");
        
        if (memo != null)
            setText(memo.getUserName());
        else
            setText(rb.getString("MenuWangrow"));

        jmri.util.swing.WindowInterface wi = new jmri.util.swing.sdi.JmriJFrameInterface();
        
        for (Item item : panelItems) {
            if (item == null) {
                add(new javax.swing.JSeparator());
            } else {
                add(new NceNamedPaneAction( rb.getString(item.name), wi, item.load, memo));
            }
        }
        add(new javax.swing.JSeparator());

        //setText(rb.getString("MenuItemWangrow"));

        //add(new jmri.jmrix.nce.ncemon.NceMonAction(rb.getString("MenuItemCommandMonitor")));
        //add(new jmri.jmrix.nce.packetgen.NcePacketGenAction(rb.getString("MenuItemSendCommand")));
    }
    
    private Item[] panelItems = new Item[] {
        new Item("MenuItemCommandMonitor", "jmri.jmrix.nce.ncemon.NceMonPanel"),
        new Item("MenuItemSendCommand", "jmri.jmrix.nce.packetgen.NcePacketGenPanel")
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



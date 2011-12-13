// NceMenu.java

package jmri.jmrix.nce.swing;

import java.util.ResourceBundle;
import javax.swing.*;
import jmri.jmrix.nce.NceSystemConnectionMemo;
import jmri.jmrix.nce.NceTrafficController;

/**
 * Create a "Systems" menu containing the JMRI NCE-specific tools.
 *
 * @author	Bob Jacobsen   Copyright 2003, 2010
 * converted to multiple connection
 * @author	kcameron	Copyright 2010
 * @version     $Revision$
 */

public class NceMenu extends JMenu {

    /**
     * Create a NCE menu.
     * And loads the NceSystemConnectionMemo to the various actions.
     * Actions will open new windows.
     */
    // Need to Sort out the NCE server menu items;
    public NceMenu(NceSystemConnectionMemo memo) {
        super();

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.JmrixSystemsBundle");

        // memo can not be null!
        if (memo == null){
        	new Exception().printStackTrace();
        	return;
        }        	
            
        setText(memo.getUserName());
            
        jmri.util.swing.WindowInterface wi = new jmri.util.swing.sdi.JmriJFrameInterface();
        
        for (Item item : panelItems) {
            if (item == null) {
                add(new javax.swing.JSeparator());
            } else {
            	NceNamedPaneAction a = new NceNamedPaneAction( rb.getString(item.name), wi, item.load, memo);
                add(a);
                a.setEnabled(item.enable.equals(ALL) || (item.enable.equals(PH) && memo.getNceUSB() == NceTrafficController.USB_SYSTEM_NONE));               	
            }
        }
        
        // do we have a NceTrafficController?
        setEnabled(memo.getNceTrafficController() != null);	// disable menu, no connection, no tools!
        
        add(new javax.swing.JSeparator());
    }
    
    // Enable or disable menu items based on system connection
    private static final String ALL = "All NCE connections";
    private static final String PH = "NCE Power House Only";
        
    private Item[] panelItems = new Item[] {
        new Item("MenuItemCommandMonitor", "jmri.jmrix.nce.ncemon.NceMonPanel", ALL),
        new Item("MenuItemSendCommand", "jmri.jmrix.nce.packetgen.NcePacketGenPanel", ALL),
        new Item("MenuItemMacroCommand", "jmri.jmrix.nce.macro.NceMacroGenPanel", ALL),
        new Item("MenuItemMacroEdit", "jmri.jmrix.nce.macro.NceMacroEditPanel", PH),
        new Item("MenuItemConsistEdit", "jmri.jmrix.nce.consist.NceConsistEditPanel", PH),
        new Item("MenuItemTrackPacketMonitor", "jmri.jmrix.ncemonitor.NcePacketMonitorPanel", ALL),
        new Item("MenuItemClockMon", "jmri.jmrix.nce.clockmon.ClockMonPanel", PH),
        new Item("MenuItemShowCabs", "jmri.jmrix.nce.cab.NceShowCabPanel", PH),
        new Item("MenuItemBoosterProg", "jmri.jmrix.nce.boosterprog.BoosterProgPanel", ALL)
    };
    
    static class Item {
        Item(String name, String load, String enable) {
            this.name = name;
            this.load = load;
            this.enable = enable;
        }
        String name;
        String load;
        String enable;
    }
}

/* @(#)NceMenu.java */
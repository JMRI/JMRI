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
	 * 
	 */
	private static final long serialVersionUID = 7065069034402845776L;

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
                if ((item.enable & memo.getNceCmdGroups()) != 0) {
                    a.setEnabled(true);  
                } else {
                    a.setEnabled(false);  
                }             	
            }
        }
        
        // do we have a NceTrafficController?
        setEnabled(memo.getNceTrafficController() != null);	// disable menu, no connection, no tools!
        
        add(new javax.swing.JSeparator());
    }

    private Item[] panelItems = new Item[] {
        new Item("MenuItemCommandMonitor", "jmri.jmrix.nce.ncemon.NceMonPanel", NceTrafficController.CMDS_ALL_SYS),
        new Item("MenuItemSendCommand", "jmri.jmrix.nce.packetgen.NcePacketGenPanel", NceTrafficController.CMDS_ALL_SYS),
        new Item("MenuItemMacroCommand", "jmri.jmrix.nce.macro.NceMacroGenPanel", NceTrafficController.CMDS_ALL_SYS),
        new Item("MenuItemMacroEdit", "jmri.jmrix.nce.macro.NceMacroEditPanel", NceTrafficController.CMDS_NOT_USB),
        new Item("MenuItemConsistEdit", "jmri.jmrix.nce.consist.NceConsistEditPanel", NceTrafficController.CMDS_NOT_USB),
        new Item("MenuItemTrackPacketMonitor", "jmri.jmrix.ncemonitor.NcePacketMonitorPanel", NceTrafficController.CMDS_ALL_SYS),
        new Item("MenuItemClockMon", "jmri.jmrix.nce.clockmon.ClockMonPanel", NceTrafficController.CMDS_CLOCK),
        new Item("MenuItemShowCabs", "jmri.jmrix.nce.cab.NceShowCabPanel", NceTrafficController.CMDS_MEM),
        new Item("MenuItemBoosterProg", "jmri.jmrix.nce.boosterprog.BoosterProgPanel", NceTrafficController.CMDS_NOT_USB),
        new Item("MenuItemUsbInt", "jmri.jmrix.nce.usbinterface.UsbInterfacePanel", NceTrafficController.CMDS_USB)
    };
    
    static class Item {
        Item(String name, String load, long enable) {
            this.name = name;
            this.load = load;
            this.enable = enable;
        }
        String name;
        String load;
        long enable;
    }
}

/* @(#)NceMenu.java */
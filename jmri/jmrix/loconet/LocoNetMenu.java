// LocoNetMenu.java

package jmri.jmrix.loconet;

import java.util.ResourceBundle;

import javax.swing.JMenu;

/**
 * Create a "Systems" menu containing the Jmri LocoNet-specific tools.
 *
 * @author	Bob Jacobsen   Copyright 2003
 * @version     $Revision: 1.20 $
 */
public class LocoNetMenu extends JMenu {

    /**
     * Create a LocoNet menu.
     * Preloads the TrafficController to certain actions
     */
    public LocoNetMenu(LocoNetSystemConnectionMemo memo) {
        super();

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.loconet.LocoNetBundle");

        setText(memo.getUserName());

        add(new jmri.jmrix.loconet.locomon.LocoMonAction(rb.getString("MenuItemLocoNetMonitor"), 
                memo.getLnTrafficController()));
        add(new jmri.jmrix.loconet.slotmon.SlotMonAction(rb.getString("MenuItemSlotMonitor")));
        add(new jmri.jmrix.loconet.clockmon.ClockMonAction( rb.getString("MenuItemClockMon")));
        add(new jmri.jmrix.loconet.locostats.LocoStatsAction( rb.getString("MenuItemLocoStats"),
                memo.getLnTrafficController()));
        

        add(new javax.swing.JSeparator());
        
        add(new jmri.jmrix.loconet.bdl16.BDL16Action(rb.getString("MenuItemBDL16Programmer")));
        add(new jmri.jmrix.loconet.locoio.LocoIOAction(rb.getString("MenuItemLocoIOProgrammer")));
        add(new jmri.jmrix.loconet.pm4.PM4Action(rb.getString("MenuItemPM4Programmer")));
        add(new jmri.jmrix.loconet.se8.SE8Action(rb.getString("MenuItemSE8cProgrammer")));
        add(new jmri.jmrix.loconet.ds64.DS64Action(rb.getString("MenuItemDS64Programmer")));
        add(new jmri.jmrix.loconet.cmdstnconfig.CmdStnConfigAction( rb.getString("MenuItemCmdStnConfig")));
        add(new jmri.jmrix.loconet.locoid.LocoIdAction( rb.getString("MenuItemSetID")));
        
        add(new javax.swing.JSeparator());
        

        add(new jmri.jmrix.loconet.locormi.LnMessageServerAction( rb.getString("MenuItemStartLocoNetServer")));
        add(new jmri.jmrix.loconet.loconetovertcp.ServerAction( rb.getString("MenuItemLocoNetOverTCPServer"))) ;

        add(new javax.swing.JSeparator());
        
        add(new jmri.jmrix.loconet.swing.throttlemsg.MessageFrameAction( rb.getString("MenuItemThrottleMessages")));
        add(new jmri.jmrix.loconet.locogen.LocoGenAction(rb.getString("MenuItemSendPacket")));
        add(new jmri.jmrix.loconet.downloader.LoaderPanelAction( rb.getString("MenuItemDownload")));
        add(new jmri.jmrix.loconet.soundloader.LoaderPanelAction( rb.getString("MenuItemSoundload")));
        add(new jmri.jmrix.loconet.soundloader.EditorFrameAction( rb.getString("MenuItemSoundEditor")));

        add(new javax.swing.JSeparator());
        add(new jmri.jmrix.loconet.pr3.swing.Pr3SelectAction( rb.getString("MenuItemPr3ModeSelect")));

    }
}



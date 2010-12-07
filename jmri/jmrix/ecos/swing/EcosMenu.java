package jmri.jmrix.ecos.swing;

import java.util.ResourceBundle;
import javax.swing.*;

import jmri.jmrix.ecos.EcosSystemConnectionMemo;
/**
 *
 * @author Kevin Dickerson
 */
public class EcosMenu extends JMenu{

        public EcosMenu(EcosSystemConnectionMemo memo) {
        super();

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.ecos.EcosBundle");

        if (memo != null)
            setText(memo.getUserName());
        else
            setText(rb.getString("MenuEcos"));

        jmri.util.swing.WindowInterface wi = new jmri.util.swing.sdi.JmriJFrameInterface();
        
        for (Item item : panelItems) {
            if (item == null) {
                add(new javax.swing.JSeparator());
            } else {
                add(new EcosNamedPaneAction( rb.getString(item.name), wi, item.load, memo));
            }
        }
        
        if (jmri.InstanceManager.getDefault(jmri.jmrit.beantable.ListedTableFrame.class)==null){
            new jmri.jmrit.beantable.ListedTableFrame();
        }

        add(new jmri.jmrit.beantable.ListedTableAction("ECoS Loco Database", "jmri.jmrix.ecos.swing.locodatabase.EcosLocoTableTabAction"));
        add(new apps.gui3.TabbedPreferencesAction("ECoS Preferences", "ECOS", memo.getUserName()));
        add(new jmri.jmrix.ecos.utilities.AddRosterEntryToEcos("Add Roster Entry to ECoS", memo));
        


    }
    
        Item[] panelItems = new Item[] {
            new Item("MenuItemEcosMonitor", "jmri.jmrix.ecos.swing.monitor.EcosMonPane"),
            new Item("MenuItemSendPacket",  "jmri.jmrix.ecos.swing.packetgen.PacketGenPanel"),
            new Item("MenuItemInfo",        "jmri.jmrix.ecos.swing.statusframe.StatusPanel")
        
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

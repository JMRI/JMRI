// Menu.java

package jmri.jmrix.ecos;

import java.util.ResourceBundle;
import javax.swing.*;

/**
 * Create a "Systems" menu containing the Jmri ECOS-specific tools.
 *
 * @author	Bob Jacobsen   Copyright 2003, 2008
 * @version     $Revision: 1.4 $
 */
public class Menu extends JMenu {
    public Menu(String name) {
        this();
        setText(name);
    }

    Action prefsAction;
    
    public Menu() {

        super();

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.JmrixSystemsBundle");

        setText(rb.getString("MenuItemEcos"));

        
        add(new jmri.jmrix.ecos.swing.monitor.MonAction(rb.getString("MenuItemCommandMonitor")));
        add(new jmri.jmrix.ecos.swing.packetgen.PacketGenAction(rb.getString("MenuItemSendCommand")));
        add(new jmri.jmrix.ecos.swing.statusframe.StatusFrameAction("ECoS Info"));
        if (jmri.InstanceManager.getDefault(jmri.jmrit.beantable.ListedTableFrame.class)==null){
            jmri.jmrit.beantable.ListedTableFrame tmp = new jmri.jmrit.beantable.ListedTableFrame();
        }
        
        add(new jmri.jmrit.beantable.ListedTableAction("ECoS Loco Database", "jmri.jmrix.ecos.swing.locodatabase.EcosLocoTableAction"));

        add(new apps.gui3.TabbedPreferencesAction("ECoS Preferences", "ECOS"));

        AbstractAction addRostertoecos = new jmri.jmrix.ecos.utilities.AddRosterEntryToEcos("Add Roster Entry to ECoS");
        add(addRostertoecos);
    }

}

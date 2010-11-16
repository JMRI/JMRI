// Menu.java

package jmri.jmrix.ecos;

import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.JMenu;

/**
 * Create a "Systems" menu containing the Jmri ECOS-specific tools.
 *
 * @author	Bob Jacobsen   Copyright 2003, 2008
 * @version     $Revision: 1.3 $
 */
public class Menu extends JMenu {
    public Menu(String name) {
        this();
        setText(name);
    }

    public Menu() {

        super();

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.JmrixSystemsBundle");

        setText(rb.getString("MenuItemEcos"));

        
        add(new jmri.jmrix.ecos.swing.monitor.MonAction(rb.getString("MenuItemCommandMonitor")));
        add(new jmri.jmrix.ecos.swing.packetgen.PacketGenAction(rb.getString("MenuItemSendCommand")));
        add(new jmri.jmrix.ecos.swing.statusframe.StatusFrameAction("ECoS Info"));
        add(new jmri.jmrix.ecos.swing.preferences.PreferencesFrameAction("ECoS Preferences"));
        add(new jmri.jmrix.ecos.swing.locodatabase.EcosLocoTableAction("ECoS Loco Database"));
        //Add the locodatabase to the listed table method
        jmri.jmrit.beantable.ListedTableFrame tmp = new jmri.jmrit.beantable.ListedTableFrame();
        tmp.addTable("jmri.jmrix.ecos.swing.locodatabase.EcosLocoTableAction", "ECoS Loco Database", true);
        tmp = null;
        AbstractAction addRostertoecos = new jmri.jmrix.ecos.utilities.AddRosterEntryToEcos("Add Roster Entry to ECoS");
        add(addRostertoecos);
    }

}

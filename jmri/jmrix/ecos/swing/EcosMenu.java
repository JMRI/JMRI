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

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.JmrixSystemsBundle");

        if (memo != null)
            setText(memo.getUserName());
        else
            setText(rb.getString("MenuItemEcos"));

        jmri.util.swing.WindowInterface wi = new jmri.util.swing.sdi.JmriJFrameInterface();
        add(new jmri.jmrix.ecos.swing.monitor.MonAction(rb.getString("MenuItemCommandMonitor")));
        add(new jmri.jmrix.ecos.swing.packetgen.PacketGenAction(rb.getString("MenuItemSendCommand")));
        add(new jmri.jmrix.ecos.swing.statusframe.StatusFrameAction("ECoS Info", memo));
        if (jmri.InstanceManager.getDefault(jmri.jmrit.beantable.ListedTableFrame.class)==null){
            new jmri.jmrit.beantable.ListedTableFrame();
        }

        add(new jmri.jmrit.beantable.ListedTableAction("ECoS Loco Database", "jmri.jmrix.ecos.swing.locodatabase.EcosLocoTableAction"));
        add(new apps.gui3.TabbedPreferencesAction("ECoS Preferences", "ECOS", null));

        add(new jmri.jmrix.ecos.utilities.AddRosterEntryToEcos("Add Roster Entry to ECoS", memo));

    }

}

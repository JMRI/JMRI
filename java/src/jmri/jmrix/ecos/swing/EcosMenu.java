package jmri.jmrix.ecos.swing;

import javax.swing.JMenu;

import jmri.InstanceManager;
import jmri.jmrix.ecos.EcosSystemConnectionMemo;
import jmri.util.prefs.JmriPreferencesActionFactory;

/**
 * Create a "Systems" menu containing the Jmri ECoS-specific tools.
 *
 * @author Kevin Dickerson
 */
public class EcosMenu extends JMenu {

    public EcosMenu(EcosSystemConnectionMemo memo) {
        super();

        String title;
        if (memo != null) {
            title = memo.getUserName();
        } else {
            title = Bundle.getMessage("MenuEcos");
        }
        setText(title);

        jmri.util.swing.WindowInterface wi = new jmri.util.swing.sdi.JmriJFrameInterface();

        for (Item item : panelItems) {
            if (item == null) {
                add(new javax.swing.JSeparator());
            } else {
                add(new EcosNamedPaneAction(Bundle.getMessage(item.name), wi, item.load, memo));
            }
        }

        add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getMessage("MenuItemDatabase"),
            "jmri.jmrix.ecos.swing.locodatabase.EcosLocoTableTabAction"));
        add(InstanceManager.getDefault(JmriPreferencesActionFactory.class).
                getCategorizedAction(Bundle.getMessage("MenuItemECoSPrefs"), "ECoS", title));
        if (memo != null) {
            add(new jmri.jmrix.ecos.utilities.AddRosterEntryToEcos(Bundle.getMessage("MenuItemAddLocoToEcos"), memo));
        }
    }

    Item[] panelItems = new Item[]{
        new Item("MenuItemEcosMonitor", "jmri.jmrix.ecos.swing.monitor.EcosMonPane"),
        new Item("MenuItemSendPacket", "jmri.jmrix.ecos.swing.packetgen.PacketGenPanel"),
        new Item("MenuItemInfo", "jmri.jmrix.ecos.swing.statusframe.StatusPanel")

    };

    static class Item {

        Item(String name, String load) {
            this.name = name;
            this.load = load;
        }

        String name;
        String load;
    }

    // private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EcosMenu.class);

}

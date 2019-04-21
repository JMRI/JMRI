package jmri.jmrix.ecos.swing;

import javax.swing.JMenu;
import jmri.jmrix.ecos.EcosSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

        if (jmri.InstanceManager.getNullableDefault(jmri.jmrit.beantable.ListedTableFrame.class) == null) {
            try {
                new jmri.jmrit.beantable.ListedTableFrame();
            } catch (java.lang.NullPointerException ex) {
                log.error("Unable to register ECoS table");
            }
        }

        add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getMessage("MenuItemDatabase"), "jmri.jmrix.ecos.swing.locodatabase.EcosLocoTableTabAction"));
        add(new apps.gui3.tabbedpreferences.TabbedPreferencesAction(Bundle.getMessage("MenuItemECoSPrefs"), "ECoS", title));
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

    private final static Logger log = LoggerFactory.getLogger(EcosMenu.class);

}

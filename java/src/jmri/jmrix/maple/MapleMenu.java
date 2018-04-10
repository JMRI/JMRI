package jmri.jmrix.maple;

import javax.swing.JMenu;

/**
 * Create a "Systems" menu containing the Jmri Maple-specific tools.
 *
 * @author Bob Jacobsen Copyright 2008
 */
public class MapleMenu extends JMenu {

    public MapleMenu(String name, MapleSystemConnectionMemo memo) {
        this(memo);
        setText(name);
    }

    public MapleMenu(MapleSystemConnectionMemo memo) {

        super();
        if (memo != null) {
            setText(memo.getUserName());
        } else {
            setText(Bundle.getMessage("MenuMaple"));
        }

        add(new jmri.jmrix.maple.serialmon.SerialMonAction(Bundle.getMessage("MenuItemCommandMonitor"), memo));
        add(new jmri.jmrix.maple.packetgen.SerialPacketGenAction(Bundle.getMessage("MenuItemSendCommand"), memo));
        add(new javax.swing.JSeparator());
        if (memo != null) {
            add(new jmri.jmrix.maple.nodeconfig.NodeConfigAction(memo));
        }
        add(new javax.swing.JSeparator());
        if (memo != null) {
            add(new jmri.jmrix.maple.assignment.ListAction(Bundle.getMessage("MenuItemAssignments"), memo));
        }
    }

}

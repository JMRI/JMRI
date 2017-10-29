package jmri.jmrix.maple;

import java.util.ResourceBundle;
import javax.swing.JMenu;

/**
 * Create a "Systems" menu containing the Jmri Maple-specific tools
 *
 * @author Bob Jacobsen Copyright 2008
 */
public class MapleMenu extends JMenu {

    MapleSystemConnectionMemo _memo = null;

    public MapleMenu(String name, MapleSystemConnectionMemo memo) {
        this(memo);
        setText(name);
    }

    public MapleMenu(MapleSystemConnectionMemo memo) {

        super();
        _memo = memo;

        if (memo != null) {
            setText(memo.getUserName());
        } else {
            setText(Bundle.getMessage("MenuMaple"));
        }

        add(new jmri.jmrix.maple.serialmon.SerialMonAction(Bundle.getMessage("MenuItemCommandMonitor"))); // TODO more memo, cf CMRI
        add(new jmri.jmrix.maple.packetgen.SerialPacketGenAction(Bundle.getMessage("MenuItemSendCommand")));
        add(new javax.swing.JSeparator());
        add(new jmri.jmrix.maple.nodeconfig.NodeConfigAction(_memo));
        add(new javax.swing.JSeparator());
        add(new jmri.jmrix.maple.assignment.ListAction(Bundle.getMessage("MenuItemAssignments"), _memo));
    }

}

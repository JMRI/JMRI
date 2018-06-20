package jmri.jmrix.grapevine;

import java.util.ResourceBundle;
import javax.swing.JMenu;

/**
 * Create a "Systems" menu containing the Jmri Grapevine-specific tools.
 *
 * @author Bob Jacobsen Copyright 2003, 2006, 2007
 */
public class GrapevineMenu extends JMenu {

    private GrapevineSystemConnectionMemo memo = null;

    public GrapevineMenu(String name, GrapevineSystemConnectionMemo _memo) {
        this(_memo);
        setText(name);
    }

    public GrapevineMenu(GrapevineSystemConnectionMemo _memo) {

        super();
        memo = _memo;

        if (memo != null) {
            setText(memo.getUserName());
        } else {
            setText(Bundle.getMessage("MenuSystem"));
        }

        if (memo != null) {
            // do we have a GrapevineTrafficController?
            setEnabled(memo.getTrafficController() != null); // disable menu, no connection, no tools!
            add(new jmri.jmrix.grapevine.serialmon.SerialMonAction(Bundle.getMessage("MenuItemCommandMonitor"), memo));
            add(new jmri.jmrix.grapevine.packetgen.SerialPacketGenAction(Bundle.getMessage("MenuItemSendCommand"), memo));
            add(new jmri.jmrix.grapevine.nodeconfig.NodeConfigAction(Bundle.getMessage("ConfigNodesTitle"), memo));
            add(new jmri.jmrix.grapevine.nodetable.NodeTableAction(Bundle.getMessage("MenuItemNodeTable"), memo));
        }
    }

}

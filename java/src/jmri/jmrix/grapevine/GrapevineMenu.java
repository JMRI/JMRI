package jmri.jmrix.grapevine;

import java.util.ResourceBundle;
import javax.swing.JMenu;

/**
 * Create a "Systems" menu containing the Jmri Grapevine-specific tools
 *
 * @author Bob Jacobsen Copyright 2003, 2006, 2007
 */
public class GrapevineMenu extends JMenu {

    private GrapevineSystemConnectionMemo memo = null;

    public GrapevineMenu(String name,GrapevineSystemConnectionMemo _memo) {
        this(_memo);
        setText(name);
    }

    public GrapevineMenu(GrapevineSystemConnectionMemo _memo) {

        super();
        memo = _memo;

        setText(Bundle.getMessage("MenuSystem"));

        add(new jmri.jmrix.grapevine.serialmon.SerialMonAction(Bundle.getMessage("MenuItemCommandMonitor"), memo));
        add(new jmri.jmrix.grapevine.packetgen.SerialPacketGenAction(Bundle.getMessage("MenuItemSendCommand"), memo));
        add(new jmri.jmrix.grapevine.nodeconfig.NodeConfigAction(Bundle.getMessage("MenuItemConfigNodes"), memo));
        add(new jmri.jmrix.grapevine.nodetable.NodeTableAction(Bundle.getMessage("MenuItemNodeTable"), memo));
    }

}

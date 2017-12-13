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

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.grapevine.GrapevineBundle");

        setText(rb.getString("MenuSystem"));

        add(new jmri.jmrix.grapevine.serialmon.SerialMonAction(rb.getString("MenuItemCommandMonitor"),memo));
        add(new jmri.jmrix.grapevine.packetgen.SerialPacketGenAction(rb.getString("MenuItemSendCommand"),memo));
        add(new jmri.jmrix.grapevine.nodeconfig.NodeConfigAction(rb.getString("MenuItemConfigNodes"),memo));
        add(new jmri.jmrix.grapevine.nodetable.NodeTableAction(rb.getString("MenuItemNodeTable"),memo));

    }

}

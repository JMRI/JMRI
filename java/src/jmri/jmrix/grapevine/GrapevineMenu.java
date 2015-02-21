// GrapevineMenu.java
package jmri.jmrix.grapevine;

import java.util.ResourceBundle;
import javax.swing.JMenu;

/**
 * Create a "Systems" menu containing the Jmri Grapevine-specific tools
 *
 * @author	Bob Jacobsen Copyright 2003, 2006, 2007
 * @version $Revision$
 */
public class GrapevineMenu extends JMenu {

    /**
     *
     */
    private static final long serialVersionUID = 4017450895998949082L;

    public GrapevineMenu(String name) {
        this();
        setText(name);
    }

    public GrapevineMenu() {

        super();

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.grapevine.GrapevineBundle");

        setText(rb.getString("MenuSystem"));

        add(new jmri.jmrix.grapevine.serialmon.SerialMonAction(rb.getString("MenuItemCommandMonitor")));
        add(new jmri.jmrix.grapevine.packetgen.SerialPacketGenAction(rb.getString("MenuItemSendCommand")));
        add(new jmri.jmrix.grapevine.nodeconfig.NodeConfigAction(rb.getString("MenuItemConfigNodes")));
        add(new jmri.jmrix.grapevine.nodetable.NodeTableAction(rb.getString("MenuItemNodeTable")));

    }

}

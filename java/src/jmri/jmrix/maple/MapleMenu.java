// MapleMenu.java
package jmri.jmrix.maple;

import java.util.ResourceBundle;
import javax.swing.JMenu;

/**
 * Create a "Systems" menu containing the Jmri Maple-specific tools
 *
 * @author	Bob Jacobsen Copyright 2008
 * @version $Revision$
 */
public class MapleMenu extends JMenu {

    /**
     *
     */
    private static final long serialVersionUID = 5496558545288767910L;

    public MapleMenu(String name) {
        this();
        setText(name);
    }

    public MapleMenu() {

        super();

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.maple.MapleBundle");

        setText(rb.getString("MenuMaple"));

        add(new jmri.jmrix.maple.serialmon.SerialMonAction(rb.getString("MenuItemCommandMonitor")));
        add(new jmri.jmrix.maple.packetgen.SerialPacketGenAction(rb.getString("MenuItemSendCommand")));
        add(new javax.swing.JSeparator());
        add(new jmri.jmrix.maple.nodeconfig.NodeConfigAction());
        add(new javax.swing.JSeparator());
        add(new jmri.jmrix.maple.assignment.ListAction(rb.getString("MenuItemAssignments")));
    }

}

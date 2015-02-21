// NceMenu.java
package jmri.jmrix.direct;

import java.util.ResourceBundle;
import javax.swing.JMenu;

/**
 * Create a "Systems" menu containing the Jmri direct-drive-specific tools.
 * <P>
 *
 * @author	Bob Jacobsen Copyright 2003
 * @version $Revision$
 */
public class DirectMenu extends JMenu {

    /**
     *
     */
    private static final long serialVersionUID = -4869945275533906814L;

    public DirectMenu(String name) {
        this();
        setText(name);
    }

    public DirectMenu() {

        super();

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.JmrixSystemsBundle");

        setText(rb.getString("MenuItemDirect"));

        // add(new jmri.jmrix.nce.ncemon.NceMonAction(rb.getString("MenuItemCommandMonitor")));
        // add(new jmri.jmrix.nce.packetgen.NcePacketGenAction(rb.getString("MenuItemSendCommand")));
        // add(new jmri.jmrix.ncemonitor.NcePacketMonitorAction("Track Packet Monitor"));
    }

}

// SystemMenu.java

package jmri.jmrix.srcp;

import java.util.ResourceBundle;

import javax.swing.JMenu;

/**
 * Create a "Systems" menu containing the system-specific tools
 *
 * @author	Bob Jacobsen   Copyright 2008
 * @version     $Revision$
 */
public class SystemMenu extends JMenu {
    public SystemMenu(String name) {
        this();
        setText(name);
    }

    public SystemMenu() {

        super();

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.JmrixSystemsBundle");

        // setText(rb.getString("MenuSystems"));
        setText(rb.getString("MenuItemSRCP"));

        add(new jmri.jmrix.srcp.srcpmon.SRCPMonAction(rb.getString("MenuItemCommandMonitor")));
        add(new jmri.jmrix.srcp.packetgen.PacketGenAction(rb.getString("MenuItemSendCommand")));

    }

}



/**
 * NceMenu.java
 */

package jmri.jmrix.nce;

import java.util.ResourceBundle;

import javax.swing.JMenu;

/**
 * Create a "Systems" menu containing the Jmri NCE-specific tools
 *
 * @author	Bob Jacobsen   Copyright 2003
 * @version     $Revision: 1.1 $
 */
public class NceMenu extends JMenu {
    public NceMenu(String name) {
        this();
        setText(name);
    }

    public NceMenu() {

        super();

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.JmrixSystemsBundle");

        setText(rb.getString("MenuItemNCE"));

        add(new jmri.jmrix.nce.ncemon.NceMonAction(rb.getString("MenuItemCommandMonitor")));
        add(new jmri.jmrix.nce.packetgen.NcePacketGenAction(rb.getString("MenuItemSendCommand")));
        add(new jmri.jmrix.ncemonitor.NcePacketMonitorAction("Track Packet Monitor"));

    }

}



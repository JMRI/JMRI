// XNetMenu.java

package jmri.jmrix.lenz;

import java.util.ResourceBundle;

import javax.swing.JMenu;

/**
 * Create a "Systems" menu containing the Jmri XPressNet-specific tools
 *
 * @author	Bob Jacobsen   Copyright 2003
 * @version     $Revision: 1.1 $
 */
public class XNetMenu extends JMenu {
    public XNetMenu(String name) {
        this();
        setText(name);
    }

    public XNetMenu() {

        super();

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.JmrixSystemsBundle");

        setText(rb.getString("MenuItemXPressNet"));

        add(new jmri.jmrix.lenz.mon.XNetMonAction(rb.getString("MenuItemCommandMonitor")));
        add(new jmri.jmrix.lenz.packetgen.PacketGenAction(rb.getString("MenuItemSendCommand")));

    }

}



// SystemMenu.java

package jmri.jmrix.powerline;

import java.util.ResourceBundle;

import javax.swing.JMenu;

/**
 * Create a "Systems" menu containing the Jmri powerline-specific tools
 *
 * @author	Bob Jacobsen   Copyright 2003, 2006, 2007, 2008
 * @version     $Revision: 1.1 $
 */
public class SystemMenu extends JMenu {
    public SystemMenu(String name) {
        this();
        setText(name);
    }

    public SystemMenu() {

        super();

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.powerline.SystemBundle");

        setText(rb.getString("MenuSystem"));

        add(new jmri.jmrix.powerline.serialmon.SerialMonAction(rb.getString("MenuItemCommandMonitor")));
        add(new jmri.jmrix.powerline.packetgen.SerialPacketGenAction(rb.getString("MenuItemSendCommand")));

    }

}



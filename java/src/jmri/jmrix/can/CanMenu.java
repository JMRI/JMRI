// CanMenu.java

package jmri.jmrix.can;

import java.util.ResourceBundle;

import javax.swing.JMenu;

/**
 * Create a menu containing the Jmri CAN-specific tools
 *
 * @author	Bob Jacobsen   Copyright 2003, 2008
 * @author      Andrew Crosland 2008
 * @version     $Revision$
 * @deprecated 2.99.2
 */
@Deprecated
public class CanMenu extends JMenu {
    public CanMenu(String name) {
        this();
        setText(name);
    }

    public CanMenu() {

        super();

        /*ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.JmrixSystemsBundle");

        setText(rb.getString("MenuItemCAN"));

        add(new jmri.jmrix.can.swing.monitor.MonitorAction(rb.getString("MenuItemConsole")));
        add(new jmri.jmrix.can.swing.send.CanSendAction(rb.getString("MenuItemSendFrame")));*/

    }

}



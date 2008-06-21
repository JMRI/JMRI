// CanUsbMenu.java

package jmri.jmrix.can.adapters.gridconnect.canusb;

import java.util.ResourceBundle;

import javax.swing.JMenu;

/**
 * Create a "Systems" menu containing the Jmri CAN-USB-specific tools
 *
 * @author	Bob Jacobsen   Copyright 2003
 * @author      Andrew Crosland 2008
 * @version     $Revision: 1.1 $
 */
public class CanUsbMenu extends JMenu {
    public CanUsbMenu(String name) {
        this();
        setText(name);
    }

    public CanUsbMenu() {

        super();

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.JmrixSystemsBundle");

        // setText(rb.getString("MenuSystems"));
        setText("CAN-USB");

        add(new jmri.jmrix.can.cbus.swing.console.CbusConsoleAction(rb.getString("MenuItemConsole")));
        add(new jmri.jmrix.can.swing.send.CanSendAction(rb.getString("MenuItemSendFrame")));

    }

}

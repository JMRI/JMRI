/**
 * CanRsMenu.java
 */

package jmri.jmrix.can.adapters.gridconnect.canrs;

import java.util.ResourceBundle;

import javax.swing.JMenu;

/**
 * Create a "Systems" menu containing the Jmri CAN-RS-specific tools
 *
 * @author	Bob Jacobsen   Copyright 2003
 * @author      Andrew Crosland 2008
 * @version     $Revision: 1.1 $
 */
public class CanRsMenu extends JMenu {
    public CanRsMenu(String name) {
        this();
        setText(name);
    }

    public CanRsMenu() {

        super();

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.JmrixSystemsBundle");

        // setText(rb.getString("MenuSystems"));
        setText("CAN-RS");

        add(new jmri.jmrix.can.cbus.swing.console.CbusConsoleAction(rb.getString("MenuItemConsole")));
        add(new jmri.jmrix.can.swing.send.CanSendAction(rb.getString("MenuItemSendFrame")));

    }

}



// CbusMenu.java

package jmri.jmrix.can.cbus;

import java.util.ResourceBundle;

import javax.swing.JMenu;

/**
 * Create a menu containing the Jmri CAN- and CBUS-specific tools
 *
 * @author	    Bob Jacobsen   Copyright 2003, 2008, 2009
 * @author      Andrew Crosland 2008
 * @version     $Revision$
 */
public class CbusMenu extends JMenu {
    public CbusMenu(String name) {
        this();
        setText(name);
    }

    public CbusMenu() {

        super();

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.JmrixSystemsBundle");

        setText(rb.getString("MenuItemCBUS"));

        add(new jmri.jmrix.can.cbus.swing.console.CbusConsoleAction(rb.getString("MenuItemConsole")));
        add(new jmri.jmrix.can.swing.send.CanSendAction(rb.getString("MenuItemSendFrame")));
        add(new jmri.jmrix.can.cbus.swing.configtool.ConfigToolAction(ResourceBundle
                .getBundle("jmri.jmrix.can.cbus.swing.configtool.ConfigToolBundle")
                        .getString("MenuItemConfigTool")));
        add(new jmri.jmrix.can.cbus.swing.eventtable.CbusEventTableAction(rb.getString("MenuItemEventTable")));

        add(new jmri.jmrix.can.cbus.swing.nodeconfig.NodeConfigToolAction("Node Config Tool"));
    }

}



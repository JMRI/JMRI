// XNetMenu.java

package jmri.jmrix.lenz;

import java.util.ResourceBundle;

import javax.swing.JMenu;

/**
 * Create a menu containing the XPressNet specific tools
 *
 * @author	Paul Bender   Copyright 2003
 * @version     $Revision: 1.6 $
 */
public class XNetMenu extends JMenu {
    public XNetMenu(String name) {
        this();
        setText(name);
    }

    public XNetMenu() {

        super();

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.lenz.XNetBundle");

        setText(rb.getString("MenuXPressNet"));

        add(new jmri.jmrix.lenz.mon.XNetMonAction(rb.getString("MenuItemXNetCommandMonitor")));
        add(new jmri.jmrix.lenz.systeminfo.SystemInfoAction(rb.getString("MenuItemXNetSystemInformation")));
        //add(new jmri.jmrix.lenz.datamon.DataMonAction(rb.getString("MenuItemCSDatabaseManager")));
        add(new jmri.jmrix.lenz.li101.LI101Action(rb.getString("MenuItemLI101ConfigurationManager")));
        add(new jmri.jmrix.lenz.lzv100.LZV100Action(rb.getString("MenuItemLZV100ConfigurationManager")));
        add(new jmri.jmrix.lenz.packetgen.PacketGenAction(rb.getString("MenuItemSendXNetCommand")));

    }

}



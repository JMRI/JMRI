// XNetMenu.java

package jmri.jmrix.lenz;

import java.util.ResourceBundle;

import javax.swing.JMenu;

/**
 * Create a menu containing the XPressNet specific tools
 *
 * @author	Paul Bender   Copyright 2003
 * @version     $Revision: 2.4 $
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
        add(new jmri.jmrix.lenz.packetgen.PacketGenAction(rb.getString("MenuItemSendXNetCommand")));
	add(new javax.swing.JSeparator());
        add(new jmri.jmrix.lenz.stackmon.StackMonAction(rb.getString("MenuItemCSDatabaseManager")));
        add(new jmri.jmrix.lenz.li101.LI101Action(rb.getString("MenuItemLI101ConfigurationManager")));
        add(new jmri.jmrix.lenz.lz100.LZ100Action(rb.getString("MenuItemLZ100ConfigurationManager")));
        add(new jmri.jmrix.lenz.lzv100.LZV100Action(rb.getString("MenuItemLZV100ConfigurationManager")));
        add(new jmri.jmrix.lenz.lv102.LV102Action(rb.getString("MenuItemLV102ConfigurationManager")));

    }

}



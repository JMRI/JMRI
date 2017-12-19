package jmri.jmrix.secsi;

import java.util.ResourceBundle;
import javax.swing.JMenu;

/**
 * Create a "Systems" menu containing the Jmri SECSI-specific tools
 *
 * @author	Bob Jacobsen Copyright 2003, 2006, 2007
 */
public class SecsiMenu extends JMenu {

    public SecsiMenu(String name) {
        this();
        setText(name);
    }

    public SecsiMenu() {

        super();

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.secsi.SecsiBundle");

        setText(rb.getString("MenuSystem"));

        add(new jmri.jmrix.secsi.serialmon.SerialMonAction(rb.getString("MenuItemCommandMonitor")));
        add(new jmri.jmrix.secsi.packetgen.SerialPacketGenAction(rb.getString("MenuItemSendCommand")));
    }

}

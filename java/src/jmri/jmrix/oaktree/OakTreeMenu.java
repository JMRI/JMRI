package jmri.jmrix.oaktree;

import java.util.ResourceBundle;
import javax.swing.JMenu;

/**
 * Create a "Systems" menu containing the Jmri Oak Tree-specific tools
 *
 * @author	Bob Jacobsen Copyright 2003, 2006
 */
public class OakTreeMenu extends JMenu {

    public OakTreeMenu(String name) {
        this();
        setText(name);
    }

    public OakTreeMenu() {

        super();

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.oaktree.OakTreeBundle");

        setText(rb.getString("MenuOakTree"));

        add(new jmri.jmrix.oaktree.serialmon.SerialMonAction(rb.getString("MenuItemCommandMonitor")));
        add(new jmri.jmrix.oaktree.packetgen.SerialPacketGenAction(rb.getString("MenuItemSendCommand")));

    }

}

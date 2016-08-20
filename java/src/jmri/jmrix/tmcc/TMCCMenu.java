package jmri.jmrix.tmcc;

import java.util.ResourceBundle;
import javax.swing.JMenu;

/**
 * Create a "Systems" menu containing the Jmri TMCC-specific tools
 *
 * @author	Bob Jacobsen Copyright 2003, 2006
 */
public class TMCCMenu extends JMenu {

    public TMCCMenu(String name) {
        this();
        setText(name);
    }

    public TMCCMenu() {

        super();

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.tmcc.TMCCBundle");

        setText(rb.getString("MenuTMCC"));

        add(new jmri.jmrix.tmcc.serialmon.SerialMonAction(rb.getString("MenuItemCommandMonitor")));
        add(new jmri.jmrix.tmcc.packetgen.SerialPacketGenAction(rb.getString("MenuItemSendCommand")));

    }

}

package jmri.jmrix.secsi;

import java.util.ResourceBundle;
import javax.swing.JMenu;

/**
 * Create a "Systems" menu containing the Jmri SECSI-specific tools
 *
 * @author	Bob Jacobsen Copyright 2003, 2006, 2007
 */
public class SecsiMenu extends JMenu {

    private SecsiSystemConnectionMemo memo = null;

    public SecsiMenu(String name,SecsiSystemConnectionMemo _memo) {
        this(_memo);
        setText(name);
    }

    public SecsiMenu(SecsiSystemConnectionMemo _memo) {

        super();
        memo = _memo;
        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.secsi.SecsiBundle");

        setText(rb.getString("MenuSystem"));

        add(new jmri.jmrix.secsi.serialmon.SerialMonAction(rb.getString("MenuItemCommandMonitor"),memo));
        add(new jmri.jmrix.secsi.packetgen.SerialPacketGenAction(rb.getString("MenuItemSendCommand"),memo));
    }

}

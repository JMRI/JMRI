package jmri.jmrix.srcp.swing;

import java.util.ResourceBundle;
import javax.swing.JMenu;
import jmri.jmrix.srcp.SRCPSystemConnectionMemo;
import jmri.jmrix.srcp.swing.packetgen.PacketGenAction;
import jmri.jmrix.srcp.swing.srcpmon.SRCPMonAction;

/**
 * Create a "Systems" menu containing the system-specific SRCP tools.
 *
 * @author	Bob Jacobsen Copyright 2008
 */
public class SystemMenu extends JMenu {

    public SystemMenu(String name, SRCPSystemConnectionMemo memo) {
        this(memo);
        setText(name);
    }

    public SystemMenu(SRCPSystemConnectionMemo memo) {
        super();

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.JmrixSystemsBundle");

        if (memo != null) {
            setText(memo.getUserName());
        } else {
            setText(rb.getString("MenuItemSRCP"));
        }

        if (memo != null) {
            add(new SRCPMonAction(rb.getString("MenuItemCommandMonitor"), memo));
            add(new PacketGenAction(rb.getString("MenuItemSendCommand"), memo));
        }
    }

}

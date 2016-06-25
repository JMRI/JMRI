package jmri.jmrix.srcp.swing;

import java.util.ResourceBundle;
import javax.swing.JMenu;
import jmri.jmrix.srcp.SRCPSystemConnectionMemo;
import jmri.jmrix.srcp.swing.srcpmon.SRCPMonAction;
import jmri.jmrix.srcp.swing.packetgen.PacketGenAction;

/**
 * Create a "Systems" menu containing the system-specific tools
 *
 * @author	Bob Jacobsen Copyright 2008
 */
public class SystemMenu extends JMenu {

    private SRCPSystemConnectionMemo _memo = null;

    public SystemMenu(String name, SRCPSystemConnectionMemo memo) {
        this(memo);
        setText(name);
    }

    public SystemMenu(SRCPSystemConnectionMemo memo) {

        super();
        _memo = memo;

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.JmrixSystemsBundle");

        // setText(rb.getString("MenuSystems"));
        setText(rb.getString("MenuItemSRCP"));

        add(new SRCPMonAction(rb.getString("MenuItemCommandMonitor"),_memo));
        add(new PacketGenAction(rb.getString("MenuItemSendCommand"),_memo));

    }

}

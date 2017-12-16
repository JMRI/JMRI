package jmri.jmrix.oaktree;

import java.util.ResourceBundle;
import javax.swing.JMenu;

/**
 * Create a "Systems" menu containing the Jmri Oak Tree-specific tools
 *
 * @author Bob Jacobsen Copyright 2003, 2006
 */
public class OakTreeMenu extends JMenu {

    private OakTreeSystemConnectionMemo _memo = null;

    public OakTreeMenu(String name,OakTreeSystemConnectionMemo memo) {
        this(memo);
        setText(name);
    }

    public OakTreeMenu(OakTreeSystemConnectionMemo memo) {

        super();

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.oaktree.OakTreeBundle");

        setText(rb.getString("MenuOakTree"));

        add(new jmri.jmrix.oaktree.serialmon.SerialMonAction(rb.getString("MenuItemCommandMonitor"),_memo));
        add(new jmri.jmrix.oaktree.packetgen.SerialPacketGenAction(rb.getString("MenuItemSendCommand"),_memo));

    }

}

package jmri.jmrix.jmriclient.swing;

import java.util.ResourceBundle;
import javax.swing.JMenu;

/**
 * Create a "JMRIClient" menu containing the system-specific tools.
 *
 * @author Bob Jacobsen Copyright 2008
 */
public class JMRIClientMenu extends JMenu {

    public JMRIClientMenu(String name, jmri.jmrix.jmriclient.JMRIClientSystemConnectionMemo memo) {
        this(memo);
        setText(name);
    }

    public JMRIClientMenu(jmri.jmrix.jmriclient.JMRIClientSystemConnectionMemo memo) {
        super();

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.JmrixSystemsBundle");

        if (memo != null) {
            setText(memo.getUserName());
        } else {
            setText(rb.getString("MenuItemJMRIClient"));
        }

        if (memo != null) {
            add(new jmri.jmrix.jmriclient.swing.mon.JMRIClientMonAction(rb.getString("MenuItemCommandMonitor"), memo));
            add(new jmri.jmrix.jmriclient.swing.packetgen.PacketGenAction(rb.getString("MenuItemSendCommand"), memo));
        }
    }

}

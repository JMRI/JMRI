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

        if (memo != null) {
            setText(memo.getUserName());
        } else {
            setText(Bundle.getMessage("MenuItemJMRIClient"));
        }

        if (memo != null) {
            add(new jmri.jmrix.jmriclient.swing.mon.JMRIClientMonAction());
            add(new jmri.jmrix.jmriclient.swing.packetgen.PacketGenAction(Bundle.getMessage("MenuItemSendCommand"), memo));
        }
    }

}

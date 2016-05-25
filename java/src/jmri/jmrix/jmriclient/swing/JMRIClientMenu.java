// JMRIClientMenu.java
package jmri.jmrix.jmriclient.swing;

import java.util.ResourceBundle;
import javax.swing.JMenu;

/**
 * Create a "JMRIClient" menu containing the system-specific tools
 *
 * @author	Bob Jacobsen Copyright 2008
 * @version $Revision$
 */
public class JMRIClientMenu extends JMenu {

    /**
     *
     */
    private static final long serialVersionUID = -5861232083288110754L;

    public JMRIClientMenu(String name, jmri.jmrix.jmriclient.JMRIClientSystemConnectionMemo memo) {
        this(memo);
        setText(name);
    }

    public JMRIClientMenu(jmri.jmrix.jmriclient.JMRIClientSystemConnectionMemo memo) {

        super();
        _memo = memo;

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.JmrixSystemsBundle");

        setText(rb.getString("MenuItemJMRIClient"));

        add(new jmri.jmrix.jmriclient.swing.mon.JMRIClientMonAction(rb.getString("MenuItemCommandMonitor"), _memo));
        add(new jmri.jmrix.jmriclient.swing.packetgen.PacketGenAction(rb.getString("MenuItemSendCommand"), _memo));
    }

    jmri.jmrix.jmriclient.JMRIClientSystemConnectionMemo _memo = null;

}

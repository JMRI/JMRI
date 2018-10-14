package jmri.jmrix.ieee802154.xbee.swing;

import java.util.ResourceBundle;
import javax.swing.JMenu;

/**
 * Create a menu containing the XBee specific tools
 *
 * @author Paul Bender Copyright 2013
 */
public class XBeeMenu extends JMenu {

    public XBeeMenu(String name, jmri.jmrix.ieee802154.xbee.XBeeConnectionMemo memo) {
        this(memo);
        setText(name);
    }

    public XBeeMenu(jmri.jmrix.ieee802154.xbee.XBeeConnectionMemo memo) {

        //super(memo);

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.ieee802154.IEEE802154ActionListBundle");

        if (memo != null) {
            setText(memo.getUserName());
        } else {
            setText(rb.getString("MenuXBee"));
        }

        add(new jmri.jmrix.ieee802154.xbee.swing.packetgen.PacketGenAction(rb.getString("jmri.jmrix.ieee802154.xbee.swing.packetgen.PacketGenAction"), memo));
        add(new jmri.jmrix.ieee802154.xbee.swing.nodeconfig.XBeeNodeConfigAction(rb.getString("jmri.jmrix.ieee802154.xbee.swing.nodeconfig.XBeeNodeConfigAction"), memo));

    }

}

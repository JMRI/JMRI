package jmri.jmrix.ieee802154.swing;

import java.util.ResourceBundle;
import javax.swing.JMenu;

/**
 * Create a menu containing the IEEE 802.15.4 specific tools
 *
 * @author Paul Bender Copyright 2013
 */
public class IEEE802154Menu extends JMenu {

    public IEEE802154Menu(String name, jmri.jmrix.ieee802154.IEEE802154SystemConnectionMemo memo) {
        this(memo);
        setText(name);
    }

    public IEEE802154Menu(jmri.jmrix.ieee802154.IEEE802154SystemConnectionMemo memo) {

        super();

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.ieee802154.IEEE802154ActionListBundle");

        if (memo != null) {
            setText(memo.getUserName());
        } else {
            setText(rb.getString("MenuIEEE802154"));
        }

        if (memo != null) {
            add(new jmri.jmrix.ieee802154.swing.mon.IEEE802154MonAction());
            add(new jmri.jmrix.ieee802154.swing.packetgen.PacketGenAction(rb.getString("jmri.jmrix.ieee802154.swing.packetgen.PacketGenAction"), memo));
        }
    }

}

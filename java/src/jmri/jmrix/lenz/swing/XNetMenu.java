// XNetMenu.java
package jmri.jmrix.lenz.swing;

import java.util.ResourceBundle;
import javax.swing.JMenu;

/**
 * Create a menu containing the XPressNet specific tools
 *
 * @author	Paul Bender Copyright 2003,2010
 * @version $Revision$
 */
public class XNetMenu extends JMenu {

    /**
     *
     */
    private static final long serialVersionUID = -8473128194237379596L;

    public XNetMenu(String name, jmri.jmrix.lenz.XNetSystemConnectionMemo memo) {
        this(memo);
        setText(name);
    }

    public XNetMenu(jmri.jmrix.lenz.XNetSystemConnectionMemo memo) {

        super();

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.lenz.XNetBundle");

        if (memo != null) {
            setText(memo.getUserName());
        } else {
            setText(rb.getString("MenuXPressNet"));
        }

        add(new jmri.jmrix.lenz.swing.mon.XNetMonAction());
        add(new jmri.jmrix.lenz.swing.systeminfo.SystemInfoAction(rb.getString("MenuItemXNetSystemInformation"), memo));
        add(new jmri.jmrix.lenz.swing.packetgen.PacketGenAction(rb.getString("MenuItemSendXNetCommand"), memo));
        add(new javax.swing.JSeparator());
        add(new jmri.jmrix.lenz.swing.stackmon.StackMonAction(rb.getString("MenuItemCSDatabaseManager"), memo));
        add(new jmri.jmrix.lenz.swing.li101.LI101Action(rb.getString("MenuItemLI101ConfigurationManager"), memo));
        add(new jmri.jmrix.lenz.swing.liusb.LIUSBConfigAction(rb.getString("MenuItemLIUSBConfigurationManager"), memo));
        add(new jmri.jmrix.lenz.swing.lz100.LZ100Action(rb.getString("MenuItemLZ100ConfigurationManager"), memo));
        add(new jmri.jmrix.lenz.swing.lzv100.LZV100Action(rb.getString("MenuItemLZV100ConfigurationManager"), memo));
        // The LV102 configuration works with OpsModeProgramming, so does not
        // need the system connection memo.
        add(new jmri.jmrix.lenz.swing.lv102.LV102Action(rb.getString("MenuItemLV102ConfigurationManager")));

    }

}

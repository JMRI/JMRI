package jmri.jmrix.lenz.swing;

import javax.swing.JMenu;

/**
 * Create a menu containing the XpressNet specific tools
 *
 * @author Paul Bender Copyright 2003,2010
 */
public class XNetMenu extends JMenu {

    public XNetMenu(String name, jmri.jmrix.lenz.XNetSystemConnectionMemo memo) {
        this(memo);
        setText(name);
    }

    public XNetMenu(jmri.jmrix.lenz.XNetSystemConnectionMemo memo) {

        super();

        if (memo != null) {
            setText(memo.getUserName());
        } else {
            setText(Bundle.getMessage("MenuXpressNet"));
        }

        add(new jmri.jmrix.lenz.swing.mon.XNetMonAction());
        add(new jmri.jmrix.lenz.swing.systeminfo.SystemInfoAction(Bundle.getMessage("MenuItemXNetSystemInformation"), memo));
        add(new jmri.jmrix.lenz.swing.packetgen.PacketGenAction(Bundle.getMessage("MenuItemSendXNetCommand"), memo));
        add(new javax.swing.JSeparator());
        add(new jmri.jmrix.lenz.swing.stackmon.StackMonAction(Bundle.getMessage("MenuItemCSDatabaseManager"), memo));
        add(new jmri.jmrix.lenz.swing.li101.LI101Action(Bundle.getMessage("MenuItemLI101ConfigurationManager"), memo));
        add(new jmri.jmrix.lenz.swing.liusb.LIUSBConfigAction(Bundle.getMessage("MenuItemLIUSBConfigurationManager"), memo));
        add(new jmri.jmrix.lenz.swing.lz100.LZ100Action(Bundle.getMessage("MenuItemLZ100ConfigurationManager"), memo));
        add(new jmri.jmrix.lenz.swing.lzv100.LZV100Action(Bundle.getMessage("MenuItemLZV100ConfigurationManager"), memo));
        // The LV102 configuration works with OpsModeProgramming, so does not
        // need the system connection memo.
        add(new jmri.jmrix.lenz.swing.lv102.LV102Action(Bundle.getMessage("MenuItemLV102ConfigurationManager")));
    }

}

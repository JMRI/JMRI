package jmri.jmrix.sprog;

import java.util.ResourceBundle;
import javax.swing.JMenu;

/**
 * Create a "Systems" menu containing the Jmri SPROG-specific tools
 *
 * @author	Andrew Crosland Copyright 2006
 */
public class SPROGCSMenu extends JMenu {

    private SprogSystemConnectionMemo _memo = null;

    public SPROGCSMenu(SprogSystemConnectionMemo memo) {

        super();
        _memo = memo;
        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.JmrixSystemsBundle");

        setText(memo.getUserName());

        add(new jmri.jmrix.sprog.sprogslotmon.SprogSlotMonAction(rb.getString("MenuItemSlotMonitor"),_memo));
        add(new jmri.jmrix.sprog.sprogmon.SprogMonAction(rb.getString("MenuItemCommandMonitor"),_memo));
        add(new jmri.jmrix.sprog.packetgen.SprogPacketGenAction(rb.getString("MenuItemSendCommand"),_memo));
        add(new jmri.jmrix.sprog.console.SprogConsoleAction(rb.getString("MenuItemConsole"),_memo));
        add(new jmri.jmrix.sprog.update.SprogVersionAction("Get SPROG Firmware Version",_memo));
        add(new jmri.jmrix.sprog.update.Sprogv4UpdateAction("SPROG v3/v4 Firmware Update",_memo));
        add(new jmri.jmrix.sprog.update.SprogIIUpdateAction("SPROG II Firmware Update",_memo));
    }
}

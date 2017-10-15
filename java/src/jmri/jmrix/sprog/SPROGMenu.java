package jmri.jmrix.sprog;

import java.util.ResourceBundle;
import javax.swing.JMenu;

/**
 * Create a "Systems" menu containing the Jmri SPROG-specific tools.
 *
 * @author Bob Jacobsen Copyright 2003
 */
public class SPROGMenu extends JMenu {

    SprogSystemConnectionMemo _memo = null;

    public SPROGMenu(SprogSystemConnectionMemo memo) {
        super();
        _memo = memo;

        setText(memo.getUserName());

        add(new jmri.jmrix.sprog.sprogmon.SprogMonAction(Bundle.getMessage("MonitorXTitle", "SPROG"), _memo));
        add(new jmri.jmrix.sprog.packetgen.SprogPacketGenAction(Bundle.getMessage("SendCommandTitle"), _memo));
        add(new jmri.jmrix.sprog.console.SprogConsoleAction(Bundle.getMessage("MenuItemConsole"), _memo));
        add(new jmri.jmrix.sprog.update.SprogVersionAction(Bundle.getMessage("GetSprogFirmwareVersion"), _memo));
        add(new jmri.jmrix.sprog.update.Sprogv4UpdateAction(Bundle.getMessage("SprogXFirmwareUpdate", "v3/v4"), _memo));
        add(new jmri.jmrix.sprog.update.SprogIIUpdateAction(Bundle.getMessage("SprogXFirmwareUpdate", " II/SPROG 3"), _memo));
    }

}

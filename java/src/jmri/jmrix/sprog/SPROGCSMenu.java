package jmri.jmrix.sprog;

import javax.swing.JMenu;

/**
 * Create a Systems menu containing the Jmri SPROG-specific tools.
 *
 * @author	Andrew Crosland Copyright 2006
 */

public class SPROGCSMenu extends JMenu {

    private SprogSystemConnectionMemo _memo = null;

    public SPROGCSMenu(SprogSystemConnectionMemo memo) {

        super();
        _memo = memo;

        setText(memo.getUserName());

        add(new jmri.jmrix.sprog.sprogslotmon.SprogSlotMonAction(Bundle.getMessage("SprogSlotMonitorTitle"), _memo));
        add(new jmri.jmrix.sprog.sprogmon.SprogMonAction(Bundle.getMessage("MonitorXTitle", "SPROG"), _memo));
        add(new jmri.jmrix.sprog.packetgen.SprogPacketGenAction(Bundle.getMessage("MenuItemSendCommand"), _memo));
        add(new jmri.jmrix.sprog.console.SprogConsoleAction(Bundle.getMessage("SprogConsoleTitle"), _memo));
        add(new jmri.jmrix.sprog.update.SprogVersionAction(Bundle.getMessage("GetSprogFirmwareVersion"), _memo));
        add(new jmri.jmrix.sprog.update.Sprogv4UpdateAction(Bundle.getMessage("SprogXFirmwareUpdate", " v3/v4"), _memo));
        add(new jmri.jmrix.sprog.update.SprogIIUpdateAction(Bundle.getMessage("SprogXFirmwareUpdate", " II"), _memo));
    }

}

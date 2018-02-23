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
        add(new javax.swing.JSeparator());
        add(new jmri.jmrix.sprog.update.SprogVersionAction(Bundle.getMessage("GetSprogFirmwareVersion"), _memo));
        // Removed to avoid confusion with newer SPROG II and 3 that have now reached v3 and v4:
        //add(new jmri.jmrix.sprog.update.Sprogv4UpdateAction(Bundle.getMessage("SprogXFirmwareUpdate", " v3/v4"), _memo));
        // Removed as attempting a firmware update in command station mode is not expected to work
        //add(new jmri.jmrix.sprog.update.SprogIIUpdateAction(Bundle.getMessage("SprogXFirmwareUpdate", " II"), _memo));
    }

}

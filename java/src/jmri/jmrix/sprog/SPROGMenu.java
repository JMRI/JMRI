package jmri.jmrix.sprog;

import javax.swing.JMenu;

/**
 * Create a Systems menu containing the Jmri SPROG-specific tools.
 *
 * @author Bob Jacobsen Copyright 2003
 */
public class SPROGMenu extends JMenu {

    public SPROGMenu(SprogSystemConnectionMemo memo) {
        super();

        if (memo != null) {
            setText(memo.getUserName());
        } else {
            setText("Sprog");
        }

        if (memo != null) {
            add(new jmri.jmrix.sprog.sprogmon.SprogMonAction(Bundle.getMessage("MonitorXTitle", "SPROG"), memo));
            add(new jmri.jmrix.sprog.packetgen.SprogPacketGenAction(Bundle.getMessage("MenuItemSendCommand"), memo));
            add(new jmri.jmrix.sprog.console.SprogConsoleAction(Bundle.getMessage("SprogConsoleTitle"), memo));
            add(new javax.swing.JSeparator());
            add(new jmri.jmrix.sprog.update.SprogVersionAction(Bundle.getMessage("GetSprogFirmwareVersion"), memo));
            // Removed to avoid confusion with newer SPROG II and 3 that have now reached v3 and v4:
            //add(new jmri.jmrix.sprog.update.Sprogv4UpdateAction(Bundle.getMessage("SprogXFirmwareUpdate", " v3/v4"), memo));
            add(new jmri.jmrix.sprog.update.SprogIIUpdateAction(Bundle.getMessage("SprogXFirmwareUpdate"), memo));
        }
    }

}

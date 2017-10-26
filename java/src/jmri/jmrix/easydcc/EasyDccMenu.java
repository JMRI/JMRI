package jmri.jmrix.easydcc;

import java.util.ResourceBundle;
import javax.swing.JMenu;

/**
 * Create a "Systems" menu containing the Jmri EasyDCC-specific tools.
 *
 * @author Bob Jacobsen Copyright 2003
 */
public class EasyDccMenu extends JMenu {

    public EasyDccMenu(EasyDccSystemConnectionMemo memo) {

        super();
        if (memo != null) {
            setText(memo.getUserName());
        } else {
            setText("Easy DCC");
        }

        // do we have an EasyDccTrafficController?
        setEnabled(memo.getTrafficController() != null); // disable menu, no connection, no tools!
        if (memo != null) {
            add(new jmri.jmrix.easydcc.easydccmon.EasyDccMonAction(Bundle.getMessage("MonitorXTitle", "EasyDCC"), memo));
            add(new jmri.jmrix.easydcc.packetgen.EasyDccPacketGenAction(Bundle.getMessage("MenuItemSendCommand"), memo));
        }
    }

}

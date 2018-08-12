package jmri.jmrix.secsi;

import java.util.ResourceBundle;
import javax.swing.JMenu;

/**
 * Create a "Systems" menu containing the JMRI SECSI-specific tools.
 *
 * @author Bob Jacobsen Copyright 2003, 2006, 2007
 */
public class SecsiMenu extends JMenu {

    public SecsiMenu(String name, SecsiSystemConnectionMemo memo) {
        this(memo);
        setText(name);
    }

    public SecsiMenu(SecsiSystemConnectionMemo memo) {

        super();

        if (memo != null) {
            setText(memo.getUserName());
        } else {
            setText(Bundle.getMessage("MenuSystem"));
        }

        if (memo != null) {
            // do we have a SerialTrafficController?
            setEnabled(memo.getTrafficController() != null); // disable menu, no connection, no tools!
            add(new jmri.jmrix.secsi.serialmon.SerialMonAction(Bundle.getMessage("MenuItemCommandMonitor"), memo));
            add(new jmri.jmrix.secsi.packetgen.SerialPacketGenAction(Bundle.getMessage("MenuItemSendCommand"), memo));
            add(new jmri.jmrix.secsi.nodeconfig.NodeConfigAction(Bundle.getMessage("ConfigNodesTitle"), memo));
        }
    }

}

package jmri.jmrix.tmcc;

import javax.swing.JMenu;

/**
 * Create a "Systems" menu containing the Jmri TMCC-specific tools
 *
 * @author	Bob Jacobsen Copyright 2003, 2006
 */
public class TmccMenu extends JMenu {

    public TmccMenu(String name, TmccSystemConnectionMemo memo) {
        this(memo);
        setText(name);
    }

    public TmccMenu(TmccSystemConnectionMemo memo) {
        super();
        if (memo != null) {
            setText(memo.getUserName());
        } else {
            setText(Bundle.getMessage("MenuTMCC"));
        }

        if (memo != null) {
            // do we have a TmccTrafficController?
            setEnabled(memo.getTrafficController() != null); // disable menu, no connection, no tools!
            add(new jmri.jmrix.tmcc.serialmon.SerialMonAction(Bundle.getMessage("MenuItemCommandMonitor"), memo));
            add(new jmri.jmrix.tmcc.packetgen.SerialPacketGenAction(Bundle.getMessage("MenuItemSendCommand"), memo));
        }
    }

}

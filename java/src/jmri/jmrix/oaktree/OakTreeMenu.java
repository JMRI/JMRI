package jmri.jmrix.oaktree;

import java.util.ResourceBundle;
import javax.swing.JMenu;

/**
 * Create a "Systems" menu containing the JMRI Oak Tree-specific tools.
 *
 * @author Bob Jacobsen Copyright 2003, 2006
 */
public class OakTreeMenu extends JMenu {

    public OakTreeMenu(String name, OakTreeSystemConnectionMemo memo) {
        this(memo);
        setText(name);
    }

    public OakTreeMenu(OakTreeSystemConnectionMemo memo) {
        super();

        setText(Bundle.getMessage("MenuOakTree"));

        if (memo != null) {
            // do we have a SerialTrafficController?
            setEnabled(memo.getTrafficController() != null); // disable menu, no connection, no tools!
            add(new jmri.jmrix.oaktree.serialmon.SerialMonAction(Bundle.getMessage("MenuItemCommandMonitor"), memo));
            add(new jmri.jmrix.oaktree.packetgen.SerialPacketGenAction(Bundle.getMessage("MenuItemSendCommand"), memo));
            add(new jmri.jmrix.oaktree.nodeconfig.NodeConfigAction(Bundle.getMessage("ConfigNodesTitle"), memo));
        }
    }

}

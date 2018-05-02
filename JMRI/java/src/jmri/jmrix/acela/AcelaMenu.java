package jmri.jmrix.acela;

import javax.swing.JMenu;

/**
 * Create a "Systems" menu containing the Jmri Acela-specific tools
 * Based on CMRI serial example, modified to establish Acela support.
 *
 * @author Bob Jacobsen Copyright 2003
 * @author Bob Coleman, Copyright (C) 2007, 2008
 */
public class AcelaMenu extends JMenu {

    public AcelaMenu(String name, AcelaSystemConnectionMemo memo) {
        this(memo);
        setText(name);
    }

    public AcelaMenu(AcelaSystemConnectionMemo memo) {
        super();

        if (memo != null) {
            setText(memo.getUserName());
        } else {
            setText("Acela");
        }

        if (memo != null) {
            // do we have an AcelaTrafficController?
            setEnabled(memo.getTrafficController() != null); // disable menu, no connection, no tools!
            add(new jmri.jmrix.acela.acelamon.AcelaMonAction(Bundle.getMessage("AcelaMonitorTitle"), memo));
            add(new jmri.jmrix.acela.packetgen.AcelaPacketGenAction(Bundle.getMessage("MenuItemSendCommand"), memo));
            add(new jmri.jmrix.acela.nodeconfig.NodeConfigAction(Bundle.getMessage("ConfigNodesTitle"), memo));
        }
    }

}

package jmri.jmrix.acela;

import java.util.ResourceBundle;
import javax.swing.JMenu;

/**
 * Create a "Systems" menu containing the Jmri Acela-specific tools
 * Based on CMRI serial example, modified to establish Acela support.
 *
 * @author Bob Jacobsen Copyright 2003
 * @author Bob Coleman, Copyright (C) 2007, 2008
 */
public class AcelaMenu extends JMenu {

    private AcelaSystemConnectionMemo _memo = null;

    public AcelaMenu(String name, AcelaSystemConnectionMemo memo) {
        this(memo);
        setText(name);
    }

    public AcelaMenu(AcelaSystemConnectionMemo memo) {
        super();
        _memo = memo;

        if (memo != null) {
            setText(memo.getUserName());
        } else {
            setText("Acela");
        }

        add(new jmri.jmrix.acela.acelamon.AcelaMonAction(Bundle.getMessage("AcelaMonitorTitle"), _memo));
        add(new jmri.jmrix.acela.packetgen.AcelaPacketGenAction(Bundle.getMessage("AcelaSendCommandTitle"), _memo));
        add(new jmri.jmrix.acela.nodeconfig.NodeConfigAction(Bundle.getMessage("ConfigNodesTitle"), _memo));
    }

}

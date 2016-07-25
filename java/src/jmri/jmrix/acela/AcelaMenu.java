package jmri.jmrix.acela;

import java.util.ResourceBundle;
import javax.swing.JMenu;

/**
 * Create a "Systems" menu containing the Jmri Acela-specific tools
 *
 * @author	Bob Jacobsen Copyright 2003
 * @version $Revision$
 *
 * @author	Bob Coleman, Copyright (C) 2007, 2008 Based on CMRI serial example,
 * modified to establish Acela support.
 */
public class AcelaMenu extends JMenu {

    private AcelaSystemConnectionMemo _memo = null;

    public AcelaMenu(String name,AcelaSystemConnectionMemo memo) {
        this(memo);
        setText(name);
    }

    public AcelaMenu(AcelaSystemConnectionMemo memo) {
        super();
        _memo = memo;
        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.JmrixSystemsBundle");

        setText(rb.getString("MenuItemAcela"));

        add(new jmri.jmrix.acela.acelamon.AcelaMonAction(rb.getString("MenuItemCommandMonitor"),_memo));
        add(new jmri.jmrix.acela.packetgen.AcelaPacketGenAction(rb.getString("MenuItemSendCommand"),_memo));
        add(new jmri.jmrix.acela.nodeconfig.NodeConfigAction(rb.getString("MenuItemConfigNodes"),_memo));
    }
}

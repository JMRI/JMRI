// AcelaMenu.java
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

    /**
     *
     */
    private static final long serialVersionUID = -8525208553585140664L;

    public AcelaMenu(String name) {
        this();
        setText(name);
    }

    public AcelaMenu() {
        super();

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.JmrixSystemsBundle");

        setText(rb.getString("MenuItemAcela"));

        add(new jmri.jmrix.acela.acelamon.AcelaMonAction(rb.getString("MenuItemCommandMonitor")));
        add(new jmri.jmrix.acela.packetgen.AcelaPacketGenAction(rb.getString("MenuItemSendCommand")));
        add(new jmri.jmrix.acela.nodeconfig.NodeConfigAction(rb.getString("MenuItemConfigNodes")));
    }
}

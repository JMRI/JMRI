/**
 * SPROGCSMenu.java
 */
package jmri.jmrix.sprog;

import java.util.ResourceBundle;
import javax.swing.JMenu;

/**
 * Create a "Systems" menu containing the Jmri SPROG-specific tools
 *
 * @author	Andrew Crosland Copyright 2006
 * @version $Revision$
 */
public class SPROGCSMenu extends JMenu {

    /**
     *
     */
    private static final long serialVersionUID = -6206995194993926789L;

    public SPROGCSMenu(SprogSystemConnectionMemo memo) {
        this();
        setText(memo.getUserName());
    }

    public SPROGCSMenu() {

        super();

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.JmrixSystemsBundle");

        // setText(rb.getString("MenuSystems"));
        setText("SPROG");

        add(new jmri.jmrix.sprog.sprogslotmon.SprogSlotMonAction(rb.getString("MenuItemSlotMonitor")));
        add(new jmri.jmrix.sprog.sprogmon.SprogMonAction(rb.getString("MenuItemCommandMonitor")));
        add(new jmri.jmrix.sprog.packetgen.SprogPacketGenAction(rb.getString("MenuItemSendCommand")));
        add(new jmri.jmrix.sprog.console.SprogConsoleAction(rb.getString("MenuItemConsole")));
        add(new jmri.jmrix.sprog.update.SprogVersionAction("Get SPROG Firmware Version"));
        add(new jmri.jmrix.sprog.update.Sprogv4UpdateAction("SPROG v3/v4 Firmware Update"));
        add(new jmri.jmrix.sprog.update.SprogIIUpdateAction("SPROG II Firmware Update"));
        add(new jmri.jmrix.sprog.swing.PowerPanelAction("SPROG Power Control"));

    }

}

/* @(#)SprogCSMenu.java */

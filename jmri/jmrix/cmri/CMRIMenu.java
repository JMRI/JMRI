// CMRIMenu.java

package jmri.jmrix.cmri;

import java.util.ResourceBundle;

import javax.swing.JMenu;

/**
 * Create a "Systems" menu containing the Jmri CMRI-specific tools
 *
 * @author	Bob Jacobsen   Copyright 2003
 * @version     $Revision: 1.1 $
 */
public class CMRIMenu extends JMenu {
    public CMRIMenu(String name) {
        this();
        setText(name);
    }

    public CMRIMenu() {

        super();

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.JmrixSystemsBundle");

        // setText(rb.getString("MenuSystems"));
        setText(rb.getString("MenuItemCMRI"));

        add(new jmri.jmrix.cmri.serial.serialmon.SerialMonAction(rb.getString("MenuItemCommandMonitor")));
        add(new jmri.jmrix.cmri.serial.packetgen.SerialPacketGenAction(rb.getString("MenuItemSendCommand")));

    }

}



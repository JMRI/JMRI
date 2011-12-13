// MrcMenu.java

package jmri.jmrix.mrc;

import java.util.ResourceBundle;

import javax.swing.JMenu;

/**
 * Create a "Systems" menu containing the Jmri MRC-specific tools
 *
 * @author	Bob Jacobsen   Copyright 2003
 * @version     $Revision$
 */
public class MrcMenu extends JMenu {
    public MrcMenu(String name) {
        this();
        setText(name);
    }

    public MrcMenu() {

        super();

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.JmrixSystemsBundle");

        // setText(rb.getString("MenuSystems"));
        setText(rb.getString("MenuItemMRC"));

        add(new jmri.jmrix.mrc.mrcmon.MrcMonAction(rb.getString("MenuItemCommandMonitor")));
/*         add(new jmri.jmrix.mrc.packetgen.MrcPacketGenAction(rb.getString("MenuItemSendCommand"))); */

    }

}



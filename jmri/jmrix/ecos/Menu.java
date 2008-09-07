// Menu.java

package jmri.jmrix.ecos;

import java.util.ResourceBundle;

import javax.swing.JMenu;

/**
 * Create a "Systems" menu containing the Jmri ECOS-specific tools.
 *
 * @author	Bob Jacobsen   Copyright 2003, 2008
 * @version     $Revision: 1.1 $
 */
public class Menu extends JMenu {
    public Menu(String name) {
        this();
        setText(name);
    }

    public Menu() {

        super();

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.JmrixSystemsBundle");

        setText(rb.getString("MenuItemEcos"));

        
        add(new jmri.jmrix.ecos.swing.monitor.MonAction(rb.getString("MenuItemCommandMonitor")));
        add(new jmri.jmrix.ecos.swing.packetgen.PacketGenAction(rb.getString("MenuItemSendCommand")));
        add(new jmri.jmrix.ecos.swing.statusframe.StatusFrameAction("ECoS Info"));
    }

}

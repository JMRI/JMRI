// Mx1Menu.java

package jmri.jmrix.zimo;

import java.util.ResourceBundle;

import javax.swing.JMenu;

/**
 * Create a "Systems" menu containing the Jmri Mx-1-specific tools
 *
 * @author	Bob Jacobsen   Copyright 2003
 * @version     $Revision: 1.1 $
 */
public class Mx1Menu extends JMenu {
    public Mx1Menu(String name) {
        this();
        setText(name);
    }

    public Mx1Menu() {

        super();

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.JmrixSystemsBundle");

        setText(rb.getString("MenuItemZimo"));

        add(new jmri.jmrix.zimo.zimomon.Mx1MonAction(rb.getString("MenuItemCommandMonitor")));
        add(new jmri.jmrix.zimo.packetgen.ZimoPacketGenAction(rb.getString("MenuItemSendCommand")));

    }

}



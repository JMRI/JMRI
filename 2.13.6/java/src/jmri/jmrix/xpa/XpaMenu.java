/**
 * XpaMenu.java
 */

package jmri.jmrix.xpa;

import java.util.ResourceBundle;

import javax.swing.JMenu;

/**
 * Create a "Systems" menu containing the Jmri XPA-specific tools
 *
 * @author	Paul Bender   Copyright 2004
 * @version     $Revision$
 */
public class XpaMenu extends JMenu {
    public XpaMenu(String name) {
        this();
        setText(name);
    }

    public XpaMenu() {

        super();

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.JmrixSystemsBundle");
        ResourceBundle rb1 = ResourceBundle.getBundle("jmri.jmrix.xpa.XpaBundle");

        setText(rb1.getString("MenuXpa"));

        add(new jmri.jmrix.xpa.xpamon.XpaMonAction(rb.getString("MenuItemCommandMonitor")));
        add(new jmri.jmrix.xpa.packetgen.XpaPacketGenAction(rb.getString("MenuItemSendCommand")));
	add(new jmri.jmrix.xpa.xpaconfig.XpaConfigureAction(rb1.getString("MenuItemXpaConfigTool")));

    }

}



// WangrowMenu.java

package jmri.jmrix.wangrow;

import java.util.ResourceBundle;

import javax.swing.JMenu;

/**
 * Create a "Systems" menu containing the Jmri NCE-specific tools.
 * <P>
 * Note that this is still using specific tools from the
 * {@link jmri.jmrix.nce} package.
 *
 * @author	Bob Jacobsen   Copyright 2003
 * @version     $Revision: 1.2 $
 */
public class WangrowMenu extends JMenu {
    public WangrowMenu(String name) {
        this();
        setText(name);
    }

    public WangrowMenu() {

        super();

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.JmrixSystemsBundle");

        setText(rb.getString("MenuItemWangrow"));

        add(new jmri.jmrix.nce.ncemon.NceMonAction(rb.getString("MenuItemCommandMonitor")));
        add(new jmri.jmrix.nce.packetgen.NcePacketGenAction(rb.getString("MenuItemSendCommand")));
    }
}



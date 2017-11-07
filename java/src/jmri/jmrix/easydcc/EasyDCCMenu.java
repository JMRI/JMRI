package jmri.jmrix.easydcc;

import java.util.ResourceBundle;
import javax.swing.JMenu;

/**
 * Create a "Systems" menu containing the Jmri EasyDCC-specific tools
 *
 * @author Bob Jacobsen Copyright 2003
 */
public class EasyDCCMenu extends JMenu {

    public EasyDCCMenu(String name) {
        this();
        setText(name);
    }

    public EasyDCCMenu() {

        super();

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.JmrixSystemsBundle");

        // setText(rb.getString("MenuSystems"));
        setText(rb.getString("MenuItemEasyDCC"));

        add(new jmri.jmrix.easydcc.easydccmon.EasyDccMonAction(rb.getString("MenuItemCommandMonitor")));
        add(new jmri.jmrix.easydcc.packetgen.EasyDccPacketGenAction(rb.getString("MenuItemSendCommand")));

    }

}

// OpenLcbMenu.java

package jmri.jmrix.openlcb;

import java.util.ResourceBundle;

import javax.swing.JMenu;

/**
 * Create a menu containing the JMRI OpenLCB-specific tools
 *
 * @author	Bob Jacobsen   Copyright 2010
 * @version     $Revision$
 */
public class OpenLcbMenu extends JMenu {
    public OpenLcbMenu(jmri.jmrix.openlcb.OlcbSystemConnectionMemo memo) {

        super();

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.JmrixSystemsBundle");

        setText(rb.getString("MenuItemOpenLCB"));
        
        add(new jmri.jmrix.openlcb.swing.monitor.MonitorAction(rb.getString("MenuItemConsole")));
        add(new jmri.jmrix.openlcb.swing.send.OpenLcbCanSendAction(rb.getString("MenuItemSendFrame")));

        //add(new jmri.jmrix.can.cbus.swing.nodeconfig.NodeConfigToolAction("Node Config Tool"));

    }

}



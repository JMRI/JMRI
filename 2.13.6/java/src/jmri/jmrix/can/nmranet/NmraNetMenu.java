// NmraNetMenu.java

package jmri.jmrix.can.nmranet;

import java.util.ResourceBundle;

import javax.swing.JMenu;

/**
 * Create a menu containing the JMRI NmraNet-specific tools
 *
 * @author	Bob Jacobsen   Copyright 2009
 * @version     $Revision$
 */
public class NmraNetMenu extends JMenu {
    public NmraNetMenu(String name) {
        this();
        //setText(name);
        setText("NMRAnet");        
    }

    public NmraNetMenu() {

        super();

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.JmrixSystemsBundle");

        //setText(rb.getString("MenuItemCAN"));
        setText("NMRAnet");
        
        add(new jmri.jmrix.can.swing.monitor.MonitorAction(rb.getString("MenuItemConsole")));
        add(new jmri.jmrix.can.swing.send.CanSendAction(rb.getString("MenuItemSendFrame")));

    }

}



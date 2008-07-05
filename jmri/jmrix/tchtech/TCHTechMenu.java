/*
 * TCHTechMenu.java
 *
 * Created on August 17, 2007, 6:45 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
/* @author	Bob Jacobsen   Copyright 2003
/* @version     $Revision: 1.2 $
/**
 *
 * @author tim
 */
package jmri.jmrix.tchtech;

import java.util.ResourceBundle;

import javax.swing.JMenu;


/**
 * Create a "Systems" menu containing the Jmri TCH Technology-specific tools
 * @author Tim Hatch
 */
public class TCHTechMenu extends JMenu {
    public TCHTechMenu(String name) {
        this();
        setText(name);
    }

    public TCHTechMenu() {

        super();

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.JmrixSystemsBundle");

        setText(rb.getString("MenuItemTCHTech"));
        add(new jmri.jmrix.tchtech.serial.nodeconfig.NodeConfigAction());
        add(new javax.swing.JSeparator());
        add(new jmri.jmrix.tchtech.serial.diagnostic.DiagnosticAction(rb.getString("MenuItemDiagnostics")));
        add(new javax.swing.JSeparator());
        add(new jmri.jmrix.tchtech.serial.serialmon.SerialMonAction(rb.getString("MenuItemCommandMonitor")));
        add(new jmri.jmrix.tchtech.serial.packetgen.SerialPacketGenAction(rb.getString("MenuItemSendCommand")));
        add(new javax.swing.JSeparator());
        add(new jmri.jmrix.tchtech.serial.assignment.ListAction(rb.getString("MenuItemAssignments")));
    }

}






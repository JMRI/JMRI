package jmri.jmrix.dccpp.swing;

import java.util.ResourceBundle;
import javax.swing.JMenu;

/**
 * Create a menu containing the DCC++ specific tools.
 *
 * @author Paul Bender Copyright 2003,2010
 * @author Mark Underwood Copyright 2015
 *
 * Based on XNetMenu by Paul Bender
 */
public class DCCppMenu extends JMenu {


    public DCCppMenu(String name, jmri.jmrix.dccpp.DCCppSystemConnectionMemo memo) {
        this(memo);
        setText(name);
    }

    public DCCppMenu(jmri.jmrix.dccpp.DCCppSystemConnectionMemo memo) {

        super();
        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.dccpp.swing.DCCppSwingBundle");

        if (memo != null) {
            setText(memo.getUserName());
        } else {
            setText(rb.getString("MenuDCC++"));
        }

        add(new jmri.jmrix.dccpp.swing.mon.DCCppMonAction());
        if (memo != null) {
            add(new jmri.jmrix.dccpp.swing.packetgen.PacketGenAction(rb.getString("MenuItemSendDCCppCommand"), memo));
        }
        add(new jmri.jmrit.ampmeter.AmpMeterAction());
        add(new jmri.jmrix.dccpp.swing.ConfigBaseStationAction(rb.getString("MenuItemConfigBaseStation"), null));
        add(new javax.swing.JSeparator());
        add(new jmri.jmrix.dccpp.dccppovertcp.ServerAction(rb.getString("MenuItemDCCppOverTCPServer")));
    }

}

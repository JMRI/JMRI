// DCCppMenu.java
package jmri.jmrix.dccpp.swing;

import java.util.ResourceBundle;
import javax.swing.JMenu;

/**
 * Create a menu containing the DCC++ specific tools
 *
 * @author	Paul Bender Copyright 2003,2010
 * @author	Mark Underwood Copyright 2015
 * @version $Revision$
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

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.dccpp.DCCppBundle");

        if (memo != null) {
            setText(memo.getUserName());
        } else {
            setText(rb.getString("MenuDCC++"));
        }

        add(new jmri.jmrix.dccpp.swing.mon.DCCppMonAction());
	add(new jmri.jmrit.ampmeter.AmpMeterAction());
        //add(new jmri.jmrix.dccpp.swing.systeminfo.SystemInfoAction(rb.getString("MenuItemDCCppSystemInformation"), memo));
        //add(new jmri.jmrix.dccpp.swing.packetgen.PacketGenAction(rb.getString("MenuItemSendDCCppCommand"), memo));
        //add(new javax.swing.JSeparator());
        //add(new jmri.jmrix.dccpp.swing.stackmon.StackMonAction(rb.getString("MenuItemCSDatabaseManager"), memo));
        //add(new jmri.jmrix.dccpp.swing.li101.LI101Action(rb.getString("MenuItemLI101ConfigurationManager"), memo));
        //add(new jmri.jmrix.dccpp.swing.liusb.LIUSBConfigAction(rb.getString("MenuItemLIUSBConfigurationManager"), memo));
        //add(new jmri.jmrix.dccpp.swing.lz100.LZ100Action(rb.getString("MenuItemLZ100ConfigurationManager"), memo));
        //add(new jmri.jmrix.dccpp.swing.lzv100.LZV100Action(rb.getString("MenuItemLZV100ConfigurationManager"), memo));
        // The LV102 configuration works with OpsModeProgramming, so does not
        // need the system connection memo.
        //add(new jmri.jmrix.dccpp.swing.lv102.LV102Action(rb.getString("MenuItemLV102ConfigurationManager")));

    }

}

package jmri.jmrix.cmri;

import java.util.ResourceBundle;
import javax.swing.JMenu;

/**
 * Create a "Systems" menu containing the Jmri CMRI-specific tools
 *
 * @author	Bob Jacobsen Copyright 2003
 * @author	Chuck Catania  Copyright 2014, 2015, 2016
 */
public class CMRIMenu extends JMenu {

    public CMRIMenu(String name) {
        this();
        setText(name);
    }

    public CMRIMenu() {

        super();

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.cmri.CMRIBundle");

        setText(rb.getString("MenuCMRI"));

        add(new jmri.jmrix.cmri.serial.nodeconfigmanager.NodeConfigManagerAction(rb.getString("MenuItemNodeManager")));  //c2
        add(new javax.swing.JSeparator());
        add(new jmri.jmrix.cmri.serial.cmrinetmanager.CMRInetManagerAction(rb.getString("MenuItemCMRInetManager")));  //c2
        add(new jmri.jmrix.cmri.serial.cmrinetmanager.CMRInetMetricsAction(rb.getString("MenuItemCMRInetMetrics")));  //c2
        add(new javax.swing.JSeparator());
        add(new jmri.jmrix.cmri.serial.serialmon.SerialMonAction(rb.getString("MenuItemCommandMonitor")));
        add(new javax.swing.JSeparator());
        add(new jmri.jmrix.cmri.serial.assignment.ListAction(rb.getString("MenuItemAssignments")));
        add(new javax.swing.JSeparator());
        add(new jmri.jmrix.cmri.serial.diagnostic.DiagnosticAction(rb.getString("MenuItemDiagnostics")));
        add(new jmri.jmrix.cmri.serial.packetgen.SerialPacketGenAction(rb.getString("MenuItemSendCommand")));
//        add(new javax.swing.JSeparator());
//        add(new jmri.jmrix.cmri.serial.nodeconfig.NodeConfigAction(rb.getString("MenuItemConfigureNodes")));  //c2

}

}

package jmri.jmrix.cmri;

import javax.swing.JMenu;

/**
 * Create a "Systems" menu containing the Jmri CMRI-specific tools.
 *
 * @author Bob Jacobsen Copyright 2003
 * @author Chuck Catania  Copyright 2014, 2015, 2016, 2017, 2018
 */
public class CMRIMenu extends JMenu {

    public CMRIMenu(String name, CMRISystemConnectionMemo memo) {
        this(memo);
        setText(name);
    }

    public CMRIMenu(CMRISystemConnectionMemo memo) {
        super();
        if (memo != null) {
            setText(memo.getUserName());
        } else {
            setText(Bundle.getMessage("MenuCMRI"));
        }
        // Re-ordered c2
        if (memo != null) {
            add(new jmri.jmrix.cmri.serial.nodeconfigmanager.NodeConfigManagerAction(memo));
            add(new javax.swing.JSeparator());
            add(new jmri.jmrix.cmri.serial.cmrinetmanager.CMRInetManagerAction(Bundle.getMessage("MenuItemCMRInetManager"), memo));
            add(new jmri.jmrix.cmri.serial.cmrinetmanager.CMRInetMetricsAction(Bundle.getMessage("MenuItemCMRInetMetrics"), memo));
            add(new javax.swing.JSeparator());
            add(new jmri.jmrix.cmri.serial.serialmon.SerialMonAction(Bundle.getMessage("MenuItemCommandMonitor"), memo));
            add(new javax.swing.JSeparator());
            add(new jmri.jmrix.cmri.serial.assignment.ListAction(Bundle.getMessage("MenuItemAssignments"), memo));
            add(new javax.swing.JSeparator());
            add(new jmri.jmrix.cmri.serial.diagnostic.DiagnosticAction(Bundle.getMessage("MenuItemDiagnostics"), memo));
            //add(new jmri.jmrix.cmri.serial.packetgen.SerialPacketGenAction(Bundle.getMessage("MenuItemSendCommand"), memo));
            // add(new jmri.jmrix.cmri.serial.nodeconfig.NodeConfigAction(memo));
            // add(new jmri.jmrix.cmri.serial.assignment.ListAction(Bundle.getMessage("MenuItemAssignments"), memo));
        }
    }

}

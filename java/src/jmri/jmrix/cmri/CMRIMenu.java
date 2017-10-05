package jmri.jmrix.cmri;

import javax.swing.JMenu;

/**
 * Create a "Systems" menu containing the Jmri CMRI-specific tools
 *
 * @author Bob Jacobsen Copyright 2003
 * @author Chuck Catania  Copyright 2014, 2015, 2016, 2017
 */
public class CMRIMenu extends JMenu {

    CMRISystemConnectionMemo _memo = null;

    public CMRIMenu(String name, CMRISystemConnectionMemo memo) {
        this(memo);
        setText(name);
    }

    public CMRIMenu(CMRISystemConnectionMemo memo) {

        super();
        _memo = memo;

        if (memo != null) {
            setText(memo.getUserName());
        } else {
            setText(Bundle.getMessage("MenuCMRI"));
        }
        // Re-ordered c2
        add(new jmri.jmrix.cmri.serial.nodeconfigmanager.NodeConfigManagerAction(_memo));
        add(new javax.swing.JSeparator());
        add(new jmri.jmrix.cmri.serial.cmrinetmanager.CMRInetManagerAction(Bundle.getMessage("MenuItemCMRInetManager"),_memo));  //c2
//        add(new jmri.jmrix.cmri.serial.cmrinetmanager.CMRInetMetricsAction(rb.getString("MenuItemCMRInetMetrics")));  //c2
        add(new javax.swing.JSeparator());
        add(new jmri.jmrix.cmri.serial.serialmon.SerialMonAction(Bundle.getMessage("MenuItemCommandMonitor"),_memo));
        add(new javax.swing.JSeparator());
        add(new jmri.jmrix.cmri.serial.assignment.ListAction(Bundle.getMessage("MenuItemAssignments"),_memo));
        add(new javax.swing.JSeparator());
        add(new jmri.jmrix.cmri.serial.diagnostic.DiagnosticAction(Bundle.getMessage("MenuItemDiagnostics"),_memo));
        add(new jmri.jmrix.cmri.serial.packetgen.SerialPacketGenAction(Bundle.getMessage("MenuItemSendCommand"),_memo));
//        add(new jmri.jmrix.cmri.serial.nodeconfig.NodeConfigAction(_memo));
//        add(new jmri.jmrix.cmri.serial.assignment.ListAction(Bundle.getMessage("MenuItemAssignments"),_memo));
    }

}

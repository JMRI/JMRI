package jmri.jmrix.cmri;

import java.util.ResourceBundle;
import javax.swing.JMenu;

/**
 * Create a "Systems" menu containing the Jmri CMRI-specific tools
 *
 * @author	Bob Jacobsen Copyright 2003
 */
public class CMRIMenu extends JMenu {

    CMRISystemConnectionMemo _memo = null;

    public CMRIMenu(String name,CMRISystemConnectionMemo memo) {
        this(memo);
        setText(name);
    }

    public CMRIMenu(CMRISystemConnectionMemo memo) {

        super();
        _memo = memo;

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.cmri.CMRIBundle");

        setText(rb.getString("MenuCMRI"));

        add(new jmri.jmrix.cmri.serial.nodeconfigmanager.NodeConfigManagerAction(rb.getString("MenuItemNodeManager"),_memo));  //c2
        add(new javax.swing.JSeparator());
        add(new jmri.jmrix.cmri.serial.cmrinetmanager.CMRInetManagerAction(rb.getString("MenuItemCMRInetManager"),_memo));  //c2
        add(new jmri.jmrix.cmri.serial.cmrinetmanager.CMRInetMetricsAction(rb.getString("MenuItemCMRInetMetrics"),_memo));  //c2
        add(new jmri.jmrix.cmri.serial.serialmon.SerialMonAction(rb.getString("MenuItemCommandMonitor"),_memo));
        add(new javax.swing.JSeparator());
        add(new jmri.jmrix.cmri.serial.assignment.ListAction(rb.getString("MenuItemAssignments"),_memo));
        add(new javax.swing.JSeparator());
        add(new jmri.jmrix.cmri.serial.diagnostic.DiagnosticAction(rb.getString("MenuItemDiagnostics"),_memo));
        add(new jmri.jmrix.cmri.serial.packetgen.SerialPacketGenAction(rb.getString("MenuItemSendCommand"),_memo));
        //add(new javax.swing.JSeparator());
        //add(new jmri.jmrix.cmri.serial.nodeconfig.NodeConfigAction(_memo));
    }

}

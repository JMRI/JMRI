package jmri.jmrix.cmri;

import java.util.ResourceBundle;
import javax.swing.JMenu;

/**
 * Create a "Systems" menu containing the Jmri CMRI-specific tools
 *
 * @author Bob Jacobsen Copyright 2003
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

        if (memo != null) {
            setText(memo.getUserName());
        } else {
            setText(rb.getString("MenuCMRI"));
        }

        add(new jmri.jmrix.cmri.serial.serialmon.SerialMonAction(rb.getString("MenuItemCommandMonitor"),_memo));
        add(new jmri.jmrix.cmri.serial.packetgen.SerialPacketGenAction(rb.getString("MenuItemSendCommand"),_memo));
        add(new javax.swing.JSeparator());
        add(new jmri.jmrix.cmri.serial.diagnostic.DiagnosticAction(rb.getString("MenuItemDiagnostics"),_memo));
        add(new javax.swing.JSeparator());
        add(new jmri.jmrix.cmri.serial.nodeconfig.NodeConfigAction(_memo));
        add(new javax.swing.JSeparator());
        add(new jmri.jmrix.cmri.serial.assignment.ListAction(rb.getString("MenuItemAssignments"),_memo));
    }

}

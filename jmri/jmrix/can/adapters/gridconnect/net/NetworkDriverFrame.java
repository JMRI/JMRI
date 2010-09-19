// NetworkDriverFrame.java

package jmri.jmrix.can.adapters.gridconnect.net;

import javax.swing.*;

/**
 * Frame to control and connect NCE command station via NetworkDriver
 * interface.
 * @author			Bob Jacobsen   Copyright (C) 2003
 * @version			$Revision: 1.1 $
 */
public class NetworkDriverFrame extends jmri.jmrix.SerialPortFrame {

    public NetworkDriverFrame() {
        super("Open NCE network connection");
        adapter = new NetworkDriverAdapter();
    }

    public void openPortButtonActionPerformed(java.awt.event.ActionEvent e) throws jmri.jmrix.SerialConfigException {
        if ((String) portBox.getSelectedItem() != null) {
            // connect to the port
            adapter.openPort((String) portBox.getSelectedItem(),"SerialDriverFrame");

            adapter.configure();

            // hide this frame, since we're done
            setVisible(false);
        } else {
            // not selected
            JOptionPane.showMessageDialog(this, "Please select a port name first");
        }
    }

}

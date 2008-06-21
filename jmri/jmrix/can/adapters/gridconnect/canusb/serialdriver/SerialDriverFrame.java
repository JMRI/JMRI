// SerialDriverFrame.java

package jmri.jmrix.can.adapters.gridconnect.canusb.serialdriver;

import javax.swing.JOptionPane;

/**
 * Frame to control and connect CAN-USB via SerialDriver interface and comm port
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Revision: 1.1 $
 */
public class SerialDriverFrame extends jmri.jmrix.SerialPortFrame {
    
    public SerialDriverFrame() {
        super("Open CAN-USB connection");
        adapter = new SerialDriverAdapter();
    }
    
    public void openPortButtonActionPerformed(java.awt.event.ActionEvent e) throws jmri.jmrix.SerialConfigException {
        if ((String) portBox.getSelectedItem() != null) {
            // connect to the port
            adapter.configureBaudRate((String)baudBox.getSelectedItem());
            adapter.configureOption1((String)opt1Box.getSelectedItem());
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

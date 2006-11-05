// SerialDriverFrame.java

package jmri.jmrix.cmri.serial.serialdriver;

import javax.swing.*;

/**
 * Frame to control and connect C/MRI serial
 * @author    Bob Jacobsen   Copyright (C) 2001
 * @version   $Revision: 1.4 $
 */

public class SerialDriverFrame extends jmri.jmrix.SerialPortFrame {

    public SerialDriverFrame() {
        super("Open CMRI Serial Driver");
        adapter = new SerialDriverAdapter();
    }

    public void openPortButtonActionPerformed(java.awt.event.ActionEvent e) throws jmri.jmrix.SerialConfigException {
        if ((String) portBox.getSelectedItem() != null) {
            // connect to the port
            adapter.configureBaudRate((String)baudBox.getSelectedItem());
            adapter.configureOption1((String)opt1Box.getSelectedItem());
            String errCode = adapter.openPort((String) portBox.getSelectedItem(),"CMRIserial");

            if (errCode == null)	{
                adapter.configure();
                // hide this frame, since we're done
                setVisible(false);
            } else {
                JOptionPane.showMessageDialog(this,errCode);
            }
        } else {
            // not selected
            JOptionPane.showMessageDialog(this, "Please select a port name first");
        }
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SerialDriverFrame.class.getName());

}

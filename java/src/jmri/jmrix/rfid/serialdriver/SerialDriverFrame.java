// SerialDriverFrame.java

package jmri.jmrix.rfid.serialdriver;

import javax.swing.JOptionPane;

/**
 * Frame to control and connect RFID
 * @author      Bob Jacobsen   Copyright (C) 2001, 2006, 2007, 2008
 * @author      Matthew Harris  Copyright (C) 2011
 * @version     $Revision$
 * @since       2.11.4
 */

public class SerialDriverFrame extends jmri.jmrix.SerialPortFrame {

    public SerialDriverFrame() {
        super("Open RFID Device Driver");
        adapter = new SerialDriverAdapter();
    }

    public void openPortButtonActionPerformed(java.awt.event.ActionEvent e) throws jmri.jmrix.SerialConfigException {
        if ((String) portBox.getSelectedItem() != null) {
            // connect to the port
            adapter.configureBaudRate((String)baudBox.getSelectedItem());
            adapter.configureOption1((String)opt1Box.getSelectedItem());
            
            String errCode = adapter.openPort((String) portBox.getSelectedItem(),"RFID Device Serial");

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

}

// SerialDriverFrame.java

package jmri.jmrix.easydcc.serialdriver;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import jmri.jmrix.easydcc.EasyDccTrafficController;

/**
 * Frame to control and connect EasyDcc command station via SerialDriver interface and comm port
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Revision: 1.2 $
 */
public class SerialDriverFrame extends jmri.jmrix.SerialPortFrame {

    public SerialDriverFrame() {
        super("Open EasyDcc connection");
        adapter = new SerialDriverAdapter();
    }
    
    public void openPortButtonActionPerformed(java.awt.event.ActionEvent e) throws jmri.jmrix.SerialConfigException {
        if ((String) portBox.getSelectedItem() != null) {
            // connect to the port
            adapter.openPort((String) portBox.getSelectedItem(),"SerialDriverFrame");
            
            adapter.configure();
            
            // hide this frame, since we're done
            hide();
        } else {
            // not selected
            JOptionPane.showMessageDialog(this, "Please select a port name first");
        }
    }
    
}

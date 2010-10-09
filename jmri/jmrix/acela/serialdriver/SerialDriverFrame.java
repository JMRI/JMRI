// SerialDriverFrame.java

package jmri.jmrix.acela.serialdriver;

import javax.swing.*;

/**
 * Frame to control and connect Acela CTI interface via SerialDriver interface and comm port
 * @author	Bob Jacobsen   Copyright (C) 2001
 * @version	$Revision: 1.3 $
 *
 * @author	Bob Coleman, Copyright (C) 2007, 2008
 *              Based on Mrc example, modified to establish Acela support. 
 */

@Deprecated
public class SerialDriverFrame extends jmri.jmrix.SerialPortFrame {

    public SerialDriverFrame() {
        super("Open Acela connection");
        adapter = SerialDriverAdapter.instance();
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

/* @(#)SerialDriverFrame.java */
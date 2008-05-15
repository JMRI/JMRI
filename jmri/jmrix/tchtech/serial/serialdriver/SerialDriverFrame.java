/*
 * SerialDriverFrame.java
 *
 * Created on August 18, 2007, 10:37 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jmri.jmrix.tchtech.serial.serialdriver;

/**
 *
 * @author tim
 */
import javax.swing.*;

/**
 * Frame to control and connect TCH Technology SNIC serial
 * @author    Bob Jacobsen   Copyright (C) 2001
 * @version   $Revision: 1.1 $
 */

public class SerialDriverFrame extends jmri.jmrix.SerialPortFrame {

    public SerialDriverFrame() {
        super("Open TCH Technology NIC Serial Driver");
        adapter = new SerialDriverAdapter();
    }

    public void openPortButtonActionPerformed(java.awt.event.ActionEvent e) throws jmri.jmrix.SerialConfigException {
        if ((String) portBox.getSelectedItem() != null) {
            // connect to the port
            adapter.configureBaudRate((String)baudBox.getSelectedItem());
            adapter.configureOption1((String)opt1Box.getSelectedItem());
            String errCode = adapter.openPort((String) portBox.getSelectedItem(),"TCHTechserial");

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

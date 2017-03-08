package jmri.jmrix.lenz.swing.packetgen;

import jmri.jmrix.lenz.XNetMessage;
import jmri.jmrix.lenz.XNetTrafficController;

/**
 * Frame for user input of XpressNet messages
 *
 * @author	Bob Jacobsen Copyright (C) 2001,2002
  */
public class PacketGenFrame extends jmri.jmrix.swing.AbstractPacketGenFrame {

    final java.util.ResourceBundle rb = java.util.ResourceBundle.getBundle("jmri.jmrix.lenz.swing.XNetSwingBundle");

    @Override
    public void initComponents() throws Exception {
        super.initComponents();

        // all we need to do is set the title 
        setTitle(rb.getString("PacketGenFrameTitle"));

        // pack to cause display
        pack();
    }

    @Override
    public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {
        tc.sendXNetMessage(createPacket(packetTextField.getSelectedItem().toString()), null);
    }

    XNetMessage createPacket(String s) {
        if (s.equals("")) {
            return null; // message cannot be empty
        }
        XNetMessage m = new XNetMessage(s);
        return m;
    }

    // connect to the TrafficController
    public void connect(XNetTrafficController t) {
        tc = t;
    }

    // private data
    private XNetTrafficController tc = null;
}

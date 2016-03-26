package jmri.jmrix.roco.z21.swing.packetgen;

import jmri.jmrix.roco.z21.Z21Message;
import jmri.jmrix.roco.z21.Z21TrafficController;

/**
 * Frame for user input of XpressNet messages
 *
 * @author	Bob Jacobsen Copyright (C) 2001,2002
 */
public class PacketGenFrame extends jmri.jmrix.swing.AbstractPacketGenFrame {

    /**
     *
     */
    private static final long serialVersionUID = 7715995607748223001L;
    final java.util.ResourceBundle rb = java.util.ResourceBundle.getBundle("jmri.jmrix.roco.z21.z21ActionListBundle");

    public void initComponents() throws Exception {
        super.initComponents();

        // all we need to do is set the title 
        setTitle(rb.getString("jmri.jmrix.roco.z21.swing.packetgen.PacketGenActio"));

        // pack to cause display
        pack();
    }

    public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {
        tc.sendz21Message(createPacket(packetTextField.getSelectedItem().toString()), null);
    }

    Z21Message createPacket(String s) {
        if (s.equals("")) {
            return null; // message cannot be empty
        }
        Z21Message m = new Z21Message(s);
        return m;
    }

    // connect to the TrafficController
    public void connect(Z21TrafficController t) {
        tc = t;
    }

    // private data
    private Z21TrafficController tc = null;
}

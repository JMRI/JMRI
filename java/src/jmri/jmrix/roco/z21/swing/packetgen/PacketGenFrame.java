package jmri.jmrix.roco.z21.swing.packetgen;

import jmri.jmrix.roco.z21.Z21Message;
import jmri.jmrix.roco.z21.Z21TrafficController;

/**
 * Frame for user input of XpressNet messages.
 *
 * @author	Bob Jacobsen Copyright (C) 2001,2002
 */
public class PacketGenFrame extends jmri.jmrix.swing.AbstractPacketGenFrame {

    /** 
     * {@inheritDoc}
     */
    @Override
    public void initComponents() {
        super.initComponents();

        // all we need to do is set the title 
        setTitle(Bundle.getMessage("SendZ21MessageTitle"));

        // pack to cause display
        pack();
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {
        tc.sendz21Message(createPacket(packetTextField.getSelectedItem().toString()), null);
    }

    Z21Message createPacket(String s) {
        if (s.equals("")) {
            return null; // message cannot be empty
        }
        return new Z21Message(s);
    }

    // connect to the TrafficController
    public void connect(Z21TrafficController t) {
        tc = t;
    }

    // private data
    private Z21TrafficController tc = null;
}

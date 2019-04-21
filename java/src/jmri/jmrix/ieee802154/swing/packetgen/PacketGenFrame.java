package jmri.jmrix.ieee802154.swing.packetgen;

import jmri.jmrix.ieee802154.IEEE802154Message;
import jmri.jmrix.ieee802154.IEEE802154TrafficController;

/**
 * Frame for user input of XpressNet messages
 *
 * @author Bob Jacobsen Copyright (C) 2001,2002
 */
public class PacketGenFrame extends jmri.jmrix.swing.AbstractPacketGenFrame {

    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponents() {
        super.initComponents();

        // all we need to do is set the title 
        Bundle.getMessage("jmri.jmrix.ieee802154.swing.packetgen.PacketGenAction");

        // pack to cause display
        pack();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {
        tc.sendIEEE802154Message(createPacket(packetTextField.getSelectedItem().toString()), null);
    }

    IEEE802154Message createPacket(String s) {
        if (s.equals("")) {
            return null; // message cannot be empty
        }
        IEEE802154Message m = new IEEE802154Message(s, s.length());
        return m;
    }

    // connect to the TrafficController
    public void connect(IEEE802154TrafficController t) {
        tc = t;
    }

    // private data
    private IEEE802154TrafficController tc = null;
}

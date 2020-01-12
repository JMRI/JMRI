package jmri.jmrix.dccpp.swing.packetgen;

import jmri.jmrix.dccpp.DCCppMessage;
import jmri.jmrix.dccpp.DCCppTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for user input of XpressNet messages
 *
 * @author Bob Jacobsen Copyright (C) 2001,2002
 * @author      Mark Underwood Copyright (C) 2015
 */
public class PacketGenFrame extends jmri.jmrix.swing.AbstractPacketGenFrame {

    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponents() {
        super.initComponents();

        // all we need to do is set the title 
        setTitle(Bundle.getMessage("PacketGenFrameTitle"));
        packetTextField.setToolTipText("Enter packet as a text string without the < > brackets");

        // pack to cause display
        pack();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {
        tc.sendDCCppMessage(createPacket(packetTextField.getSelectedItem().toString()), null);
    }

    DCCppMessage createPacket(String s) {
        if (s.equals("")) {
            log.debug("Empty Packet...");
            return null; // message cannot be empty
        }
        // Strip off the brackets, if present.
        if (s.charAt(0) == '<') {
            s = s.substring(1);
        }
        if (s.lastIndexOf('>') != -1) {
            s = s.substring(0, s.lastIndexOf('>'));
        }
        DCCppMessage m = DCCppMessage.parseDCCppMessage(s);
        log.debug("Sending: {}", m);
        return(m);
    }

    // connect to the TrafficController
    public void connect(DCCppTrafficController t) {
        tc = t;
    }

    // private data
    private DCCppTrafficController tc = null;
    
    private static final Logger log = LoggerFactory.getLogger(PacketGenFrame.class);

}

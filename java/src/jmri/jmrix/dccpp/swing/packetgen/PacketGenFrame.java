// PacketGenFrame.java
package jmri.jmrix.dccpp.swing.packetgen;

import jmri.jmrix.dccpp.DCCppMessage;
import jmri.jmrix.dccpp.DCCppTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for user input of XpressNet messages
 *
 * @author	Bob Jacobsen Copyright (C) 2001,2002
 * @author      Mark Underwood Copyright (C) 2015
 * @version	$Revision$
 */
public class PacketGenFrame extends jmri.jmrix.swing.AbstractPacketGenFrame {

    final java.util.ResourceBundle rb = java.util.ResourceBundle.getBundle("jmri.jmrix.dccpp.swing.DCCppSwingBundle");

    public void initComponents() throws Exception {
        super.initComponents();

        // all we need to do is set the title 
        setTitle(rb.getString("PacketGenFrameTitle"));
        packetTextField.setToolTipText("Enter packet as a text string without the < > brackets");

        // pack to cause display
        pack();
    }

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
        log.debug("Sending: {}", m.toString());
        return(m);
    }

    // connect to the TrafficController
    public void connect(DCCppTrafficController t) {
        tc = t;
    }

    // private data
    private DCCppTrafficController tc = null;
    
    private final static Logger log = LoggerFactory.getLogger(PacketGenFrame.class.getName());

}

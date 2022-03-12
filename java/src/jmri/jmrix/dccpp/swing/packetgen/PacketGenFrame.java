package jmri.jmrix.dccpp.swing.packetgen;

import jmri.jmrix.dccpp.DCCppMessage;
import jmri.jmrix.dccpp.DCCppSystemConnectionMemo;
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

    // private data
    private DCCppTrafficController _tc = null;
    private DCCppSystemConnectionMemo _memo;

    public PacketGenFrame(DCCppSystemConnectionMemo memo) {
        super();
        _tc = memo.getDCCppTrafficController();
        _memo = memo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponents() {
        super.initComponents();

        // all we need to do is set the title, include prefix in event of multiple connections 
        setTitle(Bundle.getMessage("PacketGenFrameTitle") + " (" + _memo.getSystemPrefix() + ")");
        packetTextField.setToolTipText("Enter packet as a text string without the < > brackets");

        // pack to cause display
        pack();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {
        DCCppMessage msg = createPacket(packetTextField.getSelectedItem().toString());
        if (msg != null) {
            _tc.sendDCCppMessage(msg, null);
        } else {
            log.error("Frame packet '{}' not valid", packetTextField.getSelectedItem().toString());
        }
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
        DCCppMessage m = new DCCppMessage(s);
        log.debug("Sending: '{}'", m);
        return(m);
    }
   
    private static final Logger log = LoggerFactory.getLogger(PacketGenFrame.class);

}

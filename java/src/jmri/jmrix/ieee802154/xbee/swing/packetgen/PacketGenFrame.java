package jmri.jmrix.ieee802154.xbee.swing.packetgen;

import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.packet.GenericXBeePacket;
import com.digi.xbee.api.packet.XBeeAPIPacket;
import jmri.jmrix.ieee802154.xbee.XBeeTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for user input of Xbee ieee802154 messages
 *
 * @author Bob Jacobsen Copyright (C) 2001,2002
 */
public class PacketGenFrame extends jmri.jmrix.swing.AbstractPacketGenFrame {

    final java.util.ResourceBundle rb = java.util.ResourceBundle.getBundle("jmri.jmrix.ieee802154.IEEE802154ActionListBundle");

    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponents() {
        super.initComponents();

        // all we need to do is set the title 
        setTitle(rb.getString("jmri.jmrix.ieee802154.xbee.swing.packetgen.PacketGenAction"));

        // pack to cause display
        pack();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {
        String packetString = packetTextField.getSelectedItem().toString();
        byte packetByteArray[] = jmri.util.StringUtil.bytesFromHexString(packetString);
        XBeeAPIPacket packet = GenericXBeePacket.createPacket(packetByteArray);
        try {
            tc.getXBee().sendPacketAsync(packet);
        } catch (XBeeException xbe) {
            log.error("Error Sending message to XBee: " + xbe);
        }
    }

    // connect to the TrafficController
    public void connect(XBeeTrafficController t) {
        tc = t;
    }

    // private data
    private XBeeTrafficController tc = null;

    private final static Logger log = LoggerFactory.getLogger(PacketGenFrame.class);

}

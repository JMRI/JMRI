// PacketGenFrame.java
package jmri.jmrix.lenz.packetgen;

import javax.swing.Box;
import javax.swing.BoxLayout;
import jmri.jmrix.lenz.XNetMessage;
import jmri.jmrix.lenz.XNetTrafficController;

/**
 * Frame for user input of XpressNet messages
 *
 * @author	Bob Jacobsen Copyright (C) 2001,2002
 * @version	$Revision$
 */
public class PacketGenFrame extends jmri.util.JmriJFrame {

    /**
     *
     */
    private static final long serialVersionUID = 3873950050093565384L;
    // member declarations
    javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
    javax.swing.JButton sendButton = new javax.swing.JButton();
    javax.swing.JTextField packetTextField = new javax.swing.JTextField(12);

    public PacketGenFrame() {
        super();
    }

    public void initComponents() throws Exception {
        // the following code sets the frame's initial state

        jLabel1.setText("Packet:");
        jLabel1.setVisible(true);

        sendButton.setText("Send");
        sendButton.setVisible(true);
        sendButton.setToolTipText("Send packet");

        packetTextField.setToolTipText("Enter packet as hex pairs, e.g. 82 7D");

        setTitle("Send XpressNet Packet");
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        getContentPane().add(jLabel1);
        getContentPane().add(packetTextField);
        getContentPane().add(sendButton);
        getContentPane().add(Box.createVerticalGlue());

        sendButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                sendButtonActionPerformed(e);
            }
        });

        // pack to cause display
        pack();
    }

    public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {
        tc.sendXNetMessage(createPacket(packetTextField.getText()), null);
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

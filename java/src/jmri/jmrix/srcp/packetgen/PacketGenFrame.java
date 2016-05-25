// SRCPPacketGenFrame.java
package jmri.jmrix.srcp.packetgen;

import java.awt.Dimension;
import javax.swing.BoxLayout;
import jmri.jmrix.srcp.SRCPMessage;
import jmri.jmrix.srcp.SRCPReply;
import jmri.jmrix.srcp.SRCPTrafficController;

/**
 * Description:	Frame for user input of SRCP messages
 *
 * @author	Bob Jacobsen Copyright (C) 2008
 * @version	$Revision$
 */
public class PacketGenFrame extends jmri.util.JmriJFrame implements jmri.jmrix.srcp.SRCPListener {

    /**
     *
     */
    private static final long serialVersionUID = -6069575872393573541L;
    // member declarations
    javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
    javax.swing.JButton sendButton = new javax.swing.JButton();
    javax.swing.JTextField packetTextField = new javax.swing.JTextField(12);

    public PacketGenFrame() {
        super();
    }

    public void initComponents() throws Exception {
        // the following code sets the frame's initial state

        jLabel1.setText("Command:");
        jLabel1.setVisible(true);

        sendButton.setText("Send");
        sendButton.setVisible(true);
        sendButton.setToolTipText("Send packet");

        packetTextField.setText("");
        packetTextField.setToolTipText("Enter command as ASCII string (hex not yet available)");
        packetTextField.setMaximumSize(
                new Dimension(packetTextField.getMaximumSize().width,
                        packetTextField.getPreferredSize().height
                )
        );

        setTitle("Send SRCP command");
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        getContentPane().add(jLabel1);
        getContentPane().add(packetTextField);
        getContentPane().add(sendButton);

        sendButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                sendButtonActionPerformed(e);
            }
        });

        // pack for display
        pack();
    }

    public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {
        SRCPMessage m = new SRCPMessage(packetTextField.getText().length() + 1);
        for (int i = 0; i < packetTextField.getText().length(); i++) {
            m.setElement(i, packetTextField.getText().charAt(i));
        }

        m.setElement(packetTextField.getText().length(), '\n');
        SRCPTrafficController.instance().sendSRCPMessage(m, this);
    }

    public void message(SRCPMessage m) {
    }  // ignore replies

    public void reply(SRCPReply r) {
    } // ignore replies

    public void reply(jmri.jmrix.srcp.parser.SimpleNode n) {
    } // ignore replies
}

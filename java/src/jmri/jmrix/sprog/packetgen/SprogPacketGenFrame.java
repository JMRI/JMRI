// SprogPacketGenFrame.java
package jmri.jmrix.sprog.packetgen;

import java.awt.Dimension;
import javax.swing.BoxLayout;
import jmri.jmrix.sprog.SprogMessage;
import jmri.jmrix.sprog.SprogTrafficController;

/**
 * Frame for user input of Sprog messages.
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2010
 * @version	$Revision$
 */
public class SprogPacketGenFrame extends jmri.util.JmriJFrame {

    /**
     *
     */
    private static final long serialVersionUID = 641828887730689784L;
    // member declarations
    javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
    javax.swing.JButton sendButton = new javax.swing.JButton();
    javax.swing.JTextField packetTextField = new javax.swing.JTextField(12);

    public SprogPacketGenFrame() {
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

        setTitle("Send Sprog command");
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
        SprogMessage m = new SprogMessage(packetTextField.getText());
        SprogTrafficController.instance().sendSprogMessage(m);
    }

}

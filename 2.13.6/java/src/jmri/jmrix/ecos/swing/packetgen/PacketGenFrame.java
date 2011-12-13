// PacketGenFrame.java

package jmri.jmrix.ecos.swing.packetgen;

import jmri.jmrix.ecos.*;
import java.awt.*;
import javax.swing.*;


/**
 * Frame for user input of Ecos messages
 * @author	Bob Jacobsen   Copyright (C) 2001, 2008
 * @author Dan Boudreau 	Copyright (C) 2007
 * @version $Revision$
 * @deprecated 2.11.3
 */
@Deprecated
public class PacketGenFrame extends jmri.util.JmriJFrame implements jmri.jmrix.ecos.EcosListener {

    // member declarations
    javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
    javax.swing.JButton sendButton = new javax.swing.JButton();
    javax.swing.JTextField packetTextField = new javax.swing.JTextField(20);

    public PacketGenFrame() {
        super();
    }

    EcosSystemConnectionMemo adaptermemo;

    public void initComponents(EcosSystemConnectionMemo memo) throws Exception {
        // the following code sets the frame's initial state
        adaptermemo = memo;
        jLabel1.setText("Command: ");
        jLabel1.setVisible(true);
        
        sendButton.setText("Send");
        sendButton.setVisible(true);
        sendButton.setToolTipText("Send packet");

        packetTextField.setText("");
		packetTextField.setToolTipText("Enter command");
		packetTextField.setMaximumSize(new Dimension(packetTextField
				.getMaximumSize().width, packetTextField.getPreferredSize().height));
        
        setTitle("Send ECOS command");
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        getContentPane().add(jLabel1);
        getContentPane().add(packetTextField);
        getContentPane().add(sendButton);

        sendButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    sendButtonActionPerformed(e);
                }
            });

		addHelpMenu("package.jmri.jmrix.ecos.swing.packetgen.PacketGenFrame", true);
        
        // pack for display
        pack();
    }

    public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {

        EcosMessage m = new EcosMessage(packetTextField.getText().length());
        for (int i = 0; i < packetTextField.getText().length(); i++)
            m.setElement(i, packetTextField.getText().charAt(i));

        adaptermemo.getTrafficController().sendEcosMessage(m, this);

	}

    public void  message(EcosMessage m) {}  // ignore replies
    public void  reply(EcosReply r) {} // ignore replies

}


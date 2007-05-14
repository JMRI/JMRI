// PacketGenFrame.java

package jmri.jmrix.qsi.packetgen;

import java.awt.*;
import javax.swing.*;

import jmri.jmrix.qsi.QsiTrafficController;
import jmri.jmrix.qsi.QsiMessage;
import jmri.jmrix.qsi.QsiReply;

/**
 * Frame for user input of QSI messages
 * @author			Bob Jacobsen   Copyright (C) 2007
 * @version			$Revision: 1.3 $
 */
public class PacketGenFrame extends jmri.util.JmriJFrame implements jmri.jmrix.qsi.QsiListener {

	// member declarations
	javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
	javax.swing.JButton sendButton = new javax.swing.JButton();
	javax.swing.JTextField packetTextField = new javax.swing.JTextField(12);

	public PacketGenFrame() {
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

		setTitle("Send QSI command");
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
  		QsiMessage m = new QsiMessage(packetTextField.getText().length());
  		for (int i=0; i<packetTextField.getText().length(); i++)
  			m.setElement(i, packetTextField.getText().charAt(i));

  		QsiTrafficController.instance().sendQsiMessage(m, this);
  	}

  	public void  message(QsiMessage m) {}  // ignore replies
  	public void  reply(QsiReply r) {} // ignore replies

}

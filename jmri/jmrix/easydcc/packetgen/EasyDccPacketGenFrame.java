/**
 * EasyDccPacketGenFrame.java
 *
 * Description:		Frame for user input of EasyDcc messages
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Revision: 1.2 $
 */


package jmri.jmrix.easydcc.packetgen;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import jmri.jmrix.easydcc.EasyDccTrafficController;
import jmri.jmrix.easydcc.EasyDccMessage;
import jmri.jmrix.easydcc.EasyDccReply;

public class EasyDccPacketGenFrame extends javax.swing.JFrame implements jmri.jmrix.easydcc.EasyDccListener {

	// member declarations
	javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
	javax.swing.JButton sendButton = new javax.swing.JButton();
	javax.swing.JTextField packetTextField = new javax.swing.JTextField(12);

	public EasyDccPacketGenFrame() {
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

		setTitle("Send EasyDcc command");
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

		getContentPane().add(jLabel1);
		getContentPane().add(packetTextField);
		getContentPane().add(sendButton);


		sendButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				sendButtonActionPerformed(e);
			}
		});
		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				thisWindowClosing(e);
			}
		});

		// pack for display
		pack();
	}

  	public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {
  		EasyDccMessage m = new EasyDccMessage(packetTextField.getText().length());
  		for (int i=0; i<packetTextField.getText().length(); i++)
  			m.setElement(i, packetTextField.getText().charAt(i));

  		EasyDccTrafficController.instance().sendEasyDccMessage(m, this);
  	}

  	public void  message(EasyDccMessage m) {}  // ignore replies
  	public void  reply(EasyDccReply r) {} // ignore replies

  	private boolean mShown = false;

	public void addNotify() {
		super.addNotify();

		if (mShown)
			return;

		// resize frame to account for menubar
		JMenuBar jMenuBar = getJMenuBar();
		if (jMenuBar != null) {
			int jMenuBarHeight = jMenuBar.getPreferredSize().height;
			Dimension dimension = getSize();
			dimension.height += jMenuBarHeight;
			setSize(dimension);
		}

		mShown = true;
	}

	// Close the window when the close box is clicked
	void thisWindowClosing(java.awt.event.WindowEvent e) {
		setVisible(false);
		dispose();
	}
}

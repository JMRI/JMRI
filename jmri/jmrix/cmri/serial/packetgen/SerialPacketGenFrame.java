/**
 * SerialPacketGenFrame.java
 *
 * Description:		Frame for user input of CMRI serial messages
 * @author			Bob Jacobsen   Copyright (C) 2002
 * @version			$Id: SerialPacketGenFrame.java,v 1.2 2002-03-11 04:36:24 jacobsen Exp $
 */


package jmri.jmrix.cmri.serial.packetgen;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import jmri.jmrix.cmri.serial.SerialTrafficController;
import jmri.jmrix.cmri.serial.SerialMessage;
import jmri.jmrix.cmri.serial.SerialReply;

public class SerialPacketGenFrame extends javax.swing.JFrame implements jmri.jmrix.cmri.serial.SerialListener {

	// member declarations
	javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
	javax.swing.JButton sendButton = new javax.swing.JButton();
	javax.swing.JTextField packetTextField = new javax.swing.JTextField(12);

    javax.swing.JButton pollButton = new javax.swing.JButton("Send poll");
	javax.swing.JTextField uaAddrField = new javax.swing.JTextField(5);

	public SerialPacketGenFrame() {
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

		setTitle("Send CMRI serial command");
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

		getContentPane().add(jLabel1);
		getContentPane().add(packetTextField);
		getContentPane().add(sendButton);


		sendButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				sendButtonActionPerformed(e);
			}
		});

        getContentPane().add(new JSeparator(JSeparator.HORIZONTAL));

        // add poll message buttons
        JPanel pane3 = new JPanel();
            pane3.setLayout(new FlowLayout());
            pane3.add(new JLabel("UA:"));
            pane3.add(uaAddrField);
            pane3.add(pollButton);
            uaAddrField.setText("0");
        getContentPane().add(pane3);

		pollButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				pollButtonActionPerformed(e);
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

  	public void pollButtonActionPerformed(java.awt.event.ActionEvent e) {
        SerialMessage msg = SerialMessage.getPoll(Integer.valueOf(uaAddrField.getText()).intValue());
  		SerialTrafficController.instance().sendSerialMessage(msg, this);
  	}

   	public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {
  		SerialTrafficController.instance().sendSerialMessage(createPacket(packetTextField.getText()), this);
  	}

 	SerialMessage createPacket(String s) {
		// gather bytes in result
		int b[] = parseString(s);
		if (b.length == 0) return null;  // no such thing as a zero-length message
  		SerialMessage m = new SerialMessage(b.length);
		for (int i=0; i<b.length; i++) m.setElement(i, b[i]);
  		return m;
  	}

  	int[] parseString(String s) {
		String ts = s+"  "; // ensure blanks on end to make scan easier
		int len = 0;
		// scan for length
  		for (int i= 0; i< s.length(); i++) {
  			if (ts.charAt(i) != ' ')  {
  				// need to process char for number. Is this a single digit?
  				if (ts.charAt(i+1) != ' ') {
  					// 2 char value
  					i++;
  					len++;
  				} else {
  					// 1 char value
  					len++;
  				}
  			}
  		}
  		int[] b = new int[len];
  		// scan for content
  		int saveAt = 0;
  		for (int i= 0; i< s.length(); i++) {
  			if (ts.charAt(i) != ' ')  {
  				// need to process char for number. Is this a single digit?
  				if (ts.charAt(i+1) != ' ') {
  					// 2 char value
 					String v = new String(""+ts.charAt(i))+ts.charAt(i+1);
					b[saveAt] = Integer.valueOf(v,16).intValue();
					i++;
					saveAt++;
   				} else {
  					// 1 char value
					String v = new String(""+ts.charAt(i));
					b[saveAt] = Integer.valueOf(v,16).intValue();
					saveAt++;
  				}
  			}
  		}
  		return b;
  	}

  	public void  message(SerialMessage m) {}  // ignore replies
  	public void  reply(SerialReply r) {} // ignore replies

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

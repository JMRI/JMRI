// ZimoPacketGenFrame.java

package jmri.jmrix.zimo.packetgen;

import javax.swing.*;

import jmri.jmrix.zimo.Mx1Message;
import jmri.jmrix.zimo.Mx1TrafficController;

/**
 * Frame for user input of MX-1 messages.
 *
 * @author		Bob Jacobsen   Copyright (C) 2001,2002
 * @version		$Revision$
 *
 * Adapted by Sip Bosch for use with Zimo MX-1
 *
 */
public class ZimoPacketGenFrame extends jmri.util.JmriJFrame {

	// member declarations
	javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
	javax.swing.JButton sendButton = new javax.swing.JButton();
	javax.swing.JTextField packetTextField = new javax.swing.JTextField(12);

	public ZimoPacketGenFrame() {
	    super();
	}

	public void initComponents() throws Exception {
		// the following code sets the frame's initial state

		jLabel1.setText("Packet:");
		jLabel1.setVisible(true);

		sendButton.setText("Send");
		sendButton.setVisible(true);
		sendButton.setToolTipText("Send packet");

		packetTextField.setToolTipText("Enter packet as hex pairs, e.g. 82 7D. Note: MX-1 transcieves asci values.");

		setTitle("Send MX-1 Packet");
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
  		tc.sendMx1Message(createPacket(packetTextField.getText()), null);
  	}

  	Mx1Message createPacket(String s) {
		// gather bytes in result
		int b[] = parseString(s);
		if (b.length == 0) return null;  // no such thing as a zero-length message
  		Mx1Message m = new Mx1Message(b.length+1);
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
 					String v = ""+ts.charAt(i)+ts.charAt(i+1);
					b[saveAt] = Integer.valueOf(v,16).intValue();
					i++;
					saveAt++;
   				} else {
  					// 1 char value
					String v = ""+ts.charAt(i);
					b[saveAt] = Integer.valueOf(v,16).intValue();
					saveAt++;
  				}
  			}
  		}
  		return b;
  	}

	// connect to the TrafficController
	public void connect(Mx1TrafficController t) {
		tc = t;
	}

	// private data
	private Mx1TrafficController tc = null;

}

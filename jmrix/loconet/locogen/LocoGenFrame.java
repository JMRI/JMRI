/** 
 * LocoGenFrame.java
 *
 * Description:		Frame for user input of LocoNet messages
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			
 */


package jmri.jmrix.loconet.locogen;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import jmri.jmrix.loconet.LocoNetMessage;
import jmri.jmrix.loconet.LnConstants;
import jmri.jmrix.loconet.LnTrafficController;

public class LocoGenFrame extends javax.swing.JFrame {

	// member declarations
	javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
	javax.swing.JButton sendButton = new javax.swing.JButton();
	javax.swing.JTextField packetTextField = new javax.swing.JTextField();

	public LocoGenFrame() {
	}

	public void initComponents() throws Exception {
		// the following code sets the frame's initial state

		jLabel1.setText("Packet:");
		jLabel1.setVisible(true);

		sendButton.setText("Send");
		sendButton.setVisible(true);
		sendButton.setToolTipText("Send packet");
		
		packetTextField.setText("                   ");
		packetTextField.setToolTipText("Enter packet as hex pairs, e.g. 82 7D");
		
		setTitle("Send LocoNet Packet");
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
		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				thisWindowClosing(e);
			}
		});

		// pack to cause display
		pack();
	}
  
  	public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {
  		tc.sendLocoNetMessage(createPacket(packetTextField.getText()));
  	}
  	
  	LocoNetMessage createPacket(String s) {
		// gather bytes in result
		int b[] = parseString(s);
		if (b.length == 0) return null;  // no such thing as a zero-length message
  		LocoNetMessage m = new LocoNetMessage(b.length);
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
	// disconnect from LnTrafficController
		tc = null;
	}
	
	// connect to the LnTrafficController
	public void connect(LnTrafficController t) {
		tc = t;
	}	
	
	
	// private data
	private LnTrafficController tc = null;
	
}

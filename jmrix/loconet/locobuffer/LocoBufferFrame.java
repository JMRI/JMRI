/** 
 * LocoBufferFrame.java
 *
 * Description:		Frame to control and connect LocoNet via LocoBuffer interface and comm port
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			
 */

package jmri.jmrix.loconet.locobuffer;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import jmri.jmrix.loconet.LnTrafficController;

public class LocoBufferFrame extends javax.swing.JFrame {

	javax.swing.JButton getNamesButton = new javax.swing.JButton();
	javax.swing.JList portList = new javax.swing.JList();
	javax.swing.JButton openPortButton = new javax.swing.JButton();

	public LocoBufferFrame() {
	}

	public void initComponents() throws Exception {
		// the following code sets the frame's initial state

		getNamesButton.setText("Get port names");
		getNamesButton.setToolTipText("Updates the list of available port names");
		getNamesButton.setVisible(true);

		portList.setVisible(true);
		portList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
		portList.setToolTipText("Select the port to use");
		portList.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		portList.setListData(adapter.getPortNames());
		
		openPortButton.setText("Open port");
		openPortButton.setToolTipText("Configure program to use selected port");
		openPortButton.setVisible(true);

		setLocation(new java.awt.Point(5, 40));
		setTitle("LocoBuffer connection");
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		getContentPane().add(new JLabel("set LocoBuffer to 19200 baud,"));
		getContentPane().add(new JLabel("     local echo"));
		getContentPane().add(getNamesButton);
		getContentPane().add(portList);
		getContentPane().add(openPortButton);
		
		pack();

		getNamesButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				getNamesButtonActionPerformed(e);
			}
		});
		openPortButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				openPortButtonActionPerformed(e);
			}
		});
		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				thisWindowClosing(e);
			}
		});
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
		System.exit(0);
	}
		
	public void getNamesButtonActionPerformed(java.awt.event.ActionEvent e) {
		portList.setListData(adapter.getPortNames());

	}
	
	public void openPortButtonActionPerformed(java.awt.event.ActionEvent e) {
		if ((String) portList.getSelectedValue() != null) {
			// connect to the port
			String errCode = adapter.openPort((String) portList.getSelectedValue(),"LocoBufferFrame");
			
			if (errCode == null)	{
				adapter.configure();						
				// check for port in OK state
				if (!adapter.okToSend()) {
					log.info("LocoBuffer port not ready to send");
					JOptionPane.showMessageDialog(null, 
				   		"The LocoBuffer is unable to accept data.\n"
				   		+"Make sure its power is on, it is connected\n"
				   		+"to a working LocoNet, and the command station is on.\n"
				   		+"The LocoNet LED on the LocoBuffer should be off.\n"
				   		+"Reset the LocoBuffer by cycling its power.\n"
				   		+"Then restart this program.", 
				   		"LocoBuffer not ready", JOptionPane.ERROR_MESSAGE);
				}
				// hide this frame, since we're done
				hide();
			} else {
				JOptionPane.showMessageDialog(this,errCode);
			}
		} else {
			// not selected
			JOptionPane.showMessageDialog(this, "Please select a port name first");
		}
	}
	
// Data members
	private LocoBufferAdapter adapter = new LocoBufferAdapter();

   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LocoBufferFrame.class.getName());

}

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
			adapter.openPort((String) portList.getSelectedValue(),"LocoBufferFrame");
				
			// connect to the traffic controller
			LnTrafficController.instance().connectPort(adapter);
		
			// If a jmri.Programmer instance doesn't exist, create a 
			// loconet.SlotManager to do that
			if (jmri.InstanceManager.programmerInstance() == null) 
				jmri.jmrix.loconet.SlotManager.instance();
				
			// If a jmri.PowerManager instance doesn't exist, create a 
			// loconet.LnPowerManager to do that
			if (jmri.InstanceManager.powerManagerInstance() == null) 
				jmri.InstanceManager.setPowerManager(new jmri.jmrix.loconet.LnPowerManager());

			// start operation
			// sourceThread = new Thread(p);
			// sourceThread.start();
			sinkThread = new Thread(LnTrafficController.instance());
			sinkThread.start();
			
			// hide this frame, since we're done
			hide();
		} else {
			// not selected
			JOptionPane.showMessageDialog(this, "Please select a port name first");
		}
	}
	
// Data members
	private LocoBufferAdapter adapter = new LocoBufferAdapter();
	// private Thread sourceThread;
	private Thread sinkThread;
}

/** 
 * MS100Frame.java
 *
 * Description:		Frame to control and connect LocoNet via MS100 interface and comm port
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			
 */

package ms100;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import loconet.LnTrafficController;
import ErrLoggerJ.ErrLog;


public class MS100Frame extends javax.swing.JFrame {

// IMPORTANT: Source code between BEGIN/END comment pair will be regenerated
// every time the form is saved. All manual changes will be overwritten.
// BEGIN GENERATED CODE
	// member declarations
	javax.swing.JButton getNamesButton = new javax.swing.JButton();
	javax.swing.JList portList = new javax.swing.JList();
	javax.swing.JButton openPortButton = new javax.swing.JButton();
// END GENERATED CODE

	public MS100Frame() {
	}

	public void initComponents() throws Exception {
// IMPORTANT: Source code between BEGIN/END comment pair will be regenerated
// every time the form is saved. All manual changes will be overwritten.
// BEGIN GENERATED CODE
		// the following code sets the frame's initial state

		getNamesButton.setText("Get port names");
		getNamesButton.setLocation(new java.awt.Point(10, 10));
		getNamesButton.setVisible(true);
		getNamesButton.setSize(new java.awt.Dimension(180, 40));

		portList.setLocation(new java.awt.Point(10, 60));
		portList.setVisible(true);
		portList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
		portList.setToolTipText("Select the primary serial port");
		portList.setSize(new java.awt.Dimension(180, 80));

		openPortButton.setText("Open LocoNet port");
		openPortButton.setLocation(new java.awt.Point(10, 150));
		openPortButton.setVisible(true);
		openPortButton.setToolTipText("Use this open button if your MS100 is connected to one port");
		openPortButton.setSize(new java.awt.Dimension(180, 40));

		setLocation(new java.awt.Point(5, 40));
		setTitle("LocoNet Monitor startup");
		getContentPane().setLayout(null);
		setSize(new java.awt.Dimension(442, 257));
		getContentPane().add(getNamesButton);
		getContentPane().add(portList);
		getContentPane().add(openPortButton);


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

// END GENERATED CODE
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
		adapter.openPort((String) portList.getSelectedValue(),"MS100SerialFrame");
				
		// connect to the traffic controller
		LnTrafficController.instance().connectPort(adapter);
				
		// start operation
		// sourceThread = new Thread(p);
		// sourceThread.start();
		sinkThread = new Thread(LnTrafficController.instance());
		sinkThread.start();
		
		// hide this frame, since we're done
		hide();
	}
	
// Data members
	private MS100Adapter adapter = new MS100Adapter();
	// private Thread sourceThread;
	private Thread sinkThread;
}

/** 
 * HexFileFrame.java
 *
 * Title:			LocoMon application
 * Description:		Frame to inject LocoNet messages from a hex file
 * @author			Bob Jacobsen  Copyright 2001
 * @version			
 */

// This is a sample frame that drives a test App.  It controls reading from
// a .hex file, feeding the information to a LocoMonFrame (monitor) and 
// connecting to a LocoGenFrame (for sending a few commands).

package LocoMonAppl;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import LocoMon.LnHexFilePort;
import LocoMon.LocoMonFrame;
import LocoMon.LocoMonGen;
import LocoMon.MS100Frame;
import LocoNet.LnTrafficController;
import ErrLoggerJ.ErrLog;

public class HexFileFrame extends javax.swing.JFrame {

// IMPORTANT: Source code between BEGIN/END comment pair will be regenerated
// every time the form is saved. All manual changes will be overwritten.
// BEGIN GENERATED CODE
	// member declarations
	javax.swing.JTextField filenameTextField = new javax.swing.JTextField();
	javax.swing.JButton openHexFileButton = new javax.swing.JButton();
	javax.swing.JButton filePauseButton = new javax.swing.JButton();
	javax.swing.JButton jButton1 = new javax.swing.JButton();
	javax.swing.JTextField delayField = new javax.swing.JTextField();
	javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
// END GENERATED CODE

	public HexFileFrame() {
	}

	public void initComponents() throws Exception {
// IMPORTANT: Source code between BEGIN/END comment pair will be regenerated
// every time the form is saved. All manual changes will be overwritten.
// BEGIN GENERATED CODE
		// the following code sets the frame's initial state

		filenameTextField.setText("lnpacket.hex");
		filenameTextField.setLocation(new java.awt.Point(140, 20));
		filenameTextField.setVisible(true);
		filenameTextField.setToolTipText("Enter name for hex file");
		filenameTextField.setSize(new java.awt.Dimension(110, 40));

		openHexFileButton.setText("read from file");
		openHexFileButton.setLocation(new java.awt.Point(10, 20));
		openHexFileButton.setVisible(true);
		openHexFileButton.setToolTipText("run from hex file");
		openHexFileButton.setSize(new java.awt.Dimension(120, 40));

		filePauseButton.setText("pause");
		filePauseButton.setLocation(new java.awt.Point(80, 70));
		filePauseButton.setVisible(true);
		filePauseButton.setToolTipText("pauses the trace at the source");
		filePauseButton.setSize(new java.awt.Dimension(70, 20));

		jButton1.setText("continue");
		jButton1.setLocation(new java.awt.Point(160, 70));
		jButton1.setVisible(true);
		jButton1.setToolTipText("continues the trace at the source");
		jButton1.setSize(new java.awt.Dimension(70, 20));

		delayField.setText("200");
		delayField.setLocation(new java.awt.Point(140, 110));
		delayField.setVisible(true);
		delayField.setToolTipText("delay (in milliseconds) between commands");
		delayField.setSize(new java.awt.Dimension(70, 30));

		jLabel1.setText("delay:");
		jLabel1.setLocation(new java.awt.Point(90, 110));
		jLabel1.setVisible(true);
		jLabel1.setSize(new java.awt.Dimension(40, 30));

		setLocation(new java.awt.Point(5, 40));
		setTitle("LocoMon controls");
		getContentPane().setLayout(null);
		setSize(new java.awt.Dimension(387, 229));
		getContentPane().add(filenameTextField);
		getContentPane().add(openHexFileButton);
		getContentPane().add(filePauseButton);
		getContentPane().add(jButton1);
		getContentPane().add(delayField);
		getContentPane().add(jLabel1);


		openHexFileButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				openHexFileButtonActionPerformed(e);
			}
		});
		filePauseButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				filePauseButtonActionPerformed(e);
			}
		});
		jButton1.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				jButton1ActionPerformed(e);
			}
		});
		delayField.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				delayFieldActionPerformed(e);
			}
		});
		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				thisWindowClosing(e);
			}
		});

// END GENERATED CODE
	// this is still initComponents; add the LocoMonFrame frame
		lmFrame = new LocoMonFrame();
		try {
			lmFrame.initComponents();
			}
		catch (Exception ex) {
			ErrLog.msg(ErrLog.error, "LocoMonAppl.HexFileFrame starting LocoMonFrame", "", "Exception: "+ex.toString());
			}
		lmFrame.show();
	// this is still initComponents; add the LocoMonGen frame
		lmGen = new LocoMonGen();
		try {
			lmGen.initComponents();
			}
		catch (Exception ex) {
			ErrLog.msg(ErrLog.error, "LocoMonAppl.HexFileFrame starting LocoMonGen", "", "Exception: "+ex.toString());
			}
		lmGen.show();
	// this is still initComponents; add the MS100Frame frame
		ms100 = new MS100Frame();
		try {
			ms100.initComponents();
			}
		catch (Exception ex) {
			ErrLog.msg(ErrLog.error, "LocoMonAppl.HexFileFrame starting MS100Frame:", "", "Exception: "+ex.toString());
			}
		lmGen.show();
	// connect and configure
		// create a new Hex file handler, set its delay
		p = new LnHexFilePort();
		p.setDelay(Integer.valueOf(delayField.getText()).intValue());
		
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
	
	public void openHexFileButtonActionPerformed(java.awt.event.ActionEvent e) {
		// call load to process the file
		p.load(filenameTextField.getText());

		//create a LnTrafficController object and a LocoMonFrame,
		LnTrafficController tc = new LnTrafficController();

		// connect them all together
		tc.notifyLocoNetEvents(~0, lmFrame);
		tc.connectToPort(p);
		lmGen.connect(tc);
		
		// start operation
		sourceThread = new Thread(p);
		sourceThread.start();
		sinkThread = new Thread(tc);
		sinkThread.start();
		
	}
	
	public void filePauseButtonActionPerformed(java.awt.event.ActionEvent e) {
		sourceThread.suspend();
		// sinkThread.suspend(); // allow sink to catch up
	}
	
	public void jButton1ActionPerformed(java.awt.event.ActionEvent e) {  // resume button
	sourceThread.resume();
		// sinkThread.resume();
	}
	
	public void delayFieldActionPerformed(java.awt.event.ActionEvent e) {
		// if the hex file has been started, change its delay
		if (p!=null) p.setDelay(Integer.valueOf(delayField.getText()).intValue());
	}
	
	
	
	private Thread sourceThread;
	private Thread sinkThread;
	private LocoMonFrame lmFrame = null;
	private LnHexFilePort p = null;
	private LocoMonGen lmGen = null;
	private MS100Frame ms100 = null;
	
}

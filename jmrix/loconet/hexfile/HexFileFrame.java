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

package jmri.jmrix.loconet.hexfile;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import jmri.jmrix.loconet.LnTrafficController;

public class HexFileFrame extends javax.swing.JFrame {

	// member declarations
	javax.swing.JTextField filenameTextField = new javax.swing.JTextField();
	javax.swing.JButton openHexFileButton = new javax.swing.JButton();
	javax.swing.JButton filePauseButton = new javax.swing.JButton();
	javax.swing.JButton jButton1 = new javax.swing.JButton();
	javax.swing.JTextField delayField = new javax.swing.JTextField();
	javax.swing.JLabel jLabel1 = new javax.swing.JLabel();

	public HexFileFrame() {
	}

	public void initComponents() throws Exception {
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
		setTitle("Hexfile LocoNet simulator");
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

	boolean connected = false;
	
	// Close the window when the close box is clicked
	void thisWindowClosing(java.awt.event.WindowEvent e) {
		setVisible(false);
		dispose();
		// disconnect from LnTrafficManager if connected
		if (connected) LnTrafficController.instance().disconnectPort(p);
		connected = false;
	}
	
	public void openHexFileButtonActionPerformed(java.awt.event.ActionEvent e) {
		// call load to process the file
		p.load(filenameTextField.getText());


		// connect to the traffic controller
		LnTrafficController.instance().connectPort(p);
		connected = true;
		
		// If a jmri.Programmer instance doesn't exist, create a 
		// loconet.SlotManager to do that
		if (jmri.InstanceManager.programmerInstance() == null) 
			jmri.InstanceManager.setProgrammer(new jmri.progdebugger.ProgDebugger());

		// If a jmri.PowerManager instance doesn't exist, create a 
		// loconet.LnPowerManager to do that
		if (jmri.InstanceManager.powerManagerInstance() == null) 
			jmri.InstanceManager.setPowerManager(new jmri.jmrix.loconet.LnPowerManager());

		// start operation
		sourceThread = new Thread(p);
		sourceThread.start();
		sinkThread = new Thread(LnTrafficController.instance());
		sinkThread.start();
		
		// reach here while file runs.  Need to return so GUI still acts, 
		// but that normally lets the button go back to default.
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
	private LnHexFilePort p = null;

   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(HexFileFrame.class.getName());
	
}

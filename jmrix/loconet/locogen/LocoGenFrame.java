/** 
 * LocoGenFrame.java
 *
 * Description:		Frame for user input of LocoNet messages
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			
 */


package locogen;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import loconet.LocoNetMessage;
import loconet.LnConstants;
import loconet.LnTrafficController;

public class LocoGenFrame extends javax.swing.JFrame {

// IMPORTANT: Source code between BEGIN/END comment pair will be regenerated
// every time the form is saved. All manual changes will be overwritten.
// BEGIN GENERATED CODE
	// member declarations
	javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
	javax.swing.JButton gponButton = new javax.swing.JButton();
	javax.swing.JButton gpoffButton = new javax.swing.JButton();
	javax.swing.JButton swreqButton = new javax.swing.JButton();
	javax.swing.JTextField switchAddrTextField = new javax.swing.JTextField();
	javax.swing.JCheckBox thrownCheckBox = new javax.swing.JCheckBox();
	javax.swing.JCheckBox switchOnCheckBox = new javax.swing.JCheckBox();
// END GENERATED CODE

	public LocoGenFrame() {
	}

	public void initComponents() throws Exception {
// IMPORTANT: Source code between BEGIN/END comment pair will be regenerated
// every time the form is saved. All manual changes will be overwritten.
// BEGIN GENERATED CODE
		// the following code sets the frame's initial state

		jLabel1.setText("Commands:");
		jLabel1.setLocation(new java.awt.Point(10, 10));
		jLabel1.setVisible(true);
		jLabel1.setSize(new java.awt.Dimension(80, 30));

		gponButton.setText("On");
		gponButton.setLocation(new java.awt.Point(100, 20));
		gponButton.setVisible(true);
		gponButton.setToolTipText("Send GPON");
		gponButton.setSize(new java.awt.Dimension(50, 30));

		gpoffButton.setText("Off");
		gpoffButton.setLocation(new java.awt.Point(160, 20));
		gpoffButton.setVisible(true);
		gpoffButton.setToolTipText("Send GPOFF");
		gpoffButton.setSize(new java.awt.Dimension(60, 30));

		swreqButton.setText("Switch Request");
		swreqButton.setLocation(new java.awt.Point(100, 70));
		swreqButton.setVisible(true);
		swreqButton.setToolTipText("Send SW REQ");
		swreqButton.setSize(new java.awt.Dimension(130, 30));

		switchAddrTextField.setText("23");
		switchAddrTextField.setLocation(new java.awt.Point(270, 70));
		switchAddrTextField.setVisible(true);
		switchAddrTextField.setToolTipText("Turnout number to throw (LocoNet number, one less than number on throttle)");
		switchAddrTextField.setSize(new java.awt.Dimension(50, 30));

		thrownCheckBox.setText("Thrown");
		thrownCheckBox.setLocation(new java.awt.Point(330, 70));
		thrownCheckBox.setVisible(true);
		thrownCheckBox.setToolTipText("Checked for Thrown, unchecked for Closed");
		thrownCheckBox.setSize(new java.awt.Dimension(80, 20));

		switchOnCheckBox.setText("On");
		switchOnCheckBox.setLocation(new java.awt.Point(420, 70));
		switchOnCheckBox.setVisible(true);
		switchOnCheckBox.setToolTipText("Checked for On, unchecked for Off");
		switchOnCheckBox.setSize(new java.awt.Dimension(60, 20));
		switchOnCheckBox.setSelected(true);

		setLocation(new java.awt.Point(400, 400));
		setTitle("locogen.LocoGenFrame");
		getContentPane().setLayout(null);
		setSize(new java.awt.Dimension(566, 250));
		getContentPane().add(jLabel1);
		getContentPane().add(gponButton);
		getContentPane().add(gpoffButton);
		getContentPane().add(swreqButton);
		getContentPane().add(switchAddrTextField);
		getContentPane().add(thrownCheckBox);
		getContentPane().add(switchOnCheckBox);


		gponButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				gponButtonActionPerformed(e);
			}
		});
		gpoffButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				gpoffButtonActionPerformed(e);
			}
		});
		swreqButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				swreqButtonActionPerformed(e);
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
	// disconnect from LnTrafficController
		tc = null;
	}
	
	// connect to the LnTrafficController
	public void connect(LnTrafficController t) {
		tc = t;
	}
		
	public void gponButtonActionPerformed(java.awt.event.ActionEvent e) {
		// send GPON
		LocoNetMessage l = new LocoNetMessage(2);
		l.setOpCode(LnConstants.OPC_GPON);
		tc.sendLocoNetMessage(l);
	}
	
	public void gpoffButtonActionPerformed(java.awt.event.ActionEvent e) {
		// send GPOFF
		LocoNetMessage l = new LocoNetMessage(2);
		l.setOpCode(LnConstants.OPC_GPOFF);
		tc.sendLocoNetMessage(l);
	}
	
	public void swreqButtonActionPerformed(java.awt.event.ActionEvent e) {
		// send SWREQ
		LocoNetMessage l = new LocoNetMessage(4);
		l.setOpCode(LnConstants.OPC_SW_REQ);
		
		// load address from switchAddrTextField
		int adr = Integer.valueOf(switchAddrTextField.getText()).intValue();
		int hiadr = adr/128;
		int loadr = adr-hiadr*128;
		
		// load T/C from thrownCheckBox - default is already thrown, check closed
		if ( !thrownCheckBox.isSelected() )   hiadr |= 0x20;
			
		// load On/Off from switchOnCheckBox
		if ( switchOnCheckBox.isSelected() )   hiadr |= 0x10;
		
		// store and send
		l.setElement(1,loadr);
		l.setElement(2,hiadr);
		tc.sendLocoNetMessage(l);
	}
	
	
	
	// private data
	private LnTrafficController tc = null;
	
}

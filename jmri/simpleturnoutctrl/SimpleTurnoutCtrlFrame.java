/** 
 * SimpleTurnoutCtrlFrame.java
 *
 * Description:		Frame controlling a single turnout
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			
 */

package jmri.simpleturnoutctrl;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Date;
import java.text.DateFormat;
import java.io.File;

import jmri.jmrix.loconet.LocoNetListener;
import jmri.jmrix.loconet.LnTrafficController;
import jmri.jmrix.loconet.LocoNetMessage;
import jmri.jmrix.loconet.LnConstants;
import jmri.jmrix.loconet.LnTurnout;

import java.io.PrintStream;
import java.io.FileOutputStream;

public class SimpleTurnoutCtrlFrame extends javax.swing.JFrame {

	// GUI member declarations
	javax.swing.JLabel textAdrLabel = new javax.swing.JLabel();
	javax.swing.JTextField adrTextField = new javax.swing.JTextField();
		
	javax.swing.JButton throwButton = new javax.swing.JButton();
	javax.swing.JButton closeButton = new javax.swing.JButton();
	
	javax.swing.JLabel textStateLabel = new javax.swing.JLabel();
	javax.swing.JLabel nowStateLabel = new javax.swing.JLabel();

	public SimpleTurnoutCtrlFrame() {

		// configure items for GUI
		textAdrLabel.setText("turnout:");
		textAdrLabel.setVisible(true);

		adrTextField.setText("");
		adrTextField.setVisible(true);
		adrTextField.setToolTipText("turnout number being controlled");

		throwButton.setText("Thrown");
		throwButton.setVisible(true);
		throwButton.setToolTipText("Press to set turnout 'thrown'");
		throwButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				throwButtonActionPerformed(e);
			}
		});

		closeButton.setText("Closed");
		closeButton.setVisible(true);
		closeButton.setToolTipText("Press to set turnout 'closed'");
		closeButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				closeButtonActionPerformed(e);
			}
		});

		textStateLabel.setText("current state:");
		textStateLabel.setVisible(true);

		nowStateLabel.setText("<unknown>");
		nowStateLabel.setVisible(true);

		// general GUI config
		setTitle("Turnout Control");
		getContentPane().setLayout(new GridLayout(4,2));

		// install items in GUI
		getContentPane().add(textAdrLabel);
		getContentPane().add(adrTextField);

		getContentPane().add(textStateLabel);
		getContentPane().add(nowStateLabel);
		
		getContentPane().add(throwButton);
		getContentPane().add(closeButton);

		pack();
		
		// debugging LnTurnout
		t  = new LnTurnout(23);
		// attach a listener
		t.addPropertyChangeListener( new java.beans.PropertyChangeListener() {
			public void propertyChange(java.beans.PropertyChangeEvent e) { 
				System.out.println("Turnout property change: "+e.getPropertyName()
					+" "+e.getOldValue()+" "+e.getNewValue()
						);}
			} );
	}
  
  	LnTurnout t;
  	
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
	

public void closeButtonActionPerformed(java.awt.event.ActionEvent e) {
	// create a new LnTurnout item and ask it to handle this

	// load address from switchAddrTextField
	int adr;
	try {
		adr = Integer.valueOf(adrTextField.getText()).intValue();
		LnTurnout tmp = new LnTurnout(adr);
		tmp.setCommandedState(LnTurnout.CLOSED);
		}
	catch (Exception ex)
		{
			log.error("closeButtonActionPerformed, exception: "+ex.toString());
			return;
		}
	return;
	}

public void throwButtonActionPerformed(java.awt.event.ActionEvent e) {
	// create a new LnTurnout item and ask it to handle this

	// load address from switchAddrTextField
	int adr;
	try {
		adr = Integer.valueOf(adrTextField.getText()).intValue();
		LnTurnout tmp = new LnTurnout(adr);
		tmp.setCommandedState(LnTurnout.THROWN);
		}
	catch (Exception ex)
		{
			log.error("throwButtonActionPerformed, exception: "+ex.toString());
			return;
		}
	return;
	}

private boolean myAddress(int a1, int a2) { 
	try {
		return (((a2 & 0x0f) * 128) + (a1 & 0x7f) + 1) 
			== Integer.valueOf(adrTextField.getText()).intValue(); 
		}
	catch (java.lang.NumberFormatException e) 
		{
			return false;
		}
	}

static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SimpleTurnoutCtrlFrame.class.getName());

String newState = "";
}

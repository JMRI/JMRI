/** 
 * LocoEchoFrame.java
 *
 * Description:		Frame emulating the LocoEcho hardware device for a single channel
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			
 */

package locoecho;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Date;
import java.text.DateFormat;
import java.io.File;

import loconet.LocoNetListener;
import loconet.LnTrafficController;
import loconet.LocoNetMessage;
import loconet.LnConstants;
import java.io.PrintStream;
import java.io.FileOutputStream;
import ErrLoggerJ.ErrLog;

public class LocoEchoFrame extends javax.swing.JFrame implements loconet.LocoNetListener {

	// GUI member declarations
	javax.swing.JLabel textAdrLabel = new javax.swing.JLabel();
	javax.swing.JTextField adrTextField = new javax.swing.JTextField();
	
	javax.swing.JCheckBox thrownCheckBox = new javax.swing.JCheckBox();
	javax.swing.JCheckBox closedCheckBox = new javax.swing.JCheckBox();
	
	javax.swing.JButton throwButton = new javax.swing.JButton();
	javax.swing.JButton closeButton = new javax.swing.JButton();
	
	javax.swing.JLabel textStateLabel = new javax.swing.JLabel();
	javax.swing.JLabel nowStateLabel = new javax.swing.JLabel();

	public LocoEchoFrame() {

		// configure items for GUI
		textAdrLabel.setText("turnout:");
		textAdrLabel.setVisible(true);

		adrTextField.setText("");
		adrTextField.setVisible(true);
		adrTextField.setToolTipText("turnout number being controlled");

		thrownCheckBox.setText("handle thrown");
		thrownCheckBox.setVisible(true);
		thrownCheckBox.setSelected(true);
		thrownCheckBox.setToolTipText("if checked, this echo device handles the 'thrown' case");

		closedCheckBox.setText("handle closed");
		closedCheckBox.setVisible(true);
		closedCheckBox.setSelected(true);
		closedCheckBox.setToolTipText("if checked, this echo device handles the 'closed' case");

		throwButton.setText("Throw input");
		throwButton.setVisible(true);
		throwButton.setToolTipText("Press to emulate 'throw' input going active");
		throwButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				throwButtonActionPerformed(e);
			}
		});

		closeButton.setText("Close input");
		closeButton.setVisible(true);
		closeButton.setToolTipText("Press to emulate 'close' input going active");
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
		setTitle("LocoEcho testing");
		getContentPane().setLayout(new GridLayout(4,2));

		// install items in GUI
		getContentPane().add(textAdrLabel);
		getContentPane().add(adrTextField);

			// following are suppressed because they don't do anything here
			//getContentPane().add(thrownCheckBox);
			//getContentPane().add(closedCheckBox);

		getContentPane().add(textStateLabel);
		getContentPane().add(nowStateLabel);
		
		getContentPane().add(throwButton);
		getContentPane().add(closeButton);

		pack();
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
	// and disconnect from the LnTrafficController
	LnTrafficController.instance().removeLocoNetListener(~0,this);
	}
	
	public synchronized void message(LocoNetMessage l) {  
		// decode the packet
		boolean changedState = false;
		
		switch (l.getOpCode()) {

        case LnConstants.OPC_SW_REQ: {               /* page 9 of Loconet PE */
            int sw1 = l.getElement(1);
            int sw2 = l.getElement(2);
			if (myAddress(sw1, sw2)) {
            	changedState = true;
				if ((sw2 & LnConstants.OPC_SW_REQ_DIR)!=0)
					newState = "closed";
				else
					newState = "thrown";
				}
			break;
			}
			
        case LnConstants.OPC_SW_REP: {               /* page 9 of Loconet PE */
            int sw1 = l.getElement(1);
            int sw2 = l.getElement(2);
			if (myAddress(sw1, sw2)) {
				// see if its a turnout state report
    	        if ((sw2 & LnConstants.OPC_SW_REP_INPUTS)==0) {
    	        	changedState = true;
    	        	// sort out states
    	        	switch (sw2 & 
    	        			(LnConstants.OPC_SW_REP_CLOSED|LnConstants.OPC_SW_REP_THROWN)) {
    	        			
    	        		case LnConstants.OPC_SW_REP_CLOSED:	
    	        			newState = "closed";
    	        			break;
    	        		case LnConstants.OPC_SW_REP_THROWN:	
    	        			newState = "thrown";
    	        			break;
    	        		case LnConstants.OPC_SW_REP_CLOSED|LnConstants.OPC_SW_REP_THROWN:	
    	        			newState = "<both>";
    	        			break;
    	        		default:	
    	        			newState = "<neither>";
    	        			break;
						}
    	        	}
				}
			break;
			}
			
			
		} // end of switch
		
		// display it in the Swing thread if changed
		if (changedState) {
			Runnable r = new Runnable() {
				public void run() { nowStateLabel.setText(newState); }
				};
			javax.swing.SwingUtilities.invokeLater(r);
		}
	}

public void closeButtonActionPerformed(java.awt.event.ActionEvent e) {
	// send SWREQ for close
	LocoNetMessage l = new LocoNetMessage(4);
	l.setOpCode(LnConstants.OPC_SW_REQ);
		
	// load address from switchAddrTextField
	int adr;
	try {
		adr = Integer.valueOf(adrTextField.getText()).intValue() -1;
		}
	catch (Exception ex)
		{
			ErrLog.msg(ErrLog.error, "LocoEchoFrame", "parsing turnout address", "Exception: "+ex.toString());
			return;
		}
	int hiadr = adr/128;
	int loadr = adr-hiadr*128;
		
	// set closed
	hiadr |= 0x20;
			
	// load On/Off with on
	hiadr |= 0x10;
		
	// store and send
	l.setElement(1,loadr);
	l.setElement(2,hiadr);
	LnTrafficController.instance().sendLocoNetMessage(l);
	}

public void throwButtonActionPerformed(java.awt.event.ActionEvent e) {
	// send SWREQ for throw
	LocoNetMessage l = new LocoNetMessage(4);
	l.setOpCode(LnConstants.OPC_SW_REQ);
		
	// load address from switchAddrTextField
	int adr;
	try {
		adr = Integer.valueOf(adrTextField.getText()).intValue() -1;
		}
	catch (Exception ex)
		{
			ErrLog.msg(ErrLog.error, "LocoEchoFrame", "parsing turnout address", "Exception: "+ex.toString());
			return;
		}
	int hiadr = adr/128;
	int loadr = adr-hiadr*128;
		
	// set throw
	hiadr &= 0x20;
			
	// load On/Off with on
	hiadr |= 0x10;
		
	// store and send
	l.setElement(1,loadr);
	l.setElement(2,hiadr);
	LnTrafficController.instance().sendLocoNetMessage(l);
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

String newState = "";
}

/** 
 * SimpleProgFrame.java
 *
 * Description:		Frame providing a command station programmer
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			
 */

package jmri.simpleprog;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import jmri.Programmer;
import jmri.ProgListener;

import ErrLoggerJ.ErrLog;

public class SimpleProgFrame extends javax.swing.JFrame implements jmri.ProgListener {

	// GUI member declarations
	javax.swing.JToggleButton readButton 	= new javax.swing.JToggleButton();
	javax.swing.JToggleButton writeButton 	= new javax.swing.JToggleButton();
	javax.swing.JTextField  addrField       = new javax.swing.JTextField();
	javax.swing.JTextField  valField        = new javax.swing.JTextField();
	
	javax.swing.ButtonGroup modeGroup 		= new javax.swing.ButtonGroup();
	javax.swing.JRadioButton pagedButton    = new javax.swing.JRadioButton();
	javax.swing.JRadioButton directBitButton   = new javax.swing.JRadioButton();
	javax.swing.JRadioButton directByteButton   = new javax.swing.JRadioButton();
	javax.swing.JRadioButton registerButton = new javax.swing.JRadioButton();
	
	javax.swing.ButtonGroup radixGroup 		= new javax.swing.ButtonGroup();
	javax.swing.JRadioButton hexButton    	= new javax.swing.JRadioButton();
	javax.swing.JRadioButton decButton   	= new javax.swing.JRadioButton();
	
	javax.swing.JLabel       resultsField   = new javax.swing.JLabel();
	
	public SimpleProgFrame() {

		// configure items for GUI		
		readButton.setText("Read CV");
		readButton.setToolTipText("Read the value from the selected CV");

		writeButton.setText("Write CV");
		writeButton.setToolTipText("Write the value to the selected CV");
		
		pagedButton.setText("Paged Mode");
		pagedButton.setSelected(true);
		directBitButton.setText("Direct Byte Mode");
		directByteButton.setText("Direct Bit Mode");
		registerButton.setText("Register Mode");
		modeGroup.add(pagedButton);
		modeGroup.add(directByteButton);
		modeGroup.add(directBitButton);
		modeGroup.add(registerButton);

		hexButton.setText("Hexadecimal");
		decButton.setText("Decimal");
		decButton.setSelected(true);
		
		resultsField.setText("                 ");  // reserve space
		
		// add the actions to the buttons
		readButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				readPushed(e);
			}
		});
		writeButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				writePushed(e);
			}
		});

		// general GUI config
		setTitle("Simple Programmer");
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

		// install items in GUI
		javax.swing.JPanel tPane;  // temporary pane for layout
		javax.swing.JPanel tPane2; 
		
		tPane = new JPanel();
			tPane.setLayout(new BoxLayout(tPane, BoxLayout.X_AXIS));
			tPane.add(Box.createHorizontalGlue());
			tPane.add(readButton);
			tPane.add(writeButton);
		getContentPane().add(tPane);
		
		tPane = new JPanel();
			tPane.setLayout(new GridLayout(2,2));
			tPane.add(new JLabel("Address:"));
			tPane.add(addrField);
			tPane.add(new JLabel("Value:"));
			tPane.add(valField);
		getContentPane().add(tPane);
		
		getContentPane().add(new JSeparator());
		
		tPane = new JPanel();
			tPane.setLayout(new BoxLayout(tPane, BoxLayout.X_AXIS));
		
		tPane2 = new JPanel();
			tPane2.setLayout(new BoxLayout(tPane2, BoxLayout.Y_AXIS));
			modeGroup.add(pagedButton);
			modeGroup.add(directBitButton);
			modeGroup.add(directByteButton);
			modeGroup.add(registerButton);
			tPane2.add(pagedButton);
			tPane2.add(directBitButton);
			tPane2.add(directByteButton);
			tPane2.add(registerButton);
		tPane.add(tPane2);

		tPane.add(new JSeparator(javax.swing.SwingConstants.VERTICAL));
		
		tPane2 = new JPanel();
			tPane2.setLayout(new GridLayout(3,1));
			radixGroup.add(decButton);
			radixGroup.add(hexButton);
			tPane2.add(new JLabel("Value is:"));
			tPane2.add(decButton);
			tPane2.add(hexButton);
		tPane.add(tPane2);
		
		getContentPane().add(tPane);

		getContentPane().add(new JSeparator());

		getContentPane().add(resultsField);

		pack();
	}
  		
  	// utility function to get value, handling radix
  	private int getNewVal() {
  		try {
	  		if (decButton.isSelected())
		  		return Integer.valueOf(valField.getText()).intValue();
	  		else
	  			return Integer.valueOf(valField.getText(),16).intValue();
  		} catch (java.lang.NumberFormatException e) { 
  			valField.setText("");
  			return 0;
  		}
  	}
  	private int getNewAddr() { 
  		try {
  			return Integer.valueOf(addrField.getText()).intValue(); 
  		} catch (java.lang.NumberFormatException e) { 
  			addrField.setText("");
  			return 0;
  		}
  	}
  	private int getNewMode() {
  		if (pagedButton.isSelected())
	  		return jmri.Programmer.PAGEMODE;
	  	else if (directBitButton.isSelected())
	  		return jmri.Programmer.DIRECTMODE;
	  	else if (directByteButton.isSelected())
	  		return jmri.Programmer.DIRECTMODE;
	  	else if (registerButton.isSelected())
	  		return jmri.Programmer.REGISTERMODE;
	  	else
	  		return 0;
  	}
  	
  	public String statusCode(int status) {
  		String temp;
  		if (status == jmri.ProgListener.OK) 
  			temp = "OK. ";
  		else
  			temp = "Error. ";
  		if ((status & jmri.ProgListener.NoLocoDetected) != 0) 
  			temp += "No Locomotive on programming track. ";
  		if ((status & jmri.ProgListener.NoAck) != 0) 
  			temp += "Decoder acknowledge not seen. ";
  		return temp;
  	}
  		
	// listen for messages from the Programmer object
	public void programmingOpReply(int value, int status) {
		resultsField.setText(statusCode(status));

		//operation over, raise the buttons
		readButton.setSelected(false);
		writeButton.setSelected(false);
		
		// capture the read value
		if (value !=-1)  // -1 implies nothing being returned
	  		if (decButton.isSelected())
		  		valField.setText(""+value);
	  		else
	  			valField.setText(Integer.toHexString(value));
	}
	
	// handle the buttons being pushed
	public void readPushed(java.awt.event.ActionEvent e) {
		try {
			resultsField.setText("programming...");
			jmri.InstanceManager.programmerInstance().readCV(getNewAddr(),getNewMode(),this);
		} catch (jmri.ProgrammerException ex) {
			resultsField.setText(""+ex);
		}
	}
	public void writePushed(java.awt.event.ActionEvent e) {
		try {
			resultsField.setText("programming...");
			jmri.InstanceManager.programmerInstance().writeCV(getNewAddr(),getNewVal(),getNewMode(),this);
		} catch (jmri.ProgrammerException ex) {
			resultsField.setText(""+ex);
		}
	}

	// handle resizing when first shown
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
	// and disconnect from the SlotManager
	
	}

}

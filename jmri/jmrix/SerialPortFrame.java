// SerialPortFrame.java

package jmri.jmrix;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Vector;

/** 
 * Abstract base Frame to open and configure a SerialPortAdapter
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Id: SerialPortFrame.java,v 1.1 2002-03-01 00:02:44 jacobsen Exp $
 */
abstract public class SerialPortFrame extends javax.swing.JFrame {

	protected javax.swing.JButton getNamesButton = new javax.swing.JButton();
	protected javax.swing.JComboBox portBox = new javax.swing.JComboBox();
	protected javax.swing.JComboBox baudBox = new javax.swing.JComboBox();
	protected javax.swing.JComboBox opt1Box = new javax.swing.JComboBox();
	protected javax.swing.JButton openPortButton = new javax.swing.JButton();

	public SerialPortFrame(String name) {
		super(name);
	}

	public void initComponents() throws Exception {
		// the following code sets the frame's initial state

		getNamesButton.setText("Get port names");
		getNamesButton.setToolTipText("Updates the list of available port names");
		getNamesButton.setVisible(true);

		portBox.setVisible(true);
		portBox.setToolTipText("Select the port to use");
		portBox.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		Vector v = adapter.getPortNames();
		for (int i=0; i<v.size(); i++)
			portBox.addItem(v.elementAt(i));
		
		String[] baudList = adapter.validBaudRates();
		for (int i=0; i<baudList.length; i++) baudBox.addItem(baudList[i]);
		String[] optList = adapter.validOption1();
		for (int i=0; i<optList.length; i++) opt1Box.addItem(optList[i]);

		openPortButton.setText("Open port");
		openPortButton.setToolTipText("Configure program to use selected port");
		openPortButton.setVisible(true);

		setLocation(new java.awt.Point(5, 40));
		setTitle("LocoBuffer connection");
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		getContentPane().add(getNamesButton);
		getContentPane().add(portBox);
		getContentPane().add(baudBox);
		getContentPane().add(opt1Box);
		getContentPane().add(openPortButton);
		
		pack();

		getNamesButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				getNamesButtonActionPerformed(e);
			}
		});
		openPortButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				try {
	 				openPortButtonActionPerformed(evt);
				} catch (jmri.jmrix.SerialConfigException ex) {
					log.error("Error while opening port.  Did you select the right one?\n"+ex);
				}
			  	catch (java.lang.UnsatisfiedLinkError ex) {
					log.error("Error while opening port.  Did you select the right one?\n"+ex);
				}
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
		Vector v = adapter.getPortNames();
		portBox.removeAllItems();
		for (int i=0; i<v.size(); i++)
			portBox.addItem(v.elementAt(i));

	}
	
	/**
	 * This member does the protocol specific processing
	 */
	abstract public void openPortButtonActionPerformed(java.awt.event.ActionEvent e) throws jmri.jmrix.SerialConfigException;
	
// Data members
	protected SerialPortAdapter adapter = null;

   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SerialPortFrame.class.getName());

}

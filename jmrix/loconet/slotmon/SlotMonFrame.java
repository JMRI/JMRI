/** 
 * SlotMonFrame.java
 *
 * Description:		Frame provinging a command station slot manager
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			
 */

package slotmon;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import loconet.SlotListener;
import loconet.LocoNetSlot;
import loconet.LnConstants;
import ErrLoggerJ.ErrLog;

public class SlotMonFrame extends javax.swing.JFrame implements loconet.SlotListener {

	// GUI member declarations
	javax.swing.JCheckBox showAllCheckBox = new javax.swing.JCheckBox();

	public SlotMonFrame() {

		// configure items for GUI
		showAllCheckBox.setText("show all slots");
		showAllCheckBox.setVisible(true);
		showAllCheckBox.setSelected(true);
		showAllCheckBox.setToolTipText("if checked, even empty/idle slots will appear");

		// general GUI config
		setTitle("Slot Monitor");
		getContentPane().setLayout(new GridLayout(4,2));

		// install items in GUI
		getContentPane().add(showAllCheckBox);
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
	// and disconnect from the SlotManager
	
	}
	
	public synchronized void notifyChangedSlot(LocoNetSlot s) {
		boolean changedState = false;
		// update model from this slot
		
		// display it in the Swing thread if changed
		if (changedState) {
			Runnable r = new Runnable() {
				public void run() { /* cause the display to be redone */ }
				};
			javax.swing.SwingUtilities.invokeLater(r);
		}
	}

String newState = "";
}

//ComboCheckBox.java

package jmri.jmrit.symbolicprog;

import java.awt.Component;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;

/* Represents a JComboBox as a JCheckBox
 *
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			
 */
public class ComboCheckBox extends JCheckBox {

	ComboCheckBox(JComboBox box) {
		super();
		_box = box;
		// listen for changes to ourself
		addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				thisActionPerformed(e);
			}
		});		
		// listen for changes to original
		_box.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				originalActionPerformed(e);
			}
		});		
	}

	void thisActionPerformed(java.awt.event.ActionEvent e) {
		// update original state to this state
		if (isSelected()) _box.setSelectedIndex(1);
		else  _box.setSelectedIndex(0);
	}

	void originalActionPerformed(java.awt.event.ActionEvent e) {
		// update this state to original state
		if (_box.getSelectedIndex()==1) setSelected(true);
		else  setSelected(false);
	}
	
	JComboBox _box = null;
}

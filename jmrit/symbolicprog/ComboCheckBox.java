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

	ComboCheckBox(JComboBox box, EnumVariableValue var) {
		super();
		_var = var;
		_box = box;
		setBackground(_var._value.getBackground());
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
		// listen for changes to original state
		_var.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
			public void propertyChange(java.beans.PropertyChangeEvent e) {
				originalPropertyChanged(e);
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
	
	void originalPropertyChanged(java.beans.PropertyChangeEvent e) {
		// update this color from original state
		if (e.getPropertyName().equals("State")) {
			if (log.isDebugEnabled()) log.debug("State change seen");
			setBackground(_var._value.getBackground());
		}	
	}
	
	EnumVariableValue _var = null;
	JComboBox _box = null;

	// initialize logging	
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ComboCheckBox.class.getName());

}

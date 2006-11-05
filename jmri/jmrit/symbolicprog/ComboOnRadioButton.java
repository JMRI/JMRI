//ComboOnRadioButton.java

package jmri.jmrit.symbolicprog;

import javax.swing.*;

/* Represents a JComboBox as a JPanel containing just the "on" button
 *
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			
 */
public class ComboOnRadioButton extends ComboRadioButtons {

	ComboOnRadioButton(JComboBox box, EnumVariableValue var) {
		super(box, var);
	}		

	/**
	 * Make only the "on" button visible
	 */
	void addToPanel(JRadioButton b, int i) {
		if (i==1) add(b);
	}

	// initialize logging	
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ComboOnRadioButton.class.getName());

}

//DecVarSlider.java

package jmri.jmrit.symbolicprog;

import java.awt.Component;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.beans.PropertyChangeEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;

/* Extends a JSlider so that its color & value are consistent with 
 * an underlying variable; we return one of these in DecValVariable.getRep.
 *
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Id: DecVarSlider.java,v 1.2 2002-01-01 01:57:25 jacobsen Exp $
 */
public class DecVarSlider extends JSlider implements ChangeListener {

	DecVarSlider(DecVariableValue var, int min, int max) {
		super(new DefaultBoundedRangeModel(0, 0, min, max));
		_var = var;
		// get the original color right
		setBackground(_var.getColor());
		// listen for changes here
		addChangeListener(this);
		// listen for changes to associated variable
		_var.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
			public void propertyChange(java.beans.PropertyChangeEvent e) {
				originalPropertyChanged(e);
			}
		});		
	}

	public void stateChanged(ChangeEvent e) {
		// called for new values of a slider - set the variable value as needed
		// e.getSource() points to the JSlider object - find it in the list
		JSlider j = (JSlider) e.getSource();
		BoundedRangeModel r = j.getModel();
		
		_var.setIntValue(r.getValue());
		_var.setState(AbstractValue.EDITTED);
	}

	DecVariableValue _var;

	void originalPropertyChanged(java.beans.PropertyChangeEvent e) {
		if (log.isDebugEnabled()) log.debug("VarSlider saw property change: "+e);
		// update this color from original state
		if (e.getPropertyName().equals("State")) {
			setBackground(_var.getColor());
		}	
		if (e.getPropertyName().equals("Value")) {
			int newValue = Integer.valueOf(((JTextField)_var.getValue()).getText()).intValue();
			setValue(newValue);
		}	
	}
	
	// initialize logging	
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DecVarSlider.class.getName());

}

/** 
 * DecVariableValue.java
 *
 * Description:		Extends VariableValue to represent a decimal variable
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			
 *
 */

package jmri.symbolicprog;

import jmri.Programmer;
import jmri.InstanceManager;
import jmri.ProgListener;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;
import javax.swing.JTextField;

public class DecVariableValue extends VariableValue implements ActionListener, PropertyChangeListener {

	public DecVariableValue(String name, String comment, boolean readOnly,
							int cvNum, String mask, Vector v) {
		super(name, comment, readOnly, cvNum, mask, v);
		_value = new JTextField();
		// connect to the JTextField value, cv
		_value.addActionListener(this);
		((CvValue)_cvVector.elementAt(getCvNum())).addPropertyChangeListener(this);
	}
	
	public void actionPerformed(ActionEvent e) {
		// called for new values - set the CV as needed
		CvValue cv = (CvValue)_cvVector.elementAt(getCvNum());
		// need eventual mask & shift, etc, but for now we store
		int oldCv = cv.getValue();
		int newVal = Integer.valueOf(_value.getText()).intValue();
		int newCv = newValue(oldCv, newVal, getMask());
		((CvValue)_cvVector.elementAt(getCvNum())).setValue(newCv);
	}
	
	// to complete this class, fill in the routines to handle "Value" parameter
	// and to read/write/hear parameter changes. 
	public Component getValue()  { return _value; }
	public void setValue(int value) { 
		if (Integer.valueOf(_value.getText()).intValue() != value) 
			prop.firePropertyChange("Value", new Integer(Integer.valueOf(_value.getText()).intValue()), new Integer(value));
		_value.setText(""+value);
	}

	public void read() {
 		setBusy(true);  // will be reset when value changes
		super.setState(READ);
		((CvValue)_cvVector.elementAt(getCvNum())).read();
	}
	
 	public void write() {
 		setBusy(true);  // will be reset when value changes
 		super.setState(STORED);
 		((CvValue)_cvVector.elementAt(getCvNum())).write();
 	}

	// handle incoming parameter notification
	public void propertyChange(java.beans.PropertyChangeEvent e) {
		// notification from CV; check for Value being changed
		if (e.getPropertyName().equals("Busy")) {
			setBusy(false);
		}
		if (e.getPropertyName().equals("Value")) {
			setBusy(false);
			// update value of Variable
			CvValue cv = (CvValue)_cvVector.elementAt(getCvNum());
			int newVal = (cv.getValue() & maskVal(getMask())) >>> offsetVal(getMask());
			setValue(newVal);  // check for duplicate done inside setVal
			// state change due to CV state change, so propagate that
			setState(cv.getState());
		}
	}

	// stored value
	JTextField _value = null;

	// initialize logging	
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DecVariableValue.class.getName());
		
}

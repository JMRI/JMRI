/** 
 * EnumVariableValue.java
 *
 * Description:		Extends VariableValue to represent a enumerated variable
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
import javax.swing.JComboBox;

public class EnumVariableValue extends VariableValue implements ActionListener, PropertyChangeListener {

	public EnumVariableValue(String name, String comment, boolean readOnly,
							int cvNum, String mask, int minVal, int maxVal,
							Vector v) {
		super(name, comment, readOnly, cvNum, mask, v);
		_maxVal = maxVal;
		_minVal = minVal;
		_value = new JComboBox();
		// connect to the JTextField value, cv
		_value.addActionListener(this);
		((CvValue)_cvVector.elementAt(getCvNum())).addPropertyChangeListener(this);
	}
	
	public void addItem(String s) {
		_value.addItem(s);
	}
	
	private int _maxVal;
	private int _minVal;
	
	public Object rangeVal() {
		return new String("enum: "+_minVal+" - "+_maxVal);
	}
	
	public void actionPerformed(ActionEvent e) {
		// called for new values - set the CV as needed
		CvValue cv = (CvValue)_cvVector.elementAt(getCvNum());
		// need eventual mask & shift, etc, but for now we store
		int oldCv = cv.getValue();
		int newVal;
		try { newVal = _value.getSelectedIndex(); }
			catch (java.lang.NumberFormatException ex) { newVal = 0; }
		int newCv = newValue(oldCv, newVal, getMask());
		((CvValue)_cvVector.elementAt(getCvNum())).setValue(newCv);
	}
	
	// to complete this class, fill in the routines to handle "Value" parameter
	// and to read/write/hear parameter changes. 
	public Component getValue()  { return _value; }
	public void setValue(int value) { 
		int oldVal;
		try { oldVal = _value.getSelectedIndex(); }
			catch (java.lang.NumberFormatException ex) { oldVal = 0; }	
		if (oldVal != value) 
			prop.firePropertyChange("Value", new Integer(oldVal), new Integer(value));
		_value.setSelectedIndex(value);
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
		else if (e.getPropertyName().equals("State")) {
			CvValue cv = (CvValue)_cvVector.elementAt(getCvNum());
			setState(cv.getState());
		}
		else if (e.getPropertyName().equals("Value")) {
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
	JComboBox _value = null;

	// clean up connections when done
	public void dispose() {
		_value.removeActionListener(this);
		((CvValue)_cvVector.elementAt(getCvNum())).removePropertyChangeListener(this);
	}
	
	// initialize logging	
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(EnumVariableValue.class.getName());
		
}

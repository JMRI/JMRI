/** 
 * DecVariableValue.java
 *
 * Description:		Extends VariableValue to represent a decimal variable
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			
 *
 */

package jmri.jmrit.symbolicprog;

import jmri.Programmer;
import jmri.InstanceManager;
import jmri.ProgListener;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class DecVariableValue extends VariableValue implements ActionListener, PropertyChangeListener {

	public DecVariableValue(String name, String comment, boolean readOnly,
							int cvNum, String mask, int minVal, int maxVal,
							Vector v, JLabel status) {
		super(name, comment, readOnly, cvNum, mask, v, status);
		_maxVal = maxVal;
		_minVal = minVal;
		_value = new JTextField(4);
		// connect to the JTextField value, cv
		_value.addActionListener(this);
		((CvValue)_cvVector.elementAt(getCvNum())).addPropertyChangeListener(this);
	}
	
	int _maxVal;
	int _minVal;
	
	public Object rangeVal() {
		return new String("Decimal: "+_minVal+" - "+_maxVal);
	}
	
	public void actionPerformed(ActionEvent e) {
		// called for new values - set the CV as needed
		CvValue cv = (CvValue)_cvVector.elementAt(getCvNum());
		// compute new cv value by combining old and request
		int oldCv = cv.getValue();
		int newVal;
		try { 
			newVal = Integer.valueOf(_value.getText()).intValue(); 
			}
			catch (java.lang.NumberFormatException ex) { newVal = 0; }
		int newCv = newValue(oldCv, newVal, getMask());
		cv.setValue(newCv);
	}
	
	// to complete this class, fill in the routines to handle "Value" parameter
	// and to read/write/hear parameter changes. 
	public String getValueString() {
		return _value.getText();
	}
	
	public void setIntValue(int i) {
		_value.setText(""+i);
	}
	
	public Component getValue()  { 
	 	if (getReadOnly())  //
	 		return new JLabel(_value.getText());
	 	else
	 		return _value; 
	}

	public Component getRep(String format)  { 
		if (getReadOnly())  //
			return new JLabel(_value.getText());
		else {
			JTextField value = new JTextField(_value.getDocument(),_value.getText(), 3);
			return value; 
		}
	}

	public void setValue(int value) { 
		int oldVal;
		try { 
			oldVal = Integer.valueOf(_value.getText()).intValue();
			}
			catch (java.lang.NumberFormatException ex) { oldVal = -999; }	
		if (log.isDebugEnabled()) log.debug("setValue with new value "+value+" old value "+oldVal);
		if (oldVal != value || getState() == VariableValue.UNKNOWN) 
			prop.firePropertyChange("Value", new Integer(oldVal), new Integer(value));
		_value.setText(""+value);
	}

	public void read() {
 		setBusy(true);  // will be reset when value changes
		super.setState(READ);
		((CvValue)_cvVector.elementAt(getCvNum())).read(_status);
	}
	
 	public void write() {
 		if (getReadOnly()) log.error("unexpected write operation when readOnly is set");
 		setBusy(true);  // will be reset when value changes
 		super.setState(STORED);
 		((CvValue)_cvVector.elementAt(getCvNum())).write(_status);
 	}

	// handle incoming parameter notification
	public void propertyChange(java.beans.PropertyChangeEvent e) {
		// notification from CV; check for Value being changed
		if (log.isDebugEnabled()) log.debug("Property changed: "+e.getPropertyName());
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

	// stored value, read-only Value
	JTextField _value = null;

	// clean up connections when done
	public void dispose() {
		if (_value != null) _value.removeActionListener(this);
		((CvValue)_cvVector.elementAt(getCvNum())).removePropertyChangeListener(this);
	}
	
	// initialize logging	
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DecVariableValue.class.getName());
		
}

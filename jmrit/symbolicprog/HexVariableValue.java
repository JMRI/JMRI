/** 
 * HexVariableValue.java
 *
 * Description:		Extends VariableValue to represent a hexadecimal variable
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			
 *
 */

package jmri.jmrit.symbolicprog;

import jmri.Programmer;
import jmri.InstanceManager;
import jmri.ProgListener;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.util.Vector;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.text.Document;

public class HexVariableValue extends VariableValue implements ActionListener, PropertyChangeListener {

	public HexVariableValue(String name, String comment, boolean readOnly,
							int cvNum, String mask, int minVal, int maxVal,
							Vector v, JLabel status, String stdname) {
		super(name, comment, readOnly, cvNum, mask, v, status, stdname);
		_maxVal = maxVal;
		_minVal = minVal;
		_value = new JTextField(4);
		_defaultColor = _value.getBackground();
		_value.setBackground(COLOR_UNKNOWN);
		// connect to the JTextField value, cv
		_value.addActionListener(this);
		((CvValue)_cvVector.elementAt(getCvNum())).addPropertyChangeListener(this);
	}
	
	int _maxVal;
	int _minVal;
	
	public Object rangeVal() {
		return new String("Hex: "+_minVal+" - "+_maxVal);
	}
	
	public void actionPerformed(ActionEvent e) {
		// called for new values - set the CV as needed
		CvValue cv = (CvValue)_cvVector.elementAt(getCvNum());
		// compute new cv value by combining old and request
		int oldCv = cv.getValue();
		int newVal;
		try { newVal = Integer.valueOf(_value.getText(), 16).intValue(); }
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

	public void setValue(int value) { 
		int oldVal;
		try { oldVal = Integer.valueOf(_value.getText(), 16).intValue(); }
			catch (java.lang.NumberFormatException ex) { oldVal = 0; }	
		if (oldVal != value || getState() == VariableValue.UNKNOWN) 
			prop.firePropertyChange("Value", null, new Integer(value));
		_value.setText(Integer.toHexString(value));
	}

	public Component getRep(String format)  { 
		if (getReadOnly())  //
			return new JLabel(_value.getText());
		else {
			JTextField value = new VarTextField(_value.getDocument(),_value.getText(), 3, this);
			return value; 
		}
	}

	Color _defaultColor;
	// implement an abstract member to set colors
	void setColor(Color c) {
		if (c != null) _value.setBackground(c);
		else _value.setBackground(_defaultColor);
		prop.firePropertyChange("Value", null, null);
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
	JTextField _value = null;

	// clean up connections when done
	public void dispose() {
		if (_value != null) _value.removeActionListener(this);
		((CvValue)_cvVector.elementAt(getCvNum())).removePropertyChangeListener(this);
	}
	
	/* Internal class extends a JTextField so that its color is consistent with 
	 * an underlying variable
	 *
	 * @author			Bob Jacobsen   Copyright (C) 2001
	 * @version			
	 */
	public class VarTextField extends JTextField {

		VarTextField(Document doc, String text, int col, HexVariableValue var) {
			super(doc, text, col);
			_var = var;
			// get the original color right
			setBackground(_var._value.getBackground());
			// listen for changes to ourself
			addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					thisActionPerformed(e);
				}
			});		
			// listen for changes to original state
			_var.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
				public void propertyChange(java.beans.PropertyChangeEvent e) {
					originalPropertyChanged(e);
				}
			});		
		}

		HexVariableValue _var;
	
		void thisActionPerformed(java.awt.event.ActionEvent e) {
			// tell original
			_var.actionPerformed(e);
		}

		void originalPropertyChanged(java.beans.PropertyChangeEvent e) {
			// update this color from original state
			if (e.getPropertyName().equals("State")) {
				setBackground(_var._value.getBackground());
			}	
		}
	
	}

	// initialize logging	
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(HexVariableValue.class.getName());
		
}

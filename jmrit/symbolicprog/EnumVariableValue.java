// EnumVariableValue.java

package jmri.jmrit.symbolicprog;

import jmri.Programmer;
import jmri.InstanceManager;
import jmri.ProgListener;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;
import javax.swing.*;
import java.awt.Color;

/** 
 * Extends VariableValue to represent a enumerated variable.
 *
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Id: EnumVariableValue.java,v 1.6 2001-11-22 09:11:06 jacobsen Exp $
 *
 */
public class EnumVariableValue extends VariableValue implements ActionListener, PropertyChangeListener {

	public EnumVariableValue(String name, String comment, boolean readOnly,
							int cvNum, String mask, int minVal, int maxVal,
							Vector v, JLabel status, String stdname) {
		super(name, comment, readOnly, cvNum, mask, v, status, stdname);
		_maxVal = maxVal;
		_minVal = minVal;
		_value = new JComboBox();
		_value.setActionCommand("");
		_defaultColor = _value.getBackground();
		_value.setBackground(COLOR_UNKNOWN);
		// connect to the JTextField value, cv
		_value.addActionListener(this);
		((CvValue)_cvVector.elementAt(getCvNum())).addPropertyChangeListener(this);
	}
	
	public void addItem(String s) {
		_value.addItem(s);
	}
	
	private int _maxVal;
	private int _minVal;
	Color _defaultColor;
	
	public Object rangeVal() {
		return new String("enum: "+_minVal+" - "+_maxVal);
	}
	
	public void actionPerformed(ActionEvent e) {
		// see if this is from _value itself, or from an alternate rep.
		// if from an alternate rep, it will contain the value to select
		if (!(e.getActionCommand().equals(""))) {
			// is from alternate rep
			_value.setSelectedItem(e.getActionCommand());
		}
		if (log.isDebugEnabled()) log.debug("action event: "+e);
		// called for new values - set the CV as needed
		CvValue cv = (CvValue)_cvVector.elementAt(getCvNum());
		// compute new cv value by combining old and request
		int oldCv = cv.getValue();
		int newVal;
		try { newVal = _value.getSelectedIndex(); }
			catch (java.lang.NumberFormatException ex) { newVal = 0; }
		int newCv = newValue(oldCv, newVal, getMask());
		if (newCv != oldCv) cv.setValue(newCv);  // to prevent CV going EDITTED during loading of decoder file
	}
	
	// to complete this class, fill in the routines to handle "Value" parameter
	// and to read/write/hear parameter changes. 
	public String getValueString() {
		return ""+_value.getSelectedIndex();
	}
	public void setIntValue(int i) {
		_value.setSelectedIndex(i);
	}
	
	public Component getValue()  { return _value; }
	public void setValue(int value) { 
		int oldVal = _value.getSelectedIndex();
		if (oldVal != value || getState() == VariableValue.UNKNOWN) 
			prop.firePropertyChange("Value", null, new Integer(value));
		_value.setSelectedIndex(value);
	}

	public Component getRep(String format) {
		// sort on format type
		if (format.equals("checkbox")) {
			// this only makes sense if there are exactly two options

			return new ComboCheckBox(_value, this);
			//box.setActionCommand( ((String)(_value.getItemAt(1))) ); // set to name of "1" item
			//box.addActionListener(this);
		}
		else if (format.equals("radiobuttons")) {
			return new ComboRadioButtons(_value, this);
		}
		else 
		// return a new JComboBox representing the same model
		return new VarComboBox(_value.getModel(), this);
	}
	
	
	// implement an abstract member to set colors
	void setColor(Color c) {
		if (c != null) _value.setBackground(c);
		else _value.setBackground(_defaultColor);
		prop.firePropertyChange("Value", null, null);
	}

	// member functions to control reading/writing the variables
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
			if (((Boolean)e.getNewValue()).equals(Boolean.FALSE)) setBusy(false);
		}
		else if (e.getPropertyName().equals("State")) {
			CvValue cv = (CvValue)_cvVector.elementAt(getCvNum());
			setState(cv.getState());
		}
		else if (e.getPropertyName().equals("Value")) {
			// update value of Variable
			CvValue cv = (CvValue)_cvVector.elementAt(getCvNum());
			int newVal = (cv.getValue() & maskVal(getMask())) >>> offsetVal(getMask());
			setValue(newVal);  // check for duplicate done inside setVal
		}
	}

	// stored value
	JComboBox _value = null;

	// clean up connections when done
	public void dispose() {
		if (_value != null) _value.removeActionListener(this);
		((CvValue)_cvVector.elementAt(getCvNum())).removePropertyChangeListener(this);
	}
	
	/* Internal class extends a JComboBox so that its color is consistent with 
	 * an underlying variable; we return one of these in getRep.
	 *<P>
	 * Unlike similar cases elsewhere, this doesn't have to listen to
	 * value changes.  Those are handled automagically since we're sharing the same
	 * model between this object and the real JComboBox value.
	 *
	 * @author			Bob Jacobsen   Copyright (C) 2001
	 * @version			
	 */
	public class VarComboBox extends JComboBox {

		VarComboBox(ComboBoxModel m, EnumVariableValue var) {
			super(m);
			_var = var;
			// get the original color right
			setBackground(_var._value.getBackground());
			// listen for changes to original state
			_var.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
				public void propertyChange(java.beans.PropertyChangeEvent e) {
					if (log.isDebugEnabled()) log.debug("VarComboBox saw property change: "+e);
					originalPropertyChanged(e);
				}
			});		
		}

		EnumVariableValue _var;
	
		void originalPropertyChanged(java.beans.PropertyChangeEvent e) {
			// update this color from original state
			if (e.getPropertyName().equals("State")) {
				setBackground(_var._value.getBackground());
			}	
		}
	
	}

	// initialize logging	
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(EnumVariableValue.class.getName());
		
}

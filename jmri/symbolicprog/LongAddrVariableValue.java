/** 
 * LongAddrVariableValue.java
 *
 * Description:		Extends VariableValue to represent a NMRA long address
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

public class LongAddrVariableValue extends VariableValue implements ActionListener, PropertyChangeListener {

	public LongAddrVariableValue(String name, String comment, boolean readOnly,
							int cvNum, String mask, int minVal, int maxVal,
							Vector v) {
		super(name, comment, readOnly, cvNum, mask, v);
		_maxVal = maxVal;
		_minVal = minVal;
		_value = new JTextField();
		// connect to the JTextField value, cv
		_value.addActionListener(this);
		// connect for notification
		((CvValue)_cvVector.elementAt(getCvNum())).addPropertyChangeListener(this);
		((CvValue)_cvVector.elementAt(getCvNum()+1)).addPropertyChangeListener(this);
	}
	
	// the connection is to cvNum and cvNum+1
	
	int _maxVal;
	int _minVal;
	
	public Object rangeVal() {
		return new String("Long address");
	}
	
	public void actionPerformed(ActionEvent e) {
		// called for new values - set the CV as needed
		CvValue cvl = (CvValue)_cvVector.elementAt(getCvNum());
		CvValue cvh = (CvValue)_cvVector.elementAt(getCvNum()+1);
		// no masking involved for long address
		int oldCvL = cvl.getValue();
		int oldCvH = cvh.getValue();
		int newVal;
		try { newVal = Integer.valueOf(_value.getText()).intValue(); }
			catch (java.lang.NumberFormatException ex) { newVal = 0; }
			
		// no masked combining of old value required, as this fills the two CVs
		int newCvH = newVal/128;
		int newCvL = newVal - (newCvH*128);
		cvl.setValue(newCvL);
		cvh.setValue(newCvH);
	}
	
	// to complete this class, fill in the routines to handle "Value" parameter
	// and to read/write/hear parameter changes. 
	public String getValueString() {
		return _value.getText();
	}
	public void setIntValue(int i) {
		_value.setText(""+i);
	}
	
	public Component getValue()  { return _value; }
	public void setValue(int value) { 
		int oldVal;
		try { oldVal = Integer.valueOf(_value.getText()).intValue(); }
			catch (java.lang.NumberFormatException ex) { oldVal = 0; }	
		if (oldVal != value) 
			prop.firePropertyChange("Value", new Integer(oldVal), new Integer(value));
		_value.setText(""+value);
	}

	private int _progState = 0;
	private static final int IDLE = 0;
	private static final int READING_FIRST = 1;
	private static final int READING_SECOND = 2;
	private static final int WRITING_FIRST = 3;
	private static final int WRITING_SECOND = 4;
	
	// 
	public void read() {
 		setBusy(true);  // will be reset when value changes
		super.setState(READ);
		if (_progState != IDLE) log.warn("Programming state "+_progState+", not IDLE, in read()");
		_progState = READING_FIRST;
		((CvValue)_cvVector.elementAt(getCvNum())).read();
	}
	
 	public void write() {
 		if (getReadOnly()) log.error("unexpected write operation when readOnly is set");
 		setBusy(true);  // will be reset when value changes
 		super.setState(STORED);
		if (_progState != IDLE) log.warn("Programming state "+_progState+", not IDLE, in write()");
		_progState = WRITING_FIRST;
 		((CvValue)_cvVector.elementAt(getCvNum())).write();
 	}

	// handle incoming parameter notification
	public void propertyChange(java.beans.PropertyChangeEvent e) {
		// notification from CV; check for Value being changed
		if (e.getPropertyName().equals("Busy")) {
			// see if this was a read or write operation
			switch (_progState) {
				case IDLE:  // no, just a CV update
						setBusy(false);
						return;
				case READING_FIRST:
				case READING_SECOND:  // ignore
						return;
				case WRITING_FIRST:  // no, just a CV update
						setBusy(true);  // will be reset when value changes
 						super.setState(STORED);
						_progState = WRITING_SECOND;
 						((CvValue)_cvVector.elementAt(getCvNum()+1)).write();
						return;
				case WRITING_SECOND:  // now done with complete request
						_progState = IDLE;
						setBusy(false);
						return;
				default:  // unexpected!
						log.error("Unexpected state found: "+_progState);
						_progState = IDLE;
						return;
			}
		}
		else if (e.getPropertyName().equals("State")) {
			CvValue cv = (CvValue)_cvVector.elementAt(getCvNum());
			setState(cv.getState());
		}
		else if (e.getPropertyName().equals("Value")) {
			setBusy(false);
			// update value of Variable
			CvValue cvl = (CvValue)_cvVector.elementAt(getCvNum());
			CvValue cvh = (CvValue)_cvVector.elementAt(getCvNum()+1);
			int newVal = cvh.getValue()*128 + cvl.getValue();
			setValue(newVal);  // check for duplicate done inside setVal
			// state change due to CV state change, so propagate that
			setState(cvl.getState());
			// see if this was a read or write operation
			switch (_progState) {
				case IDLE:  // no, just a CV update
						return;
				case READING_FIRST:  // yes, now read second
						setBusy(true);  // will be reset when value changes
						super.setState(READ);
						_progState = READING_SECOND;
						((CvValue)_cvVector.elementAt(getCvNum()+1)).read();
						return;
				case READING_SECOND:  // now done with complete request
						_progState = IDLE;
						return;
				default:  // unexpected!
						log.error("Unexpected state found: "+_progState);
						_progState = IDLE;
						return;
			}
		}
	}

	// stored value
	JTextField _value = null;

	// clean up connections when done
	public void dispose() {
		_value.removeActionListener(this);
		((CvValue)_cvVector.elementAt(getCvNum())).removePropertyChangeListener(this);
	}
	
	// initialize logging	
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LongAddrVariableValue.class.getName());
		
}

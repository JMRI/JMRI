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

import java.util.Vector;
import javax.swing.JTextField;

public class DecVariableValue extends VariableValue {

	public DecVariableValue(String name, String comment, boolean readOnly,
							int cvNum, String mask, Vector v) {
		super(name, comment, readOnly, cvNum, mask, v);
		_value = new JTextField();
	}
	
	// to complete this class, fill in the routines to handle "Value" parameter
	// and to read/write/hear parameter changes.  But what should the type be?
	public Object getValue()  { return _value; }
	public void setValue(int value) { 
		// if (_value != value) prop.firePropertyChange("Value", new Integer(_value), new Integer(value));
		_value.setText(""+value);
		super.setState(EDITTED); 
	}

	public void read() {
	}
	
 	public void write() {
 	}

	// handle incoming parameter notification
	public void propertyChange(java.beans.PropertyChangeEvent e) {
	}

	// stored value
	JTextField _value = null;

	// initialize logging	
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DecVariableValue.class.getName());
		
}

/** 
 * VariableValue.java
 *
 * Description:		Represents a single Variable value; abstract base class
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			
 *
 */

package jmri.symbolicprog;

import jmri.Programmer;
import jmri.InstanceManager;
import jmri.ProgListener;

abstract class VariableValue implements java.beans.PropertyChangeListener {

	// to complete this class, fill in the routines to handle "Value" parameter
	// and to read/write/hear parameter changes.  But what should the type be?
	public int getValue()  { return _value; }
	public void setValue(int value) { 
		if (_value != value) prop.firePropertyChange("Value", new Integer(_value), new Integer(value));
		_value = value; 
		setState(EDITTED); 
	}
	private int _value = 0;

	abstract public void read();
	
	abstract public void write();

	// handle incoming parameter notification
	public void propertyChange(java.beans.PropertyChangeEvent e) {
	}


	// methods implemented here:
	public VariableValue(String name) { _name = name; }
	public String name() { return _name; }
	private String _name;
		
	public int getState()  { return _state; }
	private void setState(int state) {
		if (_state != state) prop.firePropertyChange("State", new Integer(_state), new Integer(state));
		_state = state;
	}
	private int _state = 0;
	
	// states
	public static final int UNKNOWN = 0;
	public static final int EDITTED = 4;
	public static final int READ    = 16;
	public static final int STORED  = 64;
	
	// busy during read, write operations
	public boolean isBusy() { return _busy; }
	private void setBusy(boolean busy) {
		if (_busy != busy) prop.firePropertyChange("Busy", new Boolean(_busy), new Boolean(busy));
		_busy = busy;
	}
	private boolean _busy = false;
	
	// handle outgoing parameter notification
	java.beans.PropertyChangeSupport prop = new java.beans.PropertyChangeSupport(this);	
	public void removePropertyChangeListener(java.beans.PropertyChangeListener p) { prop.removePropertyChangeListener(p); }
	public void addPropertyChangeListener(java.beans.PropertyChangeListener p) { prop.addPropertyChangeListener(p); }
		
	// initialize logging	
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(VariableValue.class.getName());
		
}

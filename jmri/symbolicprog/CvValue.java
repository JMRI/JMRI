/** 
 * CvValue.java
 *
 * Description:		Represents a single CV value
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			
 */

package jmri.symbolicprog;

import jmri.Programmer;
import jmri.InstanceManager;
import jmri.ProgListener;

public class CvValue implements ProgListener {

	public CvValue(int num) { _num = num; }
	public int number() { return _num; }
	private int _num;
	
	public int getValue()  { return _value; }
	public void setValue(int value) { 
		if (_value != value) prop.firePropertyChange("Value", new Integer(_value), new Integer(value));
		_value = value; 
		setState(EDITTED); 
	}
	private int _value = 0;
	
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
	
	// read, write operations
	public boolean isBusy() { return _busy; }
	private void setBusy(boolean busy) {
		if (_busy != busy) prop.firePropertyChange("Busy", new Boolean(_busy), new Boolean(busy));
		_busy = busy;
	}
	private boolean _busy = false;

	private boolean _reading = false;
	
	public void read() {
		// get a programmer reference and write
		Programmer p = InstanceManager.programmerInstance();
		if (p != null) {
			_busy = true;
			_reading = true;
			try {
				p.readCV(_num, this);
			} catch (Exception e) {
				log.warn("Exception during CV read: "+e);
				setBusy(false);
			}
		} else {
			log.error("No programmer available!");
		}
	}
	
	public void write() {
		// get a programmer reference and write
		Programmer p = InstanceManager.programmerInstance();
		if (p != null) {
			_busy = true;
			_reading = false;
			try {
				p.writeCV(_num, _value, this);
			} catch (Exception e) {
				log.warn("Exception during CV write: "+e);
				setBusy(false);
			}
			setState(UNKNOWN);
		} else {
			log.error("No programmer available!");
		}
	}
	
	public void programmingOpReply(int value, int status) {
		if (!_busy) log.error("opReply when not busy!");
		_busy = false;
		if (status == OK) {
			if (_reading) {
				setValue(value);
				setState(READ);
			} else {
				setState(STORED);
			}
		} else {
			setState(UNKNOWN);
		}
	}

	// handle parameter notification
	java.beans.PropertyChangeSupport prop = new java.beans.PropertyChangeSupport(this);	
	public void removePropertyChangeListener(java.beans.PropertyChangeListener p) { prop.removePropertyChangeListener(p); }
	public void addPropertyChangeListener(java.beans.PropertyChangeListener p) { prop.addPropertyChangeListener(p); }
		
	// initialize logging	
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(CvValue.class.getName());
		
}

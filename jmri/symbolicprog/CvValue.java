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

import javax.swing.JLabel;

public class CvValue implements ProgListener {

	public CvValue(int num) { _num = num; }
	public int number() { return _num; }
	private int _num;
	
	private JLabel _status = null;
	
	public int getValue()  { return _value; }
	public void setValue(int value) { 
		if (_value != value) {
			int old = _value;
			_value = value;
			setState(EDITTED);
			prop.firePropertyChange("Value", new Integer(old), new Integer(value)); 
		}
	}
	private int _value = 0;
	
	public int getState()  { return _state; }
	void setState(int state) {  // package scope
		_state = state;
		if (_state != state) prop.firePropertyChange("State", new Integer(_state), new Integer(state));
	}
	private int _state = 0;
	
	// states
	public static final int UNKNOWN  =   0;
	public static final int EDITTED  =   4;
	public static final int READ     =  16;
	public static final int STORED   =  64;
	public static final int FROMFILE = 256;
	
	// read, write operations
	public boolean isBusy() { return _busy; }
	private void setBusy(boolean busy) {
		if (_busy != busy) prop.firePropertyChange("Busy", new Boolean(_busy), new Boolean(busy));
		_busy = busy;
	}
	private boolean _busy = false;

	private boolean _reading = false;
	
	public void read(JLabel status) {
		// get a programmer reference and write
		_status = status;
		if (status != null) status.setText("Reading...");
		Programmer p = InstanceManager.programmerInstance();
		if (p != null) {
			_busy = true;
			_reading = true;
			try {
				p.readCV(_num, this);
			} catch (Exception e) {
				if (status != null) status.setText("Exception during CV read: "+e);
				log.warn("Exception during CV read: "+e);
				setBusy(false);
			}
		} else {
			if (status != null) status.setText("No programmer available!");
			log.error("No programmer available!");
		}
	}
	
	public void write(JLabel status) {
		// get a programmer reference and write
		_status = status;
		if (status != null) status.setText("Writing...");
		Programmer p = InstanceManager.programmerInstance();
		if (p != null) {
			_busy = true;
			_reading = false;
			try {
				p.writeCV(_num, _value, this);
			} catch (Exception e) {
				if (status != null) status.setText("Exception during CV write: "+e);
				log.warn("Exception during CV write: "+e);
				setBusy(false);
			}
			setState(UNKNOWN);
		} else {
			if (status != null) status.setText("No programmer available!");
			log.error("No programmer available!");
		}
	}
	
	public void programmingOpReply(int value, int retval) {
		if (!_busy) log.error("opReply when not busy!");
		setBusy(false);
		if (retval == OK) {
			if (_status != null) _status.setText("OK");
			if (_reading) {
				setState(READ);
				// set & notify value directly to avoid state going to EDITTED
				if (_value != value) {
					int old = _value;
					_value = value;
					prop.firePropertyChange("Value", new Integer(old), new Integer(value)); 
				}
			} else {  // writing
				setState(STORED);
			}
		} else {
			if (_status != null) _status.setText("Programmer returned error status "+retval);
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

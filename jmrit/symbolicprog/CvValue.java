/** 
 * CvValue.java
 *
 * Description:		Represents a single CV value
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			
 */

package jmri.jmrit.symbolicprog;

import jmri.Programmer;
import jmri.InstanceManager;
import jmri.ProgListener;

import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class CvValue extends AbstractValue implements ProgListener {

	public CvValue(int num) { 
		_num = num;
		_tableEntry = new JTextField(3);
		_defaultColor = _tableEntry.getBackground();
		_tableEntry.setBackground(COLOR_UNKNOWN);
	}
	public int number() { return _num; }
	private int _num;
	
	private JLabel _status = null;
	
	public int getValue()  { return _value; }
	public void setValue(int value) { 
		setState(EDITTED);
		if (_value != value) {
			int old = _value;
			_value = value;
			_tableEntry.setText(""+value);
			prop.firePropertyChange("Value", null, new Integer(value)); 
		}
	}
	private int _value = 0;
	
	public int getState()  { return _state; }
	public void setState(int state) {
		if (log.isDebugEnabled()) log.debug("set state from "+_state+" to "+state);
		int oldstate = _state;
		_state = state;
		switch (state) {
			case UNKNOWN : setColor(COLOR_UNKNOWN ); break;
			case EDITTED : setColor(COLOR_EDITTED ); break;
			case READ    : setColor(COLOR_READ    ); break;
			case STORED  : setColor(COLOR_STORED  ); break;
			case FROMFILE: setColor(COLOR_FROMFILE); break;
			default:      log.error("Inconsistent state: "+_state);
		}
		if (oldstate != state) prop.firePropertyChange("State", new Integer(oldstate), new Integer(state));
	}
	private int _state = 0;
		
	// read, write operations
	public boolean isBusy() { return _busy; }
	private void setBusy(boolean busy) {
		if (log.isDebugEnabled()) log.debug("set busy from "+_busy+" to "+busy+" state "+_state);
		if (_busy != busy) prop.firePropertyChange("Busy", new Boolean(_busy), new Boolean(busy));
		_busy = busy;
	}
	private boolean _busy = false;

	// color management
	Color _defaultColor;
	void setColor(Color c) {
		if (c != null) _tableEntry.setBackground(c);
		else _tableEntry.setBackground(_defaultColor);
		//prop.firePropertyChange("Value", null, null);
	}

	// object for Table entry
	JTextField _tableEntry = null;
	JTextField getTableEntry() { 
		return _tableEntry;
	}
	
	// read, write support
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
		if (log.isDebugEnabled()) log.debug("CV progOpReply with retval "+retval
											+" _reading "+_reading);
		if (!_busy) log.error("opReply when not busy!");
		setBusy(false);
		if (retval == OK) {
			if (_status != null) _status.setText("OK");
			if (_reading) {
				setState(READ);
				// set & notify value directly to avoid state going to EDITTED
				int old = _value;
				_value = value;
				if (log.isDebugEnabled()) log.debug("CV firePropChange Value "+old+" "+value);
				prop.firePropertyChange("Value", null, new Integer(value)); 
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

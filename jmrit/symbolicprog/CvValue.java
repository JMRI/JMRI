// CvValue.java

package jmri.jmrit.symbolicprog;

import jmri.Programmer;
import jmri.InstanceManager;
import jmri.ProgListener;

import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.JTextField;


/** 
 * Encapsulate a single CV value and provide programming access to the decoder.
 *
 *<P>There are three relevant parameters:  Busy, Value, State.  Busy == true means
 * that a read or write operation is going on.  When it transitions to "false", the
 * operation is complete, and the Value and State are stable.  During a read
 * operation, Value changes before State, so you can assume that Value is stable
 * if notified of a State change.
 *
 * Description:		Represents a single CV value
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Id: CvValue.java,v 1.11 2002-01-02 23:48:57 jacobsen Exp $
 */
public class CvValue extends AbstractValue implements ProgListener {

	public CvValue(int num) { 
		_num = num;
		_tableEntry = new JTextField("0", 3);
		_defaultColor = _tableEntry.getBackground();
		_tableEntry.setBackground(COLOR_UNKNOWN);
	}
	public int number() { return _num; }
	private int _num;
	
	private JLabel _status = null;
	
	public int getValue()  { return _value; }
	
	Color getColor() { return _tableEntry.getBackground(); }
	
	protected void notifyValueChange(int value) {
		prop.firePropertyChange("Value", null, new Integer(value)); 
	}
	/**
	 * Edit a new value into the CV. Only use this for external edits, e.g. set form a GUI,
	 * not for internal uses, as it sets the state to EDITTED
	 */
	public void setValue(int value) { 
		setState(EDITTED);
		if (_value != value) {
			int old = _value;
			_value = value;
			_tableEntry.setText(""+value);
			notifyValueChange(value); 
		}
	}
	private int _value = 0;
	
	public int getState()  { return _state; }
	/**
	 * Set state value and send notification.  Also sets GUI color as needed.
	 */
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
	
	/**
	 * set the busy state and send notification. Should be used _only_ if 
	 * this is the only thing changing
	 */
	private void setBusy(boolean busy) {
		if (log.isDebugEnabled()) log.debug("setBusy from "+_busy+" to "+busy+" state "+_state);
		boolean oldBusy = _busy;
		_busy = busy;
		notifyBusyChange(oldBusy, busy);
	}
	/**
	 * Notify of changes to the busy state
	 */
	private void notifyBusyChange(boolean oldBusy, boolean newBusy) {
		if (log.isDebugEnabled()) log.debug("notifyBusy from "+oldBusy+" to "+newBusy+" current state "+_state);
		if (oldBusy != newBusy) prop.firePropertyChange("Busy", 
			oldBusy ? Boolean.TRUE : Boolean.FALSE,
			newBusy ? Boolean.TRUE : Boolean.FALSE);
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
	private boolean _confirm = false;
	
	public void read(JLabel status) {
		if (log.isDebugEnabled()) log.debug("read call with Cv number "+_num);
		// get a programmer reference and write
		_status = status;
		if (status != null) status.setText("Reading...");
		Programmer p = InstanceManager.programmerInstance();
		if (p != null) {
			setBusy(true);
			_reading = true;
			_confirm = false;
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

	public void confirm(JLabel status) {
		if (log.isDebugEnabled()) log.debug("confirm call with Cv number "+_num);
		// get a programmer reference and write
		_status = status;
		if (status != null) status.setText("Reading...");
		Programmer p = InstanceManager.programmerInstance();
		if (p != null) {
			setBusy(true);
			_reading = false;
			_confirm = true;
			try {
				p.confirmCV(_num, _value, this);
			} catch (Exception e) {
				if (status != null) status.setText("Exception during CV confirm: "+e);
				log.warn("Exception during CV read: "+e);
				setBusy(false);
			}
		} else {
			if (status != null) status.setText("No programmer available!");
			log.error("No programmer available!");
		}
	}
	
	public void write(JLabel status) {
		if (log.isDebugEnabled()) log.debug("write call with Cv number "+_num);
		// get a programmer reference and write
		_status = status;
		if (status != null) status.setText("Writing...");
		Programmer p = InstanceManager.programmerInstance();
		if (p != null) {
			setBusy(true);
			_reading = false;
			_confirm = false;
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
		if (log.isDebugEnabled()) log.debug("CV progOpReply for CV "+_num+" with retval "+retval
											+" during "
											+(_reading?"read sequence":
												(_confirm?"confirm sequence":"write sequence")));
		if (!_busy) log.error("opReply when not busy!");
		boolean oldBusy = _busy;
		if (retval == OK) {
			if (_status != null) _status.setText("OK");
			if (_reading) {
				// set & notify value directly to avoid state going to EDITTED
				int old = _value;
				_value = value;
				_tableEntry.setText(Integer.toString(value));
				notifyValueChange(value);
				setState(READ);
				if (log.isDebugEnabled()) log.debug("CV setting not busy on end read");
				_busy = false;
				notifyBusyChange(oldBusy, _busy);
			}
			else if (_confirm) {
				// value doesn't change, just the state
				setState(READ);
				_busy = false;
				notifyBusyChange(oldBusy, _busy);
			} else {  // writing
				setState(STORED);
				_busy = false;
				notifyBusyChange(oldBusy, _busy);
			}
		} else {
			if (_status != null) _status.setText("Programmer error: "
											+InstanceManager.programmerInstance().decodeErrorCode(retval));
			
			// delay to ensure that the message appears!
			javax.swing.Timer timer = new javax.swing.Timer(1000, new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							errorTimeout();
						}
					});
			timer.setInitialDelay(1000);
			timer.setRepeats(false);
			timer.start();
		}
		if (log.isDebugEnabled()) log.debug("CV progOpReply end of handling CV "+_num);
	}

	void errorTimeout() {
		setState(UNKNOWN);
		if (log.isDebugEnabled()) log.debug("CV setting not busy on error reply");
		_busy = false;
		notifyBusyChange(true, _busy);
	}
	
	// handle parameter notification
	java.beans.PropertyChangeSupport prop = new java.beans.PropertyChangeSupport(this);	
	public void removePropertyChangeListener(java.beans.PropertyChangeListener p) { prop.removePropertyChangeListener(p); }
	public void addPropertyChangeListener(java.beans.PropertyChangeListener p) { prop.addPropertyChangeListener(p); }
		
	// clean up connections when done
	public void dispose() {
		if (log.isDebugEnabled()) log.debug("dispose");
	}

	// initialize logging	
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(CvValue.class.getName());
		
}

/** 
 * AbstractTurnout.java
 *
 * Description:		Abstract class providing the basic logic of the Turnout interface
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			
 */
 
 /**
  * Note that we consider it an error for there to be more than one object
  * that corresponds to a particular physical turnout on the layout.
  */

package jmri;
import jmri.Turnout;

public abstract class AbstractTurnout implements Turnout, java.io.Serializable {

	// Abstract member to handle a request to change state, typically by 
	// sending a message to the layout in some child class
	abstract protected void forwardCommandChangeToLayout(int s) throws jmri.JmriException;

	
	// implementing classes will typically have a function/listener to get
	// updates from the layout, which will then call 
	//		public void firePropertyChange(String propertyName,
	//										Object oldValue,
	//										Object newValue)	 
	// _once_ if anything has changed state
	
	// Implementation methods

	// the following sets a new Commanded state, if need be notifying the
	// listeners, but does NOT send the command downstream.  This is used
	// when a new commanded state is noticed from another command.
	public void newCommandedState(int s) {
		if (_commandedState != s) {
			int oldState = _commandedState;
			_commandedState = s;
			firePropertyChange("CommandedState", new Integer(oldState), 
								new Integer(_commandedState));
			}
	}
	
	// interface function implementations

	public String getUserName() {return _id;}
	public void   setUserName(String s) {
		String old = _id;
		_id = s;
		firePropertyChange("UserName", old, s);
	}

	public int getKnownState() {return _knownState;}

	public void setCommandedState(int s) throws jmri.JmriException {
		forwardCommandChangeToLayout(s);
		newCommandedState(s);   
	}
	
	public int getCommandedState() {return _commandedState;}
		
	public int getFeedbackType() {return _feedbackType;}
	
	// add a protected setKnownState() for implementations. Not intended for general use
	protected void setKnownState(int s) {
		if (_knownState != s) {
			int oldState = _knownState;
			_knownState = s;
			firePropertyChange("KnownState", new Integer(oldState), new Integer(_knownState));
			}
	}

	// internal data members
	private String _id;
	private int _feedbackType   = UNKNOWN;
	private int _knownState     = UNKNOWN;
	private int _commandedState = UNKNOWN;
	
	// since we can't do a "super(this)" in the ctor to inherit from PropertyChangeSupport, we'll
	// reflect to it.
	// Note that dispose() doesn't act on these.  Its not clear whether it should...
	java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);

	public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) { 
		pcs.addPropertyChangeListener(l);
	}

	protected void firePropertyChange(String p, Object old, Object n) { pcs.firePropertyChange(p,old,n);}

	public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) { 
		pcs.removePropertyChangeListener(l);
	}		
}


/* @(#)AbstractTurnout.java */

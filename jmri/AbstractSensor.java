/**
 * AbstractSensor.java
 *
 * Description:		Abstract class providing the basic logic of the Sensor interface
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version
 */

package jmri;
import jmri.Sensor;

public abstract class AbstractSensor implements Sensor, java.io.Serializable {

	// ctor takes a system-name string for initialization
	public AbstractSensor(String s) { _id = s; }

	// implementing classes will typically have a function/listener to get
	// updates from the layout, which will then call
	//		public void firePropertyChange(String propertyName,
	//										Object oldValue,
	//										Object newValue)
	// _once_ if anything has changed state

	// interface function implementations

	public String getID() {return _id;}

	public int getKnownState() {return _knownState;}

	// setKnownState() for implementations that can't
    // actually do it on the layout. Not intended for use by implementations
    // that can
	public void setKnownState(int s) throws jmri.JmriException {
		if (_knownState != s) {
			int oldState = _knownState;
			_knownState = s;
			firePropertyChange("KnownState", new Integer(oldState), new Integer(_knownState));
        }
	}

	// internal data members
	private String _id;
	protected int _knownState     = UNKNOWN;

	// since we can't do a "super(this)" in the ctor to inherit from PropertyChangeSupport, we'll
	// reflect to it
	java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);
	public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
		pcs.addPropertyChangeListener(l);
		}
	public void firePropertyChange(String p, Object old, Object n) { pcs.firePropertyChange(p,old,n);}
	public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
		pcs.removePropertyChangeListener(l);
		}

}


/* @(#)AbstractSensor.java */

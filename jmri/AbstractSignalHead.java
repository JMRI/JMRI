// AbstractSignalHead.java

package jmri;
import jmri.Sensor;

 /**
 * Abstract class providing the basic logic of the Sensor interface
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version         $Revision: 1.1 $
 */
public abstract class AbstractSignalHead implements SignalHead, java.io.Serializable {

	// implementing classes will typically have a function/listener to get
	// updates from the layout, which will then call
	//		public void firePropertyChange(String propertyName,
	//										Object oldValue,
	//										Object newValue)
	// _once_ if anything has changed state


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


/* @(#)AbstractSignalHead.java */

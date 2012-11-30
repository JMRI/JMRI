/**
 * 
 */
package jmri.jmrit.operations;

import java.beans.*;

/**
 * Implements some common base functionality for building classes that implement
 * the Java Beans conventions.
 * 
 * This will probably get more functionality as time goes on.
 * 
 * @author Gregory Madsen Copyright (C) 2012
 * 
 */
public class BeanBase {

	public BeanBase() {
		// TODO Auto-generated constructor stub
	}

	PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
		pcs.addPropertyChangeListener(l);
	}

	public synchronized void removePropertyChangeListener(
			PropertyChangeListener l) {
		pcs.removePropertyChangeListener(l);
	}

	protected void firePropertyChange(String propName, Object oldValue,
			Object newValue) {
		pcs.firePropertyChange(propName, oldValue, newValue);
	}


}

/**
 * 
 */
package jmri.jmrit.operations;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Extends the Beans PropertyChangeSupport class to facilitate testing by adding
 * properties that can be examined to see what happened. Note that reset() must
 * be called between expected event firings, to clear out the history.
 * 
 * More features will probably be added over time. 
 * 
 * @author Gregory Madsen Copyright (C) 2012
 * 
 */
public class OpsPropertyChangeListener implements PropertyChangeListener {

	private Boolean fired = false;

	public Boolean isFired() {
		return fired;
	}

	private PropertyChangeEvent event = null;

	public PropertyChangeEvent getEvent() {
		return event;
	}

	public OpsPropertyChangeListener() {
		reset();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// Here we record what happened, so that it can be checked from outside
		// the class, as in a test.
		fired = true;
		event = evt;
	}

	public void reset() {
		fired = false;
		event = null;
	}

}

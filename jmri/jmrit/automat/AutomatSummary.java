// AutomatSummary.java

package jmri.jmrit.automat;

import com.sun.java.util.collections.ArrayList;

/**
 * A singlet providing access to information about
 * existing Automat instances.
 *<P>
 * It might not always be a singlet, however, so
 * for now we're going through an explicit instance() reference.
 *
 * @author	Bob Jacobsen    Copyright (C) 2004
 * @version     $Revision: 1.1 $
 */
public class AutomatSummary  {

    private AutomatSummary() {}

	static private AutomatSummary self = null;

	static public AutomatSummary instance() {
		if (self == null) self = new AutomatSummary();
		return self;
	}

	ArrayList automats = new ArrayList();

    java.beans.PropertyChangeSupport prop = new java.beans.PropertyChangeSupport(this);
    public void removePropertyChangeListener(java.beans.PropertyChangeListener p) { prop.removePropertyChangeListener(p); }
    public void addPropertyChangeListener(java.beans.PropertyChangeListener p) { prop.addPropertyChangeListener(p); }

	/**
	 * A newly-created AbstractAutomaton instance uses this method to
	 * notify interested parties of it's existance.
	 */
	public void register(AbstractAutomaton a) {
		automats.add(a);

		//notify length changed
		prop.firePropertyChange("Insert", null, new Integer(automats.indexOf(a)));

	}

	/**
	 * Just before exiting, an AbstractAutomaton instance uses this method to
	 * notify interested parties of it's departure.
	 */
	public void remove(AbstractAutomaton a) {
                int index = automats.indexOf(a);
		automats.remove(a);

		//notify length changed
		prop.firePropertyChange("Remove", null, new Integer(index));
	}

	public int length() {
		return automats.size();  // debugging value
	}

	public AbstractAutomaton get(int i) {
		return (AbstractAutomaton)automats.get(i);
	}

	public int indexOf(AbstractAutomaton a) {
		return automats.indexOf(a);
	}

	/**
	 * An AbstractAutomaton instance uses this method to
	 * notify interested parties that it's gone around it's
	 * handle loop again.
	 */
	public void loop(AbstractAutomaton a) {
		int i = automats.indexOf(a);
		prop.firePropertyChange("Count", null, new Integer(i));
	}

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(AutomatSummary.class.getName());
}

/* @(#)AutomatSummary.java */

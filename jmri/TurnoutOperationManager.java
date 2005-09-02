/**
 * 
 */
package jmri;

import com.sun.java.util.collections.TreeMap;


import com.sun.java.util.collections.Collection;
import com.sun.java.util.collections.List;
import com.sun.java.util.collections.LinkedList;
import com.sun.java.util.collections.Iterator;

/**
 * class to look after the collection of TurnoutOperation subclasses
 * Unlike the other xxxManager, this does not inherit from AbstractManager since
 * the resources it deals with are not DCC system resources but rather
 * purely internal state
 * @author John Harper	Copyright 2005
 *
 */
public class TurnoutOperationManager {

	private TreeMap turnoutOperations = new TreeMap();
	private List operationTypes = new LinkedList(); // array of the defining instances of each class, held in order of appearance
	boolean doOperations = false;			// global on/off switch
	static TurnoutOperationManager theInstance;
	
    public TurnoutOperationManager() {
    }

    public void dispose() {
    }
    
	public TurnoutOperation[] getTurnoutOperations() {
		synchronized (this) {
			Collection entries = turnoutOperations.values();
			return (TurnoutOperation[])entries.toArray(new TurnoutOperation[0]); 
		}
	}
	
	/**
	 * add a new operation
	 * Silently replaces any existing operation with the same name
	 * @param op
	 */
	protected void addOperation(TurnoutOperation op) {
		TurnoutOperation previous;
		synchronized (this) {
			previous = (TurnoutOperation)turnoutOperations.put(op.getName(), op);
			if (op.isDefinitive()) {
				updateTypes(op);
			}
		}
		if (previous != null) {
			log.debug("replaced existing operation called "+previous.getName());
		}
	}
	
	protected void removeOperation(TurnoutOperation op) {
		synchronized (this) {
			turnoutOperations.remove(op.getName());
		}
	}
	
	/**
	 * find a TurnoutOperation by its name
	 * @param name
	 * @return	the operation
	 */
	public TurnoutOperation getOperation(String name) {
		synchronized (this) {
			return (TurnoutOperation)turnoutOperations.get(name);
		}
	}
	
	/**
	 * update the list of types to include a new or updated definitive instance.
	 * since order is important we retain the existing order, placing a new
	 * type at the end if necessary
	 * @param op	new or updated operation
	 */
	private void updateTypes(TurnoutOperation op) {
		LinkedList newTypes = new LinkedList();
		Iterator iter = operationTypes.iterator();
		boolean found = false;
		while (iter.hasNext()) {
			Object item = iter.next();
			if (item.getClass()==op.getClass()) {
				newTypes.add(op);
				found = true;
				log.debug("replacing definitive instance of "+item.getClass());
			} else {
				newTypes.add(item);
			}
		}
		if (!found) {
			newTypes.add(op);
			log.debug("adding definitive instance of "+op.getClass());
		}
		operationTypes = newTypes;
	}
	
	/**
	 * get the one-and-only instance of this class, if necessary creating it first.
	 * At creation also preload the known TurnoutOperator subclasses (done here
	 * to avoid constructor ordering problems).
	 * @return	the TurnoutOperationManager
	 */
	static public TurnoutOperationManager getInstance() {
		if (theInstance==null) {
			theInstance = new TurnoutOperationManager();
	        // now create the default instances of each of the known operation types
			theInstance.loadOperationTypes();
		}
		return theInstance;
	}
	
	/**
	 * load the operation types given by the current TurnoutManager instance, in
	 * the order given (important because the acceptable feedback modes may overlap).
	 * All we do is instantiate the classes. The constructors take care of putting
	 * everything in the right places. We allow multiple occurrences of the same
	 * name without complaining so the Proxy stuff works.
	 */
	public void loadOperationTypes() {
		String[] validTypes = InstanceManager.turnoutManagerInstance().getValidOperationTypes();
		for (int i=0; i<validTypes.length; ++i) {
			String thisClassName = "jmri."+validTypes[i]+"TurnoutOperation";
			if (getOperation(validTypes[i]) == null) {
				try {
					Class thisClass = Class.forName(thisClassName);
					TurnoutOperation to = (TurnoutOperation)thisClass.newInstance();
					if (log.isDebugEnabled()) { log.debug("loaded TurnoutOperation class "+thisClassName); };
				} catch (Exception e) { }
			}
		}
	}
	
	/**
	 * find a suitable operation for this turnout, based on its feedback type
	 * The mode is passed separately so the caller can transform it
	 * @param t	turnout
	 * @param apparentMode	mode(s) to be used when finding a matching operation
	 * @return
	 */
	public TurnoutOperation getMatchingOperation(AbstractTurnout t, int apparentMode) {
		if (doOperations) {
			Iterator iter = operationTypes.iterator();
			while (iter.hasNext()) {
				TurnoutOperation oper = (TurnoutOperation)iter.next();
				if (oper.matchFeedbackMode(apparentMode)) {
					return oper;
				}	
			}
		}
		return null;
	}
	/*
	 * get/change status of whether operations are in use
	 */
	
	public boolean getDoOperations() { return doOperations; };
	
	public void setDoOperations(boolean b) {
		boolean oldValue = doOperations;
		doOperations = b;
		firePropertyChange("doOperations", new Boolean(oldValue), new Boolean(b));
	}
	
	/**
	 * Proxy support. Take a concatenation of operation type lists from multiple
	 * systems and turn it into a single list, by eliminating duplicates and ensuring
	 * that NoFeedback - which matches anything - comes at the end if it is present
	 * at all.
	 * @param types	list of types possibly containing dupliactes
	 * @return list reduced as described above
	 */
	static public String[] concatenateTypeLists(String[] types) {
		List outTypes = new LinkedList();
		boolean noFeedbackWanted = false;
		for (int i=0; i<types.length; ++i) {
			if (types[i] == "NoFeedback") {
				noFeedbackWanted = true;
			} else if (!outTypes.contains(types[i])) {
				outTypes.add(types[i]);
			}
		}
		if (noFeedbackWanted) {
			outTypes.add("NoFeedback");
		}
		return (String[])outTypes.toArray(new String[0]);
	}
	
	/*
	 * Property change support
	 */
    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);
    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    protected void firePropertyChange(String p, Object old, Object n) { pcs.firePropertyChange(p,old,n);}

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(TurnoutOperationManager.class.getName());
}

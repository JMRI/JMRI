package jmri;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * class to look after the collection of TurnoutOperation subclasses Unlike the
 * other xxxManager, this does not inherit from AbstractManager since the
 * resources it deals with are not DCC system resources but rather purely
 * internal state.
 *
 * @author John Harper Copyright 2005
 *
 */
public class TurnoutOperationManager implements InstanceManagerAutoDefault {

    private final SortedMap<String, TurnoutOperation> turnoutOperations = new TreeMap<>();
    private List<TurnoutOperation> operationTypes = new LinkedList<>(); // array of the defining instances of each class, held in order of appearance
    boolean doOperations = false; // global on/off switch

    public TurnoutOperationManager() {
    }

    private boolean initialized = false;

    /** 
     * Does deferred initialization.
     * <p>
     * This is deferred because it invokes
     * loadOperationTypes, which gets the current turnout manager, often the
     * proxy manager, which in turn can invoke loadOperationTypes again. 
     */
    private void initialize() {
        if (!initialized) {
            initialized = true;
            // create the default instances of each of the known operation types
            loadOperationTypes();
        }
    }

    public void dispose() {
    }

    public TurnoutOperation[] getTurnoutOperations() {
        synchronized (this) {
            initialize();
            Collection<TurnoutOperation> entries = turnoutOperations.values();
            return entries.toArray(new TurnoutOperation[0]);
        }
    }

    /**
     * add a new operation Silently replaces any existing operation with the
     * same name
     *
     * @param op {@link TurnoutOperation} to add/replace
     */
    protected void addOperation(@Nonnull TurnoutOperation op) {
        Objects.requireNonNull(op, "TurnoutOperations cannot be null");
        TurnoutOperation previous;
        synchronized (this) {
            initialize();
            previous = turnoutOperations.put(op.getName(), op);
            if (op.isDefinitive()) {
                updateTypes(op);
            }
        }
        if (previous != null) {
            log.debug("replaced existing operation called " + previous.getName());
        }
        firePropertyChange("Content", null, null);
    }

    protected void removeOperation(@Nonnull TurnoutOperation op) {
        Objects.requireNonNull(op, "TurnoutOperations cannot be null");
        synchronized (this) {
            initialize();
            turnoutOperations.remove(op.getName());
        }
        firePropertyChange("Content", null, null);
    }

    /**
     * find a TurnoutOperation by its name
     *
     * @param name name of {@link TurnoutOperation} to retrieve
     * @return the operation
     */
    public TurnoutOperation getOperation(@Nonnull String name) {
        synchronized (this) {
            initialize();
            return turnoutOperations.get(name);
        }
    }

    /**
     * update the list of types to include a new or updated definitive instance.
     * since order is important we retain the existing order, placing a new type
     * at the end if necessary
     *
     * @param op new or updated operation
     */
    private void updateTypes(@Nonnull TurnoutOperation op) {
        initialize();
        LinkedList<TurnoutOperation> newTypes = new LinkedList<>();
        Iterator<TurnoutOperation> iter = operationTypes.iterator();
        boolean found = false;
        while (iter.hasNext()) {
            TurnoutOperation item = iter.next();
            if (item.getClass() == op.getClass()) {
                newTypes.add(op);
                found = true;
                log.debug("replacing definitive instance of " + item.getClass());
            } else {
                newTypes.add(item);
            }
        }
        if (!found) {
            newTypes.add(op);
            log.debug("adding definitive instance of " + op.getClass());
        }
        operationTypes = newTypes;
    }

    /**
     * Load the operation types given by the current TurnoutManager instance, in
     * the order given.
     * <p>
     * The order is important because the acceptable feedback modes may overlap.
     * All we do is instantiate the classes. The constructors take care of
     * putting everything in the right places. We allow multiple occurrences of
     * the same name without complaining so the Proxy stuff works.
     *
     * There's a threading problem here, because this invokes gets the current
     * turnout manager, often the proxy manager, which in turn invokes
     * loadOperationTypes again. This is bad. It's not clear why it even works.
     *
     */
    public void loadOperationTypes() {
        String[] validTypes = InstanceManager.turnoutManagerInstance().getValidOperationTypes();
        for (int i = 0; i < validTypes.length; ++i) {
            String thisClassName = "jmri." + validTypes[i] + "TurnoutOperation";
            if (validTypes[i] == null) {
                log.warn("null operation name in loadOperationTypes");
            } else if (getOperation(validTypes[i]) == null) {
                try {
                    Class<?> thisClass = Class.forName(thisClassName);
                    // creating the instance invokes the TurnoutOperation ctor,
                    // which calls addOperation here, which adds it to the 
                    // turnoutOperations map.
                    thisClass.getDeclaredConstructor().newInstance();
                    log.debug("loaded TurnoutOperation class {}", thisClassName);
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e1) {
                    log.error("during loadOperationTypes", e1);
                }
            }
        }
    }

    /**
     * Find a suitable operation for this turnout, based on its feedback type.
     * The mode is passed separately so the caller can transform it
     *
     * @param t            turnout
     * @param apparentMode mode(s) to be used when finding a matching operation
     * @return the turnout operation
     */
    public TurnoutOperation getMatchingOperationAlways(@Nonnull Turnout t, int apparentMode) {
        initialize();
        Iterator<TurnoutOperation> iter = operationTypes.iterator();
        TurnoutOperation currentMatch = null;
        /* The loop below always returns the LAST operation 
         that matches.  In the standard feedback modes, 
         This currently results in returning the NoFeedback 
         operation, since it is the last one added to 
         operationTypes */
        while (iter.hasNext()) {
            TurnoutOperation oper = iter.next();
            if (oper.matchFeedbackMode(apparentMode)) {
                currentMatch = oper;
            }
        }
        if (currentMatch != null) {
            return currentMatch;
        } else {
            return null;
        }
    }

    /**
     * find the correct operation for this turnout. If operations are globally
     * disabled, return nothing
     *
     * @param t            turnout
     * @param apparentMode mode(s) to be used when finding a matching operation
     * @return operation
     */
    public TurnoutOperation getMatchingOperation(@Nonnull Turnout t, int apparentMode) {
        initialize();
        if (doOperations) {
            return getMatchingOperationAlways(t, apparentMode);
        }
        return null;
    }

    public TurnoutOperation getMatchingOperationAlways(@Nonnull Turnout t) {
        return getMatchingOperationAlways(t, t.getFeedbackMode());
    }

    /*
     * get/change status of whether operations are in use
     */
    public boolean getDoOperations() {
        initialize();
        return doOperations;
    }

    public void setDoOperations(boolean b) {
        initialize();
        boolean oldValue = doOperations;
        doOperations = b;
        firePropertyChange("doOperations", oldValue, b);
    }

    /**
     * Proxy support. Take a concatenation of operation type lists from multiple
     * systems and turn it into a single list, by eliminating duplicates and
     * ensuring that NoFeedback - which matches anything - comes at the end if
     * it is present at all.
     *
     * @param types list of types possibly containing dupliactes
     * @return list reduced as described above
     */
    static public String[] concatenateTypeLists(@Nonnull String[] types) {
        List<String> outTypes = new LinkedList<>();
        boolean noFeedbackWanted = false;
        for (String type : types) {
            if ("NoFeedback".equals(type)) {
                noFeedbackWanted = true;
            } else if (type == null || type.isEmpty()) {
                log.warn("null or empty operation name returned from turnout manager");
            } else if (!outTypes.contains(type)) {
                outTypes.add(type);
            }
        }
        if (noFeedbackWanted) {
            outTypes.add("NoFeedback");
        }
        return outTypes.toArray(new String[0]);
    }

    /*
     * Property change support
     */
    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);

    public synchronized void addPropertyChangeListener(@Nonnull java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public synchronized void removePropertyChangeListener(@Nonnull java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    protected void firePropertyChange(@Nonnull String p, Object old, Object n) {
        pcs.firePropertyChange(p, old, n);
    }

    private final static Logger log = LoggerFactory.getLogger(TurnoutOperationManager.class);
}

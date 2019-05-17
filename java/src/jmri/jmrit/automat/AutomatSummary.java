package jmri.jmrit.automat;

import java.util.ArrayList;

/**
 * A singlet providing access to information about existing Automat instances.
 * <p>
 * It might not always be a singlet, however, so for now we're going through an
 * explicit instance() reference.
 * <p>
 * This can be invoked from various threads, so switches to the Swing thread to
 * notify its own listeners.
 *
 * @author Bob Jacobsen Copyright (C) 2004, 2007
 */
public class AutomatSummary {

    private AutomatSummary() {
    }

    static volatile private AutomatSummary self = null;

    static public AutomatSummary instance() {
        if (self == null) {
            self = new AutomatSummary();
        }
        return self;
    }

    private final ArrayList<AbstractAutomaton> automats = new ArrayList<>();

    java.beans.PropertyChangeSupport prop = new java.beans.PropertyChangeSupport(this);

    public void removePropertyChangeListener(java.beans.PropertyChangeListener p) {
        prop.removePropertyChangeListener(p);
    }

    public void addPropertyChangeListener(java.beans.PropertyChangeListener p) {
        prop.addPropertyChangeListener(p);
    }

    /**
     * A newly-created AbstractAutomaton instance uses this method to notify
     * interested parties of its existence.
     *
     * @param a the automaton to register
     */
    public void register(AbstractAutomaton a) {
        synchronized (automats) {
            automats.add(a);
        }

        //notify length changed
        notify("Insert", null, indexOf(a));

    }

    /**
     * Just before exiting, an AbstractAutomaton instance uses this method to
     * notify interested parties of its departure.
     *
     * @param a the automaton to remove
     */
    public void remove(AbstractAutomaton a) {
        int index = indexOf(a);

        synchronized (automats) {
            automats.remove(a);
        }

        //notify length changed
        notify("Remove", null, index);
    }

    public int length() {
        int length;
        synchronized (automats) {
            length = automats.size();
        }
        return length;  // debugging value
    }

    public AbstractAutomaton get(int i) {
        AbstractAutomaton retval;

        synchronized (automats) {
            retval = automats.get(i);
        }

        return retval;
    }

    /**
     * Provide a convenience method to look up a managed object by its name.
     *
     * @since 1.7.3
     * @param name Name of the automat to be located
     * @return null if name not found
     */
    public AbstractAutomaton get(String name) {
        AbstractAutomaton a;
        synchronized (automats) {
            for (int i = 0; i < length(); i++) {
                a = automats.get(i);
                if (a.getName().equals(name)) {
                    return a;
                }
            }
        }
        return null;
    }

    public int indexOf(AbstractAutomaton a) {
        int retval;
        synchronized (automats) {
            retval = automats.indexOf(a);
        }
        return retval;
    }

    /**
     * An AbstractAutomaton instance uses this method to notify interested
     * parties that it's gone around its handle loop again.
     *
     * @param a the looping automaton
     */
    public void loop(AbstractAutomaton a) {
        int i;
        synchronized (automats) {
            i = automats.indexOf(a);
        }
        notify("Count", null, i);
    }

    void notify(String property, Object arg1, Object arg2) {
        Runnable r = new Notifier(property, arg1, arg2);
        javax.swing.SwingUtilities.invokeLater(r);
    }

    class Notifier implements Runnable {

        Notifier(String property, Object arg1, Object arg2) {
            this.property = property;
            this.arg1 = arg1;
            this.arg2 = arg2;
        }
        Object arg1;
        Object arg2;
        String property;

        @Override
        public void run() {
            prop.firePropertyChange(property, arg1, arg2);
        }

    }
}

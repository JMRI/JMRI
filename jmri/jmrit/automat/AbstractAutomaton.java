// AbstractAutomaton.java

package jmri.jmrit.automat;

/**
 * Abstract base for user automaton classes, which provide
 * individual bits of automation.
 * <P>
 * Each individual automaton runs in a separate thread, so they
 * can operate independently.  This class handles thread
 * creation and scheduling, and provides a number of services
 * for the user code.
 * <P>
 * Subclasses provide a "handle()" function, which does the needed
 * work.  It can use any JMRI resources for input and output.  It should
 * not spin on a condition without explicit wait requests; it's more efficient
 * to use the explicit wait services if it's waiting for some specific
 * condition.
 * <P>
 * handle() is executed repeatedly until either the Automate object is
 * halted(), or it returns "false".  Returning "true" will just cause
 * handle() to be invoked again, so you can cleanly restart the Automaton
 * by returning from multiple points in the function.
 * <P>
 * Since handle() executes outside the GUI thread, it's important that
 * access to GUI (AWT, Swing) objects be scheduled through the
 * various service routines.
 * <P>
 * Services are provided by public member functions, described below.
 *
 * @author	Bob Jacobsen    Copyright (C) 2003
 * @version     $Revision: 1.1 $
 */
abstract public class AbstractAutomaton implements Runnable {

    public AbstractAutomaton() {}

    public void start() {
        Thread t = new Thread(this);
        t.start();
    }

    public void run() {
        while (handle()) {}
    }

    /**
     *
     * @return false to terminate the automaton, for example due to an error.
     */
    abstract public boolean handle();

    /**
     * Wait, in a simpler form.  This handles exceptions internally,
     * so they needn't clutter up the code.  Note that the current
     * implementation doesn't guarantee the time, either high or low.
     * @param milliseconds
     */
    public synchronized void wait(int milliseconds){
        try {
            super.wait(milliseconds);
        } catch (InterruptedException e) {
            // do nothing for now
        }
    }

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(AbstractAutomaton.class.getName());

}


/* @(#)AbstractAutomaton.java */

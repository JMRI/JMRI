// AbstractAutomaton.java

package jmri.jmrit.automat;

import jmri.*;

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
 * work, and optionally a "init()" function.
 * These can use any JMRI resources for input and output.  It should
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
 * They must only be invoked from the init and handle methods, as they
 * must be used in a delayable thread.  If invoked from the GUI thread,
 * for example, the program will appear to hang. To help ensure this,
 * a warning will be logged if they are used before the thread starts.
 *
 * @author	Bob Jacobsen    Copyright (C) 2003
 * @version     $Revision: 1.2 $
 */
abstract public class AbstractAutomaton implements Runnable {

    public AbstractAutomaton() {}

    public void start() {
        Thread t = new Thread(this);
        t.start();
    }

    public void run() {
        inThread = true;
        init();
        while (handle()) {}
    }

    /**
     * User-provided initialization routine
     */
    abstract public void init();

    /**
     * User-provided main routine. This is run repeatedly until
     * it signals the end by returning false.  Many automata
     * are intended to run forever, and will always return true.
     *
     * @return false to terminate the automaton, for example due to an error.
     */
    abstract public boolean handle();

    /**
     * Wait for an interval, in a simple form.
     * <P>
     * This handles exceptions internally,
     * so they needn't clutter up the code.  Note that the current
     * implementation doesn't guarantee the time, either high or low.
     * @param milliseconds
     */
    public synchronized void wait(int milliseconds){
        if (!inThread) log.warn("wait invoked from invalid context");
        try {
            super.wait(milliseconds);
        } catch (InterruptedException e) {
            // do nothing for now
        }
    }

    /**
     * Flag used to ensure that service routines
     * are only invoked in the automaton thread.
     */
    private boolean inThread = false;

    private AbstractAutomaton self = this;

    /**
     * Wait for a sensor to change state.
     * <P>
     * The current (OK) state of the Sensor is passed to avoid
     * a possible race condition. The new state is returned
     * for a similar reason.
     * <P>
     * This works by registering a listener, which is likely to
     * run in another thread.  That listener then interrupts the automaton's
     * thread, who confirms the change.
     *
     * @param mState Current state of the sensor
     * @param mSensor Sensor to watch
     * @return newly detected Sensor state
     */
    public synchronized int waitSensorChange(int mState, Sensor mSensor){
        if (!inThread) log.warn("waitSensorChange invoked from invalid context");
        if (log.isDebugEnabled()) log.debug("waitSensorChange starts: "+mSensor.getID());
        // register a listener
        java.beans.PropertyChangeListener l;
        mSensor.addPropertyChangeListener(l = new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                synchronized (self) {
                    self.notify();
                }
            }
        });

        int now;
        while (mState == (now = mSensor.getKnownState())) {
            try {
                super.wait(10000);
            } catch (InterruptedException e) {
                log.warn("waitSensorChange interrupted; this is unexpected");
            }
        }

        // remove the listener & report new state
        mSensor.removePropertyChangeListener(l);

        return now;
    }

    private DccThrottle throttle;
    public DccThrottle getThrottle(int address, boolean longAddress) {
        if (!inThread) log.warn("getThrottle invoked from invalid context");
        throttle = null;
        InstanceManager.throttleManagerInstance()
                .requestThrottle(address,new ThrottleListener() {
                    public void notifyThrottleFound(DccThrottle t) {
                        throttle = t;
                        synchronized (self) {
                            self.notify();
                        }
                    }
                });
        // now wait for reply from identified throttle
        while (throttle == null) {
            log.debug("waiting for throttle");
            wait(10000);
            if (throttle == null) log.warn("Still waiting for throttle!");
        }
        return throttle;
    }

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(AbstractAutomaton.class.getName());

}


/* @(#)AbstractAutomaton.java */

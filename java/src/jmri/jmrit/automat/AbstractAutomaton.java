package jmri.jmrit.automat;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.*;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import jmri.BasicRosterEntry;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.Programmer;
import jmri.ProgrammerException;
import jmri.Sensor;
import jmri.ThrottleListener;
import jmri.ThrottleManager;
import jmri.Turnout;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.Warrant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base for user automaton classes, which provide individual bits of
 * automation.
 * <p>
 * Each individual automaton runs in a separate thread, so they can operate
 * independently. This class handles thread creation and scheduling, and
 * provides a number of services for the user code.
 * <p>
 * Subclasses provide a "handle()" function, which does the needed work, and
 * optionally a "init()" function. These can use any JMRI resources for input
 * and output. It should not spin on a condition without explicit wait requests;
 * it is more efficient to use the explicit wait services when waiting for some
 * specific condition.
 * <p>
 * handle() is executed repeatedly until either the Automate object is halted(),
 * or it returns "false". Returning "true" will just cause handle() to be
 * invoked again, so you can cleanly restart the Automaton by returning from
 * multiple points in the function.
 * <p>
 * Since handle() executes outside the GUI thread, it is important that access
 * to GUI (AWT, Swing) objects be scheduled through the various service
 * routines.
 * <p>
 * Services are provided by public member functions, described below. They must
 * only be invoked from the init and handle methods, as they must be used in a
 * delayable thread. If invoked from the GUI thread, for example, the program
 * will appear to hang. To help ensure this, a warning will be logged if they
 * are used before the thread starts.
 * <p>
 * For general use, e.g. in scripts, the most useful functions are:
 * <ul>
 * <li>Wait for a specific number of milliseconds: {@link #waitMsec(int)}
 * <li>Wait for a specific sensor to be active:
 * {@link #waitSensorActive(jmri.Sensor)} This is also available in a form that
 * will wait for any of a group of sensors to be active.
 * <li>Wait for a specific sensor to be inactive:
 * {@link #waitSensorInactive(jmri.Sensor)} This is also available in a form
 * that will wait for any of a group of sensors to be inactive.
 * <li>Wait for a specific sensor to be in a specific state:
 * {@link #waitSensorState(jmri.Sensor, int)}
 * <li>Wait for a specific sensor to change:
 * {@link #waitSensorChange(int, jmri.Sensor)}
 * <li>Wait for a specific warrant to change run state:
 * {@link #waitWarrantRunState(Warrant, int)}
 * <li>Wait for a specific warrant to enter or leave a specific block:
 * {@link #waitWarrantBlock(Warrant, String, boolean)}
 * <li>Wait for a specific warrant to enter the next block or to stop:
 * {@link #waitWarrantBlockChange(Warrant)}
 * <li>Set a group of turnouts and wait for them to be consistent (actual
 * position matches desired position):
 * {@link #setTurnouts(jmri.Turnout[], jmri.Turnout[])}
 * <li>Wait for a group of turnouts to be consistent (actually as set):
 * {@link #waitTurnoutConsistent(jmri.Turnout[])}
 * <li>Wait for any one of a number of Sensors, Turnouts and/or other objects to
 * change: {@link #waitChange(jmri.NamedBean[])}
 * <li>Wait for any one of a number of Sensors, Turnouts and/or other objects to
 * change, up to a specified time: {@link #waitChange(jmri.NamedBean[], int)}
 * <li>Obtain a DCC throttle: {@link #getThrottle}
 * <li>Read a CV from decoder on programming track: {@link #readServiceModeCV}
 * <li>Write a value to a CV in a decoder on the programming track:
 * {@link #writeServiceModeCV}
 * <li>Write a value to a CV in a decoder on the main track:
 * {@link #writeOpsModeCV}
 * </ul>
 * <p>
 * Although this is named an "Abstract" class, it's actually concrete so scripts
 * can easily use some of the methods.
 *
 * @author Bob Jacobsen Copyright (C) 2003
 */
public class AbstractAutomaton implements Runnable {

    public AbstractAutomaton() {
        String className = this.getClass().getName();
        int lastdot = className.lastIndexOf(".");
        setName(className.substring(lastdot + 1, className.length()));
    }

    public AbstractAutomaton(String name) {
        setName(name);
    }

    AutomatSummary summary = AutomatSummary.instance();

    Thread currentThread = null;

    /**
     * Start this automat processing.
     * <p>
     * Overrides the superclass method to do local accounting.
     */
    public void start() {
        if (currentThread != null) {
            log.error("Start with currentThread not null!");
        }
        currentThread = new Thread(this, name);
        currentThread.start();
        summary.register(this);
        count = 0;
    }

    private boolean running = false;

    public boolean isRunning() {
        return running;
    }

    /**
     * Part of the implementation; not for general use.
     * <p>
     * This is invoked on currentThread.
     */
    @Override
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "IMSE_DONT_CATCH_IMSE",
            justification = "get these when stop() issued against thread doing BlockingQueue.take() in waitChange, should remove when stop() reimplemented")
    public void run() {
        try {
            inThread = true;
            init();
            // the real processing in the next statement is in handle();
            // and the loop call is just doing accounting
            running = true;
            while (handle()) {
                count++;
                summary.loop(this);
            }
            log.debug("normal termination, handle() returned false");
            currentThread = null;
            done();
        } catch (ThreadDeath e1) {
            if (currentThread == null) {
                log.debug("Received ThreadDeath, likely due to stop()");
            } else {
                log.warn("Received ThreadDeath while not stopped", e1);
            }
        } catch (IllegalMonitorStateException e2) {
            if (currentThread == null) {
                log.debug("Received IllegalMonitorStateException, likely due to stop()");
            } else {
                log.warn("Received IllegalMonitorStateException while not stopped", e2);
            }
        } catch (Exception e3) {
            log.warn("Unexpected Exception ends AbstractAutomaton thread", e3);
        } finally {
            currentThread = null;
            done();
        }
        running = false;
    }

    /**
     * Stop the thread immediately.
     * <p>
     * Overrides superclass method to handle local accounting.
     */
    @SuppressWarnings("deprecation") // AbstractAutomaton objects can be waiting on _lots_ of things, so
                                     // we need to find another way to deal with this besides Interrupt
    public void stop() {
        log.trace("stop() invoked");
        if (currentThread == null) {
            log.error("Stop with currentThread null!");
            return;
        }

        Thread stoppingThread = currentThread;
        currentThread = null;

        try {
            stoppingThread.stop();
        } catch (java.lang.ThreadDeath e) {
            log.error("Exception while in stop(): {}", e.toString());
        }

        done();
        // note we don't set running = false here.  It's still running until the run() routine thinks it's not.
        log.trace("stop() completed");
    }

    /**
     * Part of the internal implementation; not for general use.
     * <p>
     * Common internal end-time processing
     */
    void done() {
        summary.remove(this);
    }

    private String name = null;

    private int count;

    /**
     * Get the number of times the handle routine has executed.
     * <p>
     * Used by classes such as {@link jmri.jmrit.automat.monitor} to monitor
     * progress.
     *
     * @return the number of times {@link #handle()} has been called on this
     *         AbstractAutomation
     */
    public int getCount() {
        return count;
    }

    /**
     * Get the thread name. Used by classes monitoring this AbstractAutomation,
     * such as {@link jmri.jmrit.automat.monitor}.
     *
     * @return the name of this thread
     */
    public String getName() {
        return name;
    }

    /**
     * Update the name of this object.
     * <p>
     * name is not a bound parameter, so changes are not notified to listeners.
     *
     * @param name the new name
     * @see #getName()
     */
    public void setName(String name) {
        this.name = name;
    }

    void defaultName() {
    }

    /**
     * User-provided initialization routine.
     * <p>
     * This is called exactly once for each object created. This is where you
     * put all the code that needs to be run when your object starts up: Finding
     * sensors and turnouts, getting a throttle, etc.
     */
    protected void init() {
    }

    /**
     * User-provided main routine.
     * <p>
     * This is run repeatedly until it signals the end by returning false. Many
     * automata are intended to run forever, and will always return true.
     *
     * @return false to terminate the automaton, for example due to an error.
     */
    protected boolean handle() {
        return false;
    }

    /**
     * Control optional debugging prompt. If this is set true, each call to
     * wait() will prompt the user whether to continue.
     */
    protected boolean promptOnWait = false;

    /**
     * Wait for a specified time and then return control.
     *
     * @param milliseconds the number of milliseconds to wait
     */
    public void waitMsec(int milliseconds) {
        long target = System.currentTimeMillis() + milliseconds;
        while (true) {
            long stillToGo = target - System.currentTimeMillis();
            if (stillToGo <= 0) {
                break;
            }
            try {
                Thread.sleep(stillToGo);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // retain if needed later
            }
        }
    }

    private boolean waiting = false;

    /**
     * Indicates that object is waiting on a waitSomething call.
     * <p>
     * Specifically, the wait has progressed far enough that any change to the
     * waited-on-condition will be detected.
     *
     * @return true if waiting; false otherwise
     */
    public boolean isWaiting() {
        return waiting;
    }

    /**
     * Internal common routine to handle start-of-wait bookkeeping.
     */
    final private void startWait() {
        waiting = true;
    }

    /**
     * Internal common routine to handle end-of-wait bookkeeping.
     */
    final private void endWait() {
        if (promptOnWait) {
            debuggingWait();
        }
        waiting = false;
    }

    /**
     * Part of the internal implementation, not intended for users.
     * <p>
     * This handles exceptions internally, so they needn't clutter up the code.
     * Note that the current implementation doesn't guarantee the time, either
     * high or low.
     * <p>
     * Because of the way Jython access handles synchronization, this is
     * explicitly synchronized internally.
     *
     * @param milliseconds the number of milliseconds to wait
     */
    protected void wait(int milliseconds) {
        startWait();
        synchronized (this) {
            try {
                if (milliseconds < 0) {
                    super.wait();
                } else {
                    super.wait(milliseconds);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // retain if needed later
                log.warn("interrupted in wait");
            }
        }
        endWait();
    }

    /**
     * Flag used to ensure that service routines are only invoked in the
     * automaton thread.
     */
    private boolean inThread = false;

    private final AbstractAutomaton self = this;

    /**
     * Wait for a sensor to change state.
     * <p>
     * The current (OK) state of the Sensor is passed to avoid a possible race
     * condition. The new state is returned for a similar reason.
     * <p>
     * This works by registering a listener, which is likely to run in another
     * thread. That listener then interrupts the automaton's thread, who
     * confirms the change.
     *
     * @param mState  Current state of the sensor
     * @param mSensor Sensor to watch
     * @return newly detected Sensor state
     */
    public int waitSensorChange(int mState, Sensor mSensor) {
        if (!inThread) {
            log.warn("waitSensorChange invoked from invalid context");
        }
        if (log.isDebugEnabled()) {
            log.debug("waitSensorChange starts: " + mSensor.getSystemName());
        }
        // register a listener
        PropertyChangeListener l;
        mSensor.addPropertyChangeListener(l = (PropertyChangeEvent e) -> {
            synchronized (self) {
                self.notifyAll(); // should be only one thread waiting, but just in case
            }
        });

        int now;
        while (mState == (now = mSensor.getKnownState())) {
            wait(-1);
        }

        // remove the listener & report new state
        mSensor.removePropertyChangeListener(l);

        return now;
    }

    /**
     * Wait for a sensor to be active. (Returns immediately if already active)
     *
     * @param mSensor Sensor to watch
     */
    public void waitSensorActive(Sensor mSensor) {
        if (log.isDebugEnabled()) {
            log.debug("waitSensorActive starts");
        }
        waitSensorState(mSensor, Sensor.ACTIVE);
    }

    /**
     * Wait for a sensor to be inactive. (Returns immediately if already
     * inactive)
     *
     * @param mSensor Sensor to watch
     */
    public void waitSensorInactive(Sensor mSensor) {
        if (log.isDebugEnabled()) {
            log.debug("waitSensorInActive starts");
        }
        waitSensorState(mSensor, Sensor.INACTIVE);
    }

    /**
     * Internal service routine to wait for one sensor to be in (or become in) a
     * specific state.
     * <p>
     * Used by waitSensorActive and waitSensorInactive
     * <p>
     * This works by registering a listener, which is likely to run in another
     * thread. That listener then interrupts this thread to confirm the change.
     *
     * @param mSensor the sensor to wait for
     * @param state   the expected state
     */
    public synchronized void waitSensorState(Sensor mSensor, int state) {
        if (!inThread) {
            log.warn("waitSensorState invoked from invalid context");
        }
        if (mSensor.getKnownState() == state) {
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("waitSensorState starts: " + mSensor.getSystemName() + " " + state);
        }
        // register a listener
        PropertyChangeListener l;
        mSensor.addPropertyChangeListener(l = (PropertyChangeEvent e) -> {
            synchronized (self) {
                self.notifyAll(); // should be only one thread waiting, but just in case
            }
        });

        while (state != mSensor.getKnownState()) {
            wait(-1);  // wait for notification
        }

        // remove the listener & report new state
        mSensor.removePropertyChangeListener(l);

    }

    /**
     * Wait for one of a list of sensors to be be inactive.
     *
     * @param mSensors sensors to wait on
     */
    public void waitSensorInactive(@Nonnull Sensor[] mSensors) {
        log.debug("waitSensorInactive[] starts");
        waitSensorState(mSensors, Sensor.INACTIVE);
    }

    /**
     * Wait for one of a list of sensors to be be active.
     *
     * @param mSensors sensors to wait on
     */
    public void waitSensorActive(@Nonnull Sensor[] mSensors) {
        log.debug("waitSensorActive[] starts");
        waitSensorState(mSensors, Sensor.ACTIVE);
    }

    /**
     * Wait for one of a list of sensors to be be in a selected state.
     * <p>
     * This works by registering a listener, which is likely to run in another
     * thread. That listener then interrupts the automaton's thread, who
     * confirms the change.
     *
     * @param mSensors Array of sensors to watch
     * @param state    State to check (static value from jmri.Sensors)
     */
    public synchronized void waitSensorState(@Nonnull Sensor[] mSensors, int state) {
        if (!inThread) {
            log.warn("waitSensorState invoked from invalid context");
        }
        log.debug("waitSensorState[] starts");

        // do a quick check first, just in case
        if (checkForState(mSensors, state)) {
            log.debug("returns immediately");
            return;
        }
        // register listeners
        int i;
        PropertyChangeListener[] listeners
                = new PropertyChangeListener[mSensors.length];
        for (i = 0; i < mSensors.length; i++) {

            mSensors[i].addPropertyChangeListener(listeners[i] = (PropertyChangeEvent e) -> {
                synchronized (self) {
                    log.trace("notify waitSensorState[] of property change");
                    self.notifyAll(); // should be only one thread waiting, but just in case
                }
            });

        }

        while (!checkForState(mSensors, state)) {
            wait(-1);
        }

        // remove the listeners
        for (i = 0; i < mSensors.length; i++) {
            mSensors[i].removePropertyChangeListener(listeners[i]);
        }

    }

    /**
     * Wait for a warrant to change into or out of running state.
     * <p>
     * This works by registering a listener, which is likely to run in another
     * thread. That listener then interrupts the automaton's thread, who
     * confirms the change.
     *
     * @param warrant The name of the warrant to watch
     * @param state   State to check (static value from jmri.logix.warrant)
     */
    public synchronized void waitWarrantRunState(@Nonnull Warrant warrant, int state) {
        if (!inThread) {
            log.warn("waitWarrantRunState invoked from invalid context");
        }
        if (log.isDebugEnabled()) {
            log.debug("waitWarrantRunState " + warrant.getDisplayName() + ", " + state + " starts");
        }

        // do a quick check first, just in case
        if (warrant.getRunMode() == state) {
            log.debug("waitWarrantRunState returns immediately");
            return;
        }
        // register listener
        PropertyChangeListener listener;
        warrant.addPropertyChangeListener(listener = (PropertyChangeEvent e) -> {
            synchronized (self) {
                log.trace("notify waitWarrantRunState of property change");
                self.notifyAll(); // should be only one thread waiting, but just in case
            }
        });

        while (warrant.getRunMode() != state) {
            wait(-1);
        }

        // remove the listener
        warrant.removePropertyChangeListener(listener);

    }

    /**
     * Wait for a warrant to enter a named block.
     * <p>
     * This works by registering a listener, which is likely to run in another
     * thread. That listener then interrupts this thread to confirm the change.
     *
     * @param warrant  The name of the warrant to watch
     * @param block    block to check
     * @param occupied Determines whether to wait for the block to become
     *                 occupied or unoccupied
     */
    public synchronized void waitWarrantBlock(@Nonnull Warrant warrant, @Nonnull String block, boolean occupied) {
        if (!inThread) {
            log.warn("waitWarrantBlock invoked from invalid context");
        }
        if (log.isDebugEnabled()) {
            log.debug("waitWarrantBlock " + warrant.getDisplayName() + ", " + block + " " + occupied + " starts");
        }

        // do a quick check first, just in case
        if (warrant.getCurrentBlockName().equals(block) == occupied) {
            log.debug("waitWarrantBlock returns immediately");
            return;
        }
        // register listener
        PropertyChangeListener listener;
        warrant.addPropertyChangeListener(listener = (PropertyChangeEvent e) -> {
            synchronized (self) {
                log.trace("notify waitWarrantBlock of property change");
                self.notifyAll(); // should be only one thread waiting, but just in case
            }
        });

        while (warrant.getCurrentBlockName().equals(block) != occupied) {
            wait(-1);
        }

        // remove the listener
        warrant.removePropertyChangeListener(listener);

    }

    private volatile boolean blockChanged = false;
    private volatile String blockName = null;

    /**
     * Wait for a warrant to either enter a new block or to stop running.
     * <p>
     * This works by registering a listener, which is likely to run in another
     * thread. That listener then interrupts the automaton's thread, who
     * confirms the change.
     *
     * @param warrant The name of the warrant to watch
     *
     * @return The name of the block that was entered or null if the warrant is
     *         no longer running.
     */
    public synchronized String waitWarrantBlockChange(@Nonnull Warrant warrant) {
        if (!inThread) {
            log.warn("waitWarrantBlockChange invoked from invalid context");
        }
        if (log.isDebugEnabled()) {
            log.debug("waitWarrantBlockChange " + warrant.getDisplayName());
        }

        // do a quick check first, just in case
        if (warrant.getRunMode() != Warrant.MODE_RUN) {
            log.debug("waitWarrantBlockChange returns immediately");
            return null;
        }
        // register listeners
        blockName = null;
        blockChanged = false;

        PropertyChangeListener listener;
        warrant.addPropertyChangeListener(listener = (PropertyChangeEvent e) -> {
            if (e.getPropertyName().equals("blockChange")) {
                blockChanged = true;
                blockName = ((OBlock) e.getNewValue()).getDisplayName();
            }
            if (e.getPropertyName().equals("runMode") && !Integer.valueOf(Warrant.MODE_RUN).equals(e.getNewValue())) {
                blockName = null;
                blockChanged = true;
            }
            synchronized (self) {
                log.trace("notify waitWarrantBlockChange of property change");
                self.notifyAll(); // should be only one thread waiting, but just in case
            }
        });

        while (!blockChanged) {
            wait(-1);
        }

        // remove the listener
        warrant.removePropertyChangeListener(listener);

        return blockName;
    }

    /**
     * Wait for a list of turnouts to all be in a consistent state
     * <p>
     * This works by registering a listener, which is likely to run in another
     * thread. That listener then interrupts the automaton's thread, who
     * confirms the change.
     *
     * @param mTurnouts list of turnouts to watch
     */
    public synchronized void waitTurnoutConsistent(@Nonnull Turnout[] mTurnouts) {
        if (!inThread) {
            log.warn("waitTurnoutConsistent invoked from invalid context");
        }
        if (log.isDebugEnabled()) {
            log.debug("waitTurnoutConsistent[] starts");
        }

        // do a quick check first, just in case
        if (checkForConsistent(mTurnouts)) {
            log.debug("returns immediately");
            return;
        }
        // register listeners
        int i;
        PropertyChangeListener[] listeners
                = new PropertyChangeListener[mTurnouts.length];
        for (i = 0; i < mTurnouts.length; i++) {

            mTurnouts[i].addPropertyChangeListener(listeners[i] = (PropertyChangeEvent e) -> {
                synchronized (self) {
                    log.trace("notify waitTurnoutConsistent[] of property change");
                    self.notifyAll(); // should be only one thread waiting, but just in case
                }
            });

        }

        while (!checkForConsistent(mTurnouts)) {
            wait(-1);
        }

        // remove the listeners
        for (i = 0; i < mTurnouts.length; i++) {
            mTurnouts[i].removePropertyChangeListener(listeners[i]);
        }

    }

    /**
     * Convenience function to set a bunch of turnouts and wait until they are
     * all in a consistent state
     *
     * @param closed turnouts to set to closed state
     * @param thrown turnouts to set to thrown state
     */
    public void setTurnouts(@Nonnull Turnout[] closed, @Nonnull Turnout[] thrown) {
        Turnout[] turnouts = new Turnout[closed.length + thrown.length];
        int ti = 0;
        for (int i = 0; i < closed.length; ++i) {
            turnouts[ti++] = closed[i];
            closed[i].setCommandedState(Turnout.CLOSED);
        }
        for (int i = 0; i < thrown.length; ++i) {
            turnouts[ti++] = thrown[i];
            thrown[i].setCommandedState(Turnout.THROWN);
        }
        waitTurnoutConsistent(turnouts);
    }

    /**
     * Wait, up to a specified time, for one of a list of NamedBeans (sensors,
     * signal heads and/or turnouts) to change their state.
     * <p>
     * Registers a listener on each of the NamedBeans listed. The listener is
     * likely to run in another thread. Each fired listener then queues a check
     * to the automaton's thread.
     *
     * @param mInputs  Array of NamedBeans to watch
     * @param maxDelay maximum amount of time (milliseconds) to wait before
     *                 continuing anyway. -1 means forever
     */
    public void waitChange(@Nonnull NamedBean[] mInputs, int maxDelay) {
        if (!inThread) {
            log.warn("waitChange invoked from invalid context");
        }

        int i;
        int[] tempState = waitChangePrecheckStates;
        // do we need to create it now?
        boolean recreate = false;
        if (waitChangePrecheckBeans != null && waitChangePrecheckStates != null) {
            // Seems precheck intended, see if done right
            if (waitChangePrecheckBeans.length != mInputs.length) {
                log.warn("Precheck ignored because of mismatch in size: before {}, now {}", waitChangePrecheckBeans.length, mInputs.length);
                recreate = true;
            }
            if (waitChangePrecheckBeans.length != waitChangePrecheckStates.length) {
                log.error("Precheck data inconsistent because of mismatch in size: {}, {}", waitChangePrecheckBeans.length, waitChangePrecheckStates.length);
                recreate = true;
            }
            if (!recreate) { // have to check if the beans are the same, but only check if the above tests pass
                for (i = 0; i < mInputs.length; i++) {
                    if (waitChangePrecheckBeans[i] != mInputs[i]) {
                        log.warn("Precheck ignored because of mismatch in bean {}", i);
                        recreate = true;
                        break;
                    }
                }
            }
        } else {
            recreate = true;
        }

        if (recreate) {
            // here, have to create a new state array
            log.trace("recreate state array");
            tempState = new int[mInputs.length];
            for (i = 0; i < mInputs.length; i++) {
                tempState[i] = mInputs[i].getState();
            }
        }
        waitChangePrecheckBeans = null;
        waitChangePrecheckStates = null;
        final int[] initialState = tempState; // needs to be final for off-thread references

        log.debug("waitChange[] starts for {} listeners", mInputs.length);
        waitChangeQueue.clear();

        // register listeners
        PropertyChangeListener[] listeners = new PropertyChangeListener[mInputs.length];
        for (i = 0; i < mInputs.length; i++) {
            mInputs[i].addPropertyChangeListener(listeners[i] = (PropertyChangeEvent e) -> {
                if (!waitChangeQueue.offer(e)) {
                    log.warn("Waiting changes capacity exceeded; not adding {} to queue", e);
                }
            });

        }

        log.trace("waitChange[] listeners registered");

        // queue a check for whether there was a change while registering
        jmri.util.ThreadingUtil.runOnLayoutEventually(() -> {
            log.trace("start separate waitChange check");
            for (int j = 0; j < mInputs.length; j++) {
                if (initialState[j] != mInputs[j].getState()) {
                    log.trace("notify that input {} changed when initial on-layout check was finally done", j);
                    PropertyChangeEvent e = new PropertyChangeEvent(mInputs[j], "State", initialState[j], mInputs[j].getState());
                    if (!waitChangeQueue.offer(e)) {
                        log.warn("Waiting changes capacity exceeded; not adding {} to queue", e);
                    }
                    break;
                }
            }
            log.trace("end separate waitChange check");
        });

        // wait for notify from a listener
        startWait();

        PropertyChangeEvent prompt;
        try {
            if (maxDelay < 0) {
                prompt = waitChangeQueue.take();
            } else {
                prompt = waitChangeQueue.poll(maxDelay, TimeUnit.MILLISECONDS);
            }
            if (prompt != null) {
                log.trace("waitChange continues from {}", prompt.getSource());
            } else {
                log.trace("waitChange continues");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // retain if needed later
            log.warn("AbstractAutomaton {} waitChange interrupted", getName());
        }

        // remove the listeners
        for (i = 0; i < mInputs.length; i++) {
            mInputs[i].removePropertyChangeListener(listeners[i]);
        }
        log.trace("waitChange[] listeners removed");
        endWait();
    }

    NamedBean[] waitChangePrecheckBeans = null;
    int[] waitChangePrecheckStates = null;
    BlockingQueue<PropertyChangeEvent> waitChangeQueue = new LinkedBlockingQueue<PropertyChangeEvent>();

    /**
     * Wait forever for one of a list of NamedBeans (sensors, signal heads
     * and/or turnouts) to change, or for a specific time to pass.
     *
     * @param mInputs Array of NamedBeans to watch
     */
    public void waitChangePrecheck(NamedBean[] mInputs) {
        waitChangePrecheckBeans = new NamedBean[mInputs.length];
        waitChangePrecheckStates = new int[mInputs.length];
        for (int i = 0; i < mInputs.length; i++) {
            waitChangePrecheckBeans[i] = mInputs[i];
            waitChangePrecheckStates[i] = mInputs[i].getState();
        }
    }

    /**
     * Wait forever for one of a list of NamedBeans (sensors, signal heads
     * and/or turnouts) to change, or for a specific time to pass.
     *
     * @param mInputs Array of NamedBeans to watch
     */
    public void waitChange(NamedBean[] mInputs) {
        waitChange(mInputs, -1);
    }

    /**
     * Wait for one of an array of sensors to change.
     * <p>
     * This is an older method, now superceded by waitChange, which can wait for
     * any NamedBean.
     *
     * @param mSensors Array of sensors to watch
     */
    public void waitSensorChange(Sensor[] mSensors) {
        waitChange(mSensors);
    }

    /**
     * Check an array of sensors to see if any are in a specific state
     *
     * @param mSensors Array to check
     * @return true if any are ACTIVE
     */
    private boolean checkForState(Sensor[] mSensors, int state) {
        for (Sensor mSensor : mSensors) {
            if (mSensor.getKnownState() == state) {
                return true;
            }
        }
        return false;
    }

    private boolean checkForConsistent(Turnout[] mTurnouts) {
        for (int i = 0; i < mTurnouts.length; ++i) {
            if (!mTurnouts[i].isConsistentState()) {
                return false;
            }
        }
        return true;
    }

    private DccThrottle throttle;
    private boolean failedThrottleRequest = false;

    /**
     * Obtains a DCC throttle, including waiting for the command station
     * response.
     *
     * @param address     Numeric address value
     * @param longAddress true if this is a long address, false for a short
     *                    address
     * @param waitSecs    number of seconds to wait for throttle to acquire
     *                    before returning null
     * @return A usable throttle, or null if error
     */
    public DccThrottle getThrottle(int address, boolean longAddress, int waitSecs) {
        log.debug("requesting DccThrottle for addr " + address);
        if (!inThread) {
            log.warn("getThrottle invoked from invalid context");
        }
        throttle = null;
        ThrottleListener throttleListener = new ThrottleListener() {
            @Override
            public void notifyThrottleFound(DccThrottle t) {
                throttle = t;
                synchronized (self) {
                    self.notifyAll(); // should be only one thread waiting, but just in case
                }
            }

            @Override
            public void notifyFailedThrottleRequest(jmri.LocoAddress address, String reason) {
                log.error("Throttle request failed for " + address + " because " + reason);
                failedThrottleRequest = true;
                synchronized (self) {
                    self.notifyAll(); // should be only one thread waiting, but just in case
                }
            }
            
            /**
             * No steal or share decisions made locally
             * <p>
             * {@inheritDoc}
             * @deprecated since 4.15.7; use #notifyDecisionRequired
             */
            @Override
            @Deprecated
            public void notifyStealThrottleRequired(jmri.LocoAddress address) {
                InstanceManager.throttleManagerInstance().responseThrottleDecision(address, this, DecisionType.STEAL );
            }

            /**
             * No steal or share decisions made locally
             */
            @Override
            public void notifyDecisionRequired(jmri.LocoAddress address, DecisionType question) {
            }
        };
        boolean ok = InstanceManager.getDefault(ThrottleManager.class).requestThrottle( 
            new jmri.DccLocoAddress(address, longAddress), throttleListener, false);

        // check if reply is coming
        if (!ok) {
            log.info("Throttle for loco {} not available",address);
            InstanceManager.getDefault(ThrottleManager.class).cancelThrottleRequest(
                new jmri.DccLocoAddress(address, longAddress), throttleListener);  //kill the pending request
            return null;
        }

        // now wait for reply from identified throttle
        int waited = 0;
        while (throttle == null && failedThrottleRequest == false && waited <= waitSecs) {
            log.debug("waiting for throttle");
            wait(1000);  //  1 seconds
            waited++;
            if (throttle == null) {
                log.warn("Still waiting for throttle " + address + "!");
            }
        }
        if (throttle == null) {
            log.debug("canceling request for Throttle " + address);
            InstanceManager.getDefault(ThrottleManager.class).cancelThrottleRequest(
                new jmri.DccLocoAddress(address, longAddress), throttleListener);  //kill the pending request
        }
        return throttle;
    }

    public DccThrottle getThrottle(int address, boolean longAddress) {
        return getThrottle(address, longAddress, 30);  //default to 30 seconds wait
    }

    /**
     * Obtains a DCC throttle, including waiting for the command station
     * response.
     *
     * @param re       specifies the desired locomotive
     * @param waitSecs number of seconds to wait for throttle to acquire before
     *                 returning null
     * @return A usable throttle, or null if error
     */
    public DccThrottle getThrottle(BasicRosterEntry re, int waitSecs) {
        log.debug("requesting DccThrottle for rosterEntry " + re.getId());
        if (!inThread) {
            log.warn("getThrottle invoked from invalid context");
        }
        throttle = null;
        ThrottleListener throttleListener = new ThrottleListener() {
            @Override
            public void notifyThrottleFound(DccThrottle t) {
                throttle = t;
                synchronized (self) {
                    self.notifyAll(); // should be only one thread waiting, but just in case
                }
            }

            @Override
            public void notifyFailedThrottleRequest(jmri.LocoAddress address, String reason) {
                log.error("Throttle request failed for " + address + " because " + reason);
                failedThrottleRequest = true;
                synchronized (self) {
                    self.notifyAll(); // should be only one thread waiting, but just in case
                }
            }
            
            /**
             * No steal or share decisions made locally
             * <p>
             * {@inheritDoc}
             * @deprecated since 4.15.7; use #notifyDecisionRequired
             */
            @Override
            @Deprecated
            public void notifyStealThrottleRequired(jmri.LocoAddress address) {
                InstanceManager.throttleManagerInstance().responseThrottleDecision(address, this, DecisionType.STEAL );
            }
            
            /**
             * No steal or share decisions made locally
             * {@inheritDoc}
             */
            @Override
            public void notifyDecisionRequired(jmri.LocoAddress address, DecisionType question) {
            }
        };
        boolean ok = InstanceManager.getDefault(ThrottleManager.class)
                .requestThrottle(re, throttleListener, false);

        // check if reply is coming
        if (!ok) {
            log.info("Throttle for loco " + re.getId() + " not available");
            InstanceManager.getDefault(ThrottleManager.class).cancelThrottleRequest(
                re.getDccLocoAddress(), throttleListener);  //kill the pending request
            return null;
        }

        // now wait for reply from identified throttle
        int waited = 0;
        while (throttle == null && failedThrottleRequest == false && waited <= waitSecs) {
            log.debug("waiting for throttle");
            wait(1000);  //  1 seconds
            waited++;
            if (throttle == null) {
                log.warn("Still waiting for throttle " + re.getId() + "!");
            }
        }
        if (throttle == null) {
            log.debug("canceling request for Throttle {}", re.getId());
            InstanceManager.getDefault(ThrottleManager.class).cancelThrottleRequest(
                re.getDccLocoAddress(), throttleListener);  //kill the pending request
        }
        return throttle;
    }

    public DccThrottle getThrottle(BasicRosterEntry re) {
        return getThrottle(re, 30);  //default to 30 seconds
    }

    /**
     * Write a CV on the service track, including waiting for completion.
     *
     * @param CV    Number 1 through 512
     * @param value Value 0-255 to be written
     * @return true if completed OK
     */
    public boolean writeServiceModeCV(String CV, int value) {
        // get service mode programmer
        Programmer programmer = InstanceManager.getDefault(jmri.GlobalProgrammerManager.class)
                .getGlobalProgrammer();

        if (programmer == null) {
            log.error("No programmer available as JMRI is currently configured");
            return false;
        }

        // do the write, response will wake the thread
        try {
            programmer.writeCV(CV, value, (int value1, int status) -> {
                synchronized (self) {
                    self.notifyAll(); // should be only one thread waiting, but just in case
                }
            });
        } catch (ProgrammerException e) {
            log.warn("Exception during writeServiceModeCV: " + e);
            return false;
        }
        // wait for the result
        wait(-1);

        return true;
    }

    private volatile int cvReturnValue;

    /**
     * Read a CV on the service track, including waiting for completion.
     *
     * @param CV Number 1 through 512
     * @return -1 if error, else value
     */
    public int readServiceModeCV(String CV) {
        // get service mode programmer
        Programmer programmer = InstanceManager.getDefault(jmri.GlobalProgrammerManager.class)
                .getGlobalProgrammer();

        if (programmer == null) {
            log.error("No programmer available as JMRI is currently configured");
            return -1;
        }

        // do the read, response will wake the thread
        cvReturnValue = -1;
        try {
            programmer.readCV(CV, (int value, int status) -> {
                cvReturnValue = value;
                synchronized (self) {
                    self.notifyAll(); // should be only one thread waiting, but just in case
                }
            });
        } catch (ProgrammerException e) {
            log.warn("Exception during writeServiceModeCV: " + e);
            return -1;
        }
        // wait for the result
        wait(-1);
        return cvReturnValue;
    }

    /**
     * Write a CV in ops mode, including waiting for completion.
     *
     * @param CV          Number 1 through 512
     * @param value       0-255 value to be written
     * @param loco        Locomotive decoder address
     * @param longAddress true is the locomotive is using a long address
     * @return true if completed OK
     */
    public boolean writeOpsModeCV(String CV, int value, boolean longAddress, int loco) {
        // get service mode programmer
        Programmer programmer = InstanceManager.getDefault(jmri.AddressedProgrammerManager.class)
                .getAddressedProgrammer(longAddress, loco);

        if (programmer == null) {
            log.error("No programmer available as JMRI is currently configured");
            return false;
        }

        // do the write, response will wake the thread
        try {
            programmer.writeCV(CV, value, (int value1, int status) -> {
                synchronized (self) {
                    self.notifyAll(); // should be only one thread waiting, but just in case
                }
            });
        } catch (ProgrammerException e) {
            log.warn("Exception during writeServiceModeCV: " + e);
            return false;
        }
        // wait for the result
        wait(-1);

        return true;
    }

    JFrame messageFrame = null;
    String message = null;

    /**
     * Internal class to show a Frame
     */
    public class MsgFrame implements Runnable {

        String mMessage;
        boolean mPause;
        boolean mShow;
        JFrame mFrame = null;
        JButton mButton;
        JTextArea mArea;

        public void hide() {
            mShow = false;
            // invoke the operation
            javax.swing.SwingUtilities.invokeLater(this);
        }

        /**
         * Show a message in the message frame, and optionally wait for the user
         * to acknowledge.
         *
         * @param pMessage the message to show
         * @param pPause   true if this automaton should wait for user
         *                 acknowledgment; false otherwise
         */
        public void show(String pMessage, boolean pPause) {
            mMessage = pMessage;
            mPause = pPause;
            mShow = true;

            // invoke the operation
            javax.swing.SwingUtilities.invokeLater(this);
            // wait to proceed?
            if (mPause) {
                synchronized (self) {
                    new jmri.util.WaitHandler(this);
                }
            }
        }

        @Override
        public void run() {
            // create the frame if it doesn't exist
            if (mFrame == null) {
                mFrame = new JFrame("");
                mArea = new JTextArea();
                mArea.setEditable(false);
                mArea.setLineWrap(false);
                mArea.setWrapStyleWord(true);
                mButton = new JButton("Continue");
                mFrame.getContentPane().setLayout(new BorderLayout());
                mFrame.getContentPane().add(mArea, BorderLayout.CENTER);
                mFrame.getContentPane().add(mButton, BorderLayout.SOUTH);
                mButton.addActionListener((java.awt.event.ActionEvent e) -> {
                    synchronized (self) {
                        self.notifyAll(); // should be only one thread waiting, but just in case
                    }
                    mFrame.setVisible(false);
                });
                mFrame.pack();
            }
            if (mShow) {
                // update message, show button if paused
                mArea.setText(mMessage);
                if (mPause) {
                    mButton.setVisible(true);
                } else {
                    mButton.setVisible(false);
                }
                // do optional formatting
                format();
                // center the frame
                mFrame.pack();
                Dimension screen = mFrame.getContentPane().getToolkit().getScreenSize();
                Dimension size = mFrame.getSize();
                mFrame.setLocation((screen.width - size.width) / 2, (screen.height - size.height) / 2);
                // and show it to the user
                mFrame.setVisible(true);
            } else {
                mFrame.setVisible(false);
            }
        }

        /**
         * Abstract method to handle formatting of the text on a show
         */
        protected void format() {
        }
    }

    JFrame debugWaitFrame = null;

    /**
     * Wait for the user to OK moving forward. This is complicated by not
     * running in the GUI thread, and by not wanting to use a modal dialog.
     */
    private void debuggingWait() {
        // post an event to the GUI pane
        Runnable r = () -> {
            // create a prompting frame
            if (debugWaitFrame == null) {
                debugWaitFrame = new JFrame("Automaton paused");
                JButton b = new JButton("Continue");
                debugWaitFrame.getContentPane().add(b);
                b.addActionListener((java.awt.event.ActionEvent e) -> {
                    synchronized (self) {
                        self.notifyAll(); // should be only one thread waiting, but just in case
                    }
                    debugWaitFrame.setVisible(false);
                });
                debugWaitFrame.pack();
            }
            debugWaitFrame.setVisible(true);
        };
        javax.swing.SwingUtilities.invokeLater(r);
        // wait to proceed
        try {
            super.wait();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // retain if needed later
            log.warn("Interrupted during debugging wait, not expected");
        }
    }
    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(AbstractAutomaton.class);
}

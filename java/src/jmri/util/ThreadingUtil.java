package jmri.util;

import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.annotation.Nonnull;

/**
 * Utilities for handling JMRI's threading conventions.
 * <p>
 * For background, see
 * <a href="http://jmri.org/help/en/html/doc/Technical/Threads.shtml">http://jmri.org/help/en/html/doc/Technical/Threads.shtml</a>
 * <p>
 * Note this distinguishes "on layout", for example, Setting a sensor, from "on
 * GUI", for example, manipulating the Swing GUI. That may not be an important
 * distinction now, but it might be later, so we build it into the calls.
 *
 * @author Bob Jacobsen Copyright 2015
 */
public class ThreadingUtil {

    static public interface ThreadAction extends Runnable {

        /**
         * {@inheritDoc}
         * <p>
         * Must handle its own exceptions.
         */
        @Override
        public void run();
    }

    /**
     * Run some layout-specific code before returning.
     * <p>
     * Typical uses:
     * <p>
     * {@code ThreadingUtil.runOnLayout(() -> {
     *     sensor.setState(value);
     * }); }
     *
     * @param ta What to run, usually as a lambda expression
     */
    static public void runOnLayout(@Nonnull ThreadAction ta) {
        runOnGUI(ta);
    }

    /**
     * Run some layout-specific code at some later point.
     * <p>
     * Please note the operation may have happened before this returns. Or
     * later. No long-term guarantees.
     * <p>
     * Typical uses:
     * <p>
     * {@code ThreadingUtil.runOnLayoutEventually(() -> {
     *     sensor.setState(value);
     * }); }
     *
     * @param ta What to run, usually as a lambda expression
     */
    static public void runOnLayoutEventually(@Nonnull ThreadAction ta) {
        runOnGUIEventually(ta);
    }

    /**
     * Run some layout-specific code at some later point, at least a known time
     * in the future.
     * <p>
     * There is no long-term guarantee about the accuracy of the interval.
     * <p>
     * Typical uses:
     * <p>
     * {@code ThreadingUtil.runOnLayoutEventually(() -> {
     *     sensor.setState(value);
     * }, 1000); }
     *
     * @param ta    What to run, usually as a lambda expression
     * @param delay interval in milliseconds
     * @return reference to timer object handling delay so you can cancel if desired; note that operation may have already taken place.
     */
    @Nonnull 
    static public Timer runOnLayoutDelayed(@Nonnull ThreadAction ta, int delay) {
        return runOnGUIDelayed(ta, delay);
    }

    /**
     * Check if on the layout-operation thread.
     *
     * @return true if on the layout-operation thread
     */
    static public boolean isLayoutThread() {
        return isGUIThread();
    }

    /**
     * Run some GUI-specific code before returning
     * <p>
     * Typical uses:
     * <p>
     * {@code ThreadingUtil.runOnGUI(() -> {
     *     mine.setVisible();
     * }); }
     *
     * If an InterruptedException is encountered, it'll be deferred to the 
     * next blocking call via Thread.currentThread().interrupt()
     * 
     * @param ta What to run, usually as a lambda expression
     */
    static public void runOnGUI(@Nonnull ThreadAction ta) {
        if (isGUIThread()) {
            // run now
            ta.run();
        } else {
            // dispatch to Swing
            try {
                SwingUtilities.invokeAndWait(ta);
            } catch (InterruptedException e) {
                log.debug("Interrupted while running on GUI thread");
                Thread.currentThread().interrupt();
            } catch (InvocationTargetException e) {
                log.error("Error while on GUI thread", e.getCause());
                // should have been handled inside the ThreadAction
            }
        }
    }

    /**
     * Run some GUI-specific code at some later point.
     * <p>
     * If invoked from the GUI thread, the work is guaranteed to happen only
     * after the current routine has returned.
     * <p>
     * Typical uses:
     * <p>
     * {@code ThreadingUtil.runOnGUIEventually( ()->{ mine.setVisible(); } ); }
     *
     * @param ta What to run, usually as a lambda expression
     */
    static public void runOnGUIEventually(@Nonnull ThreadAction ta) {
        // dispatch to Swing
        SwingUtilities.invokeLater(ta);
    }

    /**
     * Run some GUI-specific code at some later point, at least a known time in
     * the future.
     * <p>
     * There is no long-term guarantee about the accuracy of the interval.
     * <p>
     * Typical uses:
     * <p>
     * {@code ThreadingUtil.runOnGUIEventually( ()->{ mine.setVisible(); }, 1000
     * ); }
     *
     * @param ta    What to run, usually as a lambda expression
     * @param delay interval in milliseconds
     * @return reference to timer object handling delay so you can cancel if desired; note that operation may have already taken place.
     */
    @Nonnull 
    static public Timer runOnGUIDelayed(@Nonnull ThreadAction ta, int delay) {
        // dispatch to Swing via timer
        Timer timer = new Timer(delay, (ActionEvent e) -> {
            ta.run();
        });
        timer.setRepeats(false);
        timer.start();
        return timer;
    }

    /**
     * Check if on the GUI event dispatch thread.
     *
     * @return true if on the event dispatch thread
     */
    static public boolean isGUIThread() {
        return SwingUtilities.isEventDispatchThread();
    }

    /**
     * Check whether a specific thread is running (or able to run) right now.
     *
     * @param t the thread to check
     * @return true is the specified thread is or could be running right now
     */
    static public boolean canThreadRun(@Nonnull Thread t) {
        Thread.State s = t.getState();
        return s.equals(Thread.State.RUNNABLE);
    }

    /**
     * Check whether a specific thread is currently waiting.
     * <p>
     * Note: This includes both waiting due to an explicit wait() call, and due
     * to being blocked attempting to synchronize.
     * <p>
     * Note: {@link #canThreadRun(Thread)} and {@link #isThreadWaiting(Thread)}
     * should never simultaneously be true, but it might look that way due to
     * sampling delays when checking on another thread.
     *
     * @param t the thread to check
     * @return true is the specified thread is or could be running right now
     */
    static public boolean isThreadWaiting(@Nonnull Thread t) {
        Thread.State s = t.getState();
        return s.equals(Thread.State.BLOCKED) || s.equals(Thread.State.WAITING) || s.equals(Thread.State.TIMED_WAITING);
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ThreadingUtil.class);
}

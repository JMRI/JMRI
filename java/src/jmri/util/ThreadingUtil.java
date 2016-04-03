package jmri.util;

/**
 * Utilities for handling JMRI's threading conventions
 * <p>
 * For background, see <a href="http://jmri.org/help/en/html/doc/Technical/Threads.shtml">http://jmri.org/help/en/html/doc/Technical/Threads.shtml</a>
 * <p>
 * Note this distinguishes "on layout", e.g. Setting a sensor, from
 * "on GUI", e.g. manipulating the Swing GUI. That may not be an important
 * distinction now, but it might be later, so we build it into the calls.
 *
 * @author Bob Jacobsen   Copyright 2015
 */
public class ThreadingUtil {

    static public interface ThreadAction extends Runnable {
        /**
         * Must handle its own exceptions
         */
        public void run();
    }

    /** 
     * Run some layout-specific code before returning
     * <p>
     * Typical uses:
     * <p><code>ThreadingUtil.runOnLayout( ()->{ sensor.setState(value); } );</code>
     * 
     * @param ta What to run, usually as a lambda expression
     */
    static public void runOnLayout(ThreadAction ta) {
        runOnGUI(ta);
    }

    /** 
     * Run some layout-specific code at some later point.
     * <p>
     * Please note the operation may have happened before this returns. Or not.
     * <p>
     * Typical uses:
     * <p><code>ThreadingUtil.runOnLayoutEventually( ()->{ sensor.setState(value); } );</code>
     * 
     * @param ta What to run, usually as a lambda expression
     */
    static public void runOnLayoutEventually(ThreadAction ta) {
        runOnGUIEventually(ta);
    }

    /** 
     * Check if on the layout-operation thread.
     */
    static public boolean isLayoutThread() {
        return isGUIThread();
    }

    /** 
     * Run some GUI-specific code before returning
     * <p>
     * Typical uses:
     * <p><code>ThreadingUtil.runOnGUI( ()->{ mine.setVisible(); } );</code>
     * 
     * @param ta What to run, usually as a lambda expression
     */
    static public void runOnGUI(ThreadAction ta) {
        if (isGUIThread()) {
            // run now
            ta.run();
        } else {
            // dispatch to Swing
            try {
                javax.swing.SwingUtilities.invokeAndWait(ta);
            } catch (InterruptedException e) {
                log.warn("While on GUI thread", e);
                // we just continue from InterruptedException for now
            } catch (java.lang.reflect.InvocationTargetException e) {
                log.error("Error while on GUI thread", e);
                // should have been handled inside the ThreadAction
            }
        }
    }

    /** 
     * Run some layout-specific code at some later point.
     * <p>
     * Please note the operation may have happened before this returns. Or not.
     * <p>
     * Typical uses:
     * <p><code>ThreadingUtil.runOnGUIEventually( ()->{ mine.setVisible(); } );</code>
     * 
     * @param ta What to run, usually as a lambda expression
     */
    static public void runOnGUIEventually(ThreadAction ta) {
        if (isGUIThread()) {
            // run now, despite the "eventually" in the name; just a simplification
            ta.run();
        } else {
            // dispatch to Swing
            javax.swing.SwingUtilities.invokeLater(ta);
        }
    }

    /** 
     * Check if on the GUI thread.
     */
    static public boolean isGUIThread() {
        return javax.swing.SwingUtilities.isEventDispatchThread();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ThreadingUtil.class.getName());
}

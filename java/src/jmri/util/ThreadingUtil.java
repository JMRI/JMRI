package jmri.util;

/**
 * Utilities for handling JMRI's threading conventions
 * <p>
 * For background, see http://localhost/help/en/html/doc/Technical/Threads.shtml
 * <p>
 * Note this distinguished "on layout", e.g. setting a sensor, from
 * "on GUI", e.g. manipulating the GUI. That may or may not be an important
 * distinction now, but it might be later, so we build it into the calls.
 *
 * @author Bob Jacobsen   Copyright 2015
 */
public class ThreadingUtil {

    static public interface ThreadAction extends Runnable {
        /**
         * Must handle it's own exceptions
         */
        public void run();
    }

    /** 
     * Run some layout-specific code before returning
     * <p>
     * Typical uses:
     * <p><code>ThreadUtil.runOnLayout( ()->{ sensor.setState(value); } );</code>
     * 
     * @param condition name of condition being waited for; will appear in Assert.fail if condition not true fast enough
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
     * <p><code>ThreadUtil.runOnLayoutEventually( ()->{ sensor.setState(value); } );</code>
     * 
     * @param condition name of condition being waited for; will appear in Assert.fail if condition not true fast enough
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
     * <p><code>ThreadUtil.runOnGUI( ()->{ mine.setVisible(); } );</code>
     * 
     * @param condition name of condition being waited for; will appear in Assert.fail if condition not true fast enough
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
     * <p><code>ThreadUtil.runOnGUIEventually( ()->{ mine.setVisible(); } );</code>
     * 
     * @param condition name of condition being waited for; will appear in Assert.fail if condition not true fast enough
     */
    static public void runOnGUIEventually(ThreadAction ta) {
        if (isGUIThread()) {
            // run now
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

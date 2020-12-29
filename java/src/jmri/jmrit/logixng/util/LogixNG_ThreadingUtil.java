package jmri.jmrit.logixng.util;

import jmri.util.*;

import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.Timer;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import jmri.InvokeOnGuiThread;
import jmri.util.ThreadingUtil;
import jmri.util.ThreadingUtil.ThreadAction;

/**
 * Utilities for handling JMRI's LogixNG threading conventions.
 * <p>
 * For background, see
 * <a href="http://jmri.org/help/en/html/doc/Technical/Threads.shtml">http://jmri.org/help/en/html/doc/Technical/Threads.shtml</a>
 * <p>
 * This is the ThreadingUtil class for LogixNG.
 *
 * @author Bob Jacobsen      Copyright 2015
 * @author Daniel Bergqvist  Copyright 2020
 */
@ThreadSafe
public class LogixNG_ThreadingUtil {

    private static volatile boolean stopThread = false;
    
    @InvokeOnGuiThread
    public static void launchLogixNGThread() {
        
        synchronized(LogixNG_ThreadingUtil.class) {
            if (logixNGThread != null) {
                throw new RuntimeException("logixNGThread is already started");
            }
            
            logixNGEventQueue = new ArrayBlockingQueue<>(1024);
            logixNGThread = new Thread(() -> {
                while (!stopThread) {
                    try {
                        ThreadEvent event = logixNGEventQueue.take();
                        if (event._lock != null) {
                            synchronized(event._lock) {
                                if (!stopThread) event._threadAction.run();
                                event._lock.notify();
                            }
                        } else {
                            event._threadAction.run();
                        }
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
            }, "JMRI LogixNGThread");
            logixNGThread.setDaemon(true);
            logixNGThread.start();
        }
    }

    /**
     * Run some LogixNG-specific code before returning.
     * <p>
     * Typical uses:
     * <p> {@code
     * ThreadingUtil.runOnLogixNG(() -> {
     *     logixNG.doSomething(value);
     * }); 
     * }
     *
     * @param ta What to run, usually as a lambda expression
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = {"WA_NOT_IN_LOOP", "UW_UNCOND_WAIT"},
            justification="Method runOnLogixNG() doesn't have a loop. Waiting for single possible event."+
                    "The thread that is going to call notify() cannot get"+
                    " it's hands on the lock until wait() is called, "+
                    " since the caller must first fetch the event from the"+
                    " event queue and the event is put on the event queue in"+
                    " the synchronize block.")
    static public void runOnLogixNG(@Nonnull ThreadAction ta) {
        if (logixNGThread == null) throw new RuntimeException("Daniel: LogixNG thread not started");
        if (logixNGThread != null) {
            Object lock = new Object();
            synchronized(lock) {
                logixNGEventQueue.add(new ThreadEvent(ta, lock));
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    log.debug("Interrupted while running on LogixNG thread");
                    Thread.currentThread().interrupt();
                }
            }
        } else {
            ThreadingUtil.runOnGUI(ta);
        }
    }

    /**
     * Run some layout-specific code at some later point.
     * <p>
     * Please note the operation may have happened before this returns. Or
     * later. No long-term guarantees.
     * <p>
     * Typical uses:
     * <p> {@code
     * ThreadingUtil.runOnLayoutEventually(() -> {
     *     sensor.setState(value);
     * }); 
     * }
     *
     * @param ta What to run, usually as a lambda expression
     */
    static public void runOnLogixNGEventually(@Nonnull ThreadAction ta) {
        if (logixNGThread == null) throw new RuntimeException("Daniel: LogixNG thread not started");
        if (logixNGThread != null) {
            logixNGEventQueue.add(new ThreadEvent(ta));
        } else {
            ThreadingUtil.runOnGUIEventually(ta);
        }
    }

    /**
     * Run some layout-specific code at some later point, at least a known time
     * in the future.
     * <p>
     * There is no long-term guarantee about the accuracy of the interval.
     * <p>
     * Typical uses:
     * <p> {@code
     * ThreadingUtil.runOnLayoutEventually(() -> {
     *     sensor.setState(value);
     * }, 1000); 
     * }
     *
     * @param ta    What to run, usually as a lambda expression
     * @param delay interval in milliseconds
     * @return reference to timer object handling delay so you can cancel if desired; note that operation may have already taken place.
     */
    @Nonnull 
    static public Timer runOnLogixNGDelayed(@Nonnull ThreadAction ta, int delay) {
        if (logixNGThread == null) throw new RuntimeException("Daniel: LogixNG thread not started");
        if (logixNGThread != null) {
            // dispatch to Swing via timer. We are forced to use a Swing Timer
            // since the method returns a Timer object and we don't want to
            // change the method interface.
            Timer timer = new Timer(delay, (ActionEvent e) -> {
                // Dispatch the event to the layout event handler once the time
                // has passed.
                logixNGEventQueue.add(new ThreadEvent(ta));
            });
            timer.setRepeats(false);
            timer.start();
            return timer;
        } else {
            return ThreadingUtil.runOnGUIDelayed(ta, delay);
        }
    }

    /**
     * Check if on the layout-operation thread.
     *
     * @return true if on the layout-operation thread
     */
    static public boolean isLogixNGThread() {
        if (logixNGThread != null) {
            return logixNGThread == Thread.currentThread();
        } else {
            return ThreadingUtil.isGUIThread();
        }
    }

    /**
     * Checks if the the current thread is the layout thread.
     * The check is only done if debug is enabled.
     */
    static public void checkIsLogixNGThread() {
        if (log.isDebugEnabled()) {
            if (!isLogixNGThread()) {
                LoggingUtil.warnOnce(log, "checkIsLogixNGThread() called on wrong thread", new Exception());
            }
        }
    }

    static private class ThreadEvent {
        private final ThreadAction _threadAction;
        private final Object _lock;

        public ThreadEvent(ThreadAction threadAction) {
            _threadAction = threadAction;
            _lock = null;
        }

        public ThreadEvent(ThreadAction threadAction,
                Object lock) {
            _threadAction = threadAction;
            _lock = lock;
        }
    }

    public static void stopLogixNGThread() {
        synchronized(LogixNG_ThreadingUtil.class) {
            if (logixNGThread != null) {
                stopThread = true;
                logixNGThread.interrupt();
                try {
                    logixNGThread.join(0);
                } catch (InterruptedException e) {
                    throw new RuntimeException("stopLogixNGThread() was interrupted");
                }
                if (logixNGThread.getState() != Thread.State.TERMINATED) {
                    throw new RuntimeException("Could not stop logixNGThread. Current state: "+logixNGThread.getState().name());
                }
                stopThread = false;
                logixNGThread = null;
            }
        }
    }

    public static void assertLogixNGThreadNotRunning() {
        synchronized(LogixNG_ThreadingUtil.class) {
            if (logixNGThread != null) {
                throw new RuntimeException("logixNGThread is running");
            }
        }
    }
    
    private static Thread logixNGThread = null;
    private static BlockingQueue<ThreadEvent> logixNGEventQueue = null;

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LogixNG_ThreadingUtil.class);

}


package jmri.jmrit.logixng.util;

import java.awt.event.ActionEvent;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.swing.Timer;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import jmri.util.*;

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
public class LogixNG_Thread {

    public static final int DEFAULT_LOGIXNG_THREAD = 0;
    public static final int DEFAULT_LOGIXNG_DEBUG_THREAD = 1;
    
    private static final Map<Integer, LogixNG_Thread> _threads = new HashMap<>();
    private static final Map<String, LogixNG_Thread> _threadNames = new HashMap<>();
    private static int _highestThreadID = -1;
    
    private final int _threadID;
    private String _name;
    private volatile boolean _stopThread = false;
    private volatile boolean _threadIsStopped = false;
    
    private final Thread _logixNGThread;
    private boolean _threadInUse = false;
    private final BlockingQueue<ThreadEvent> _eventQueue;
    
    
    public static LogixNG_Thread createNewThread(@Nonnull String name) {
        return createNewThread(-1, name);
    }
    
    public static LogixNG_Thread createNewThread(int threadID, @Nonnull String name) {
        synchronized (LogixNG_Thread.class) {
            if (threadID == -1) {
                threadID = ++_highestThreadID;
            } else {
                if (threadID > _highestThreadID) _highestThreadID = threadID;
            }
            
            LogixNG_Thread oldThread = _threads.get(threadID);
            if (oldThread != null) {
                if (oldThread._name.equals(name)) return oldThread;
                
                if (_threadNames.containsKey(name)) {
                    throw new IllegalArgumentException(String.format("Thread name %s already exists", name));
                }
                
                oldThread._name = name;
                return oldThread;
            }
            
            if (_threadNames.containsKey(name)) {
                throw new IllegalArgumentException(String.format("Thread name %s already exists", name));
            }
            LogixNG_Thread thread = new LogixNG_Thread(threadID, name);
            _threads.put(threadID, thread);
            _threadNames.put(name, thread);
            thread._logixNGThread.start();
            
            return thread;
        }
    }
    
    public static boolean validateNewThreadName(@Nonnull String name) {
        synchronized (LogixNG_Thread.class) {
            return !_threadNames.containsKey(name);
        }
    }
    
    public static LogixNG_Thread getThread(int threadID) {
        synchronized (LogixNG_Thread.class) {
            LogixNG_Thread thread = _threads.get(threadID);
            if (thread == null) {
                switch (threadID) {
                    case DEFAULT_LOGIXNG_THREAD:
                        thread = createNewThread(DEFAULT_LOGIXNG_THREAD, Bundle.getMessage("LogixNG_Thread"));
                        break;
                    case DEFAULT_LOGIXNG_DEBUG_THREAD:
                        thread = createNewThread(DEFAULT_LOGIXNG_DEBUG_THREAD, Bundle.getMessage("LogixNG_DebugThread"));
                        break;
                    default:
                        throw new IllegalArgumentException(String.format("Thread ID %d does not exists", threadID));
                }
            }
            return thread;
        }
    }
    
    public static int getThreadID(String name) {
        synchronized (LogixNG_Thread.class) {
            for (LogixNG_Thread t : _threads.values()) {
                if (name.equals(t._name)) return t._threadID;
            }
            throw new IllegalArgumentException(String.format("Thread name \"%s\" does not exists", name));
        }
    }
    
    public static void deleteThread(LogixNG_Thread thread) {
        synchronized (LogixNG_Thread.class) {
            LogixNG_Thread aThread = _threads.get(thread._threadID);
            
            if (aThread == null) throw new IllegalArgumentException("Thread does not exists");
            if (aThread != thread) throw new IllegalArgumentException("Thread ID does not match");
            if (aThread._threadInUse) throw new IllegalArgumentException("Thread is in use");
            
            _threads.remove(thread._threadID);
            _threadNames.remove(thread._name);
        }
    }
    
    public static Collection<LogixNG_Thread> getThreads() {
        return Collections.unmodifiableCollection(_threads.values());
    }
    
    private LogixNG_Thread(int threadID, String name) {
        _threadID = threadID;
        _name = name;
        
        synchronized(LogixNG_Thread.class) {
            
            _eventQueue = new ArrayBlockingQueue<>(1024);
            _logixNGThread = new Thread(() -> {
                while (!_stopThread) {
                    try {
                        ThreadEvent event = _eventQueue.take();
                        if (event._lock != null) {
                            synchronized(event._lock) {
                                if (!_stopThread) event._threadAction.run();
                                event._lock.notify();
                            }
                        } else {
                            event._threadAction.run();
                        }
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
                _threadIsStopped = true;
            }, "JMRI LogixNGThread");
            
            _logixNGThread.setDaemon(true);
        }
    }
    
    public Thread getThread() {
        return _logixNGThread;
    }
    
    public int getThreadId() {
        return _threadID;
    }
    
    public String getThreadName() {
        return _name;
    }
    
    public void setThreadName(@Nonnull String name) {
        if (_name.equals(name)) return;
        
        synchronized (LogixNG_Thread.class) {
            if (_threadNames.containsKey(name)) {
                throw new IllegalArgumentException(String.format("Thread name %s already exists", name));
            }
            _threadNames.remove(_name);
            _threadNames.put(name, this);
            _name = name;
        }
    }
    
    public boolean getThreadInUse() {
        return _threadInUse;
    }
    
    /**
     * Set the thread to "in use".
     * If a thread is in use, it cannot be unset as not in use.
     */
    public void setThreadInUse() {
        _threadInUse = true;
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
    public void runOnLogixNG(@Nonnull ThreadAction ta) {
        if (_logixNGThread != null) {
            Object lock = new Object();
            synchronized(lock) {
                _eventQueue.add(new ThreadEvent(ta, lock));
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    log.debug("Interrupted while running on LogixNG thread");
                    Thread.currentThread().interrupt();
                }
            }
        } else {
            throw new RuntimeException("LogixNG thread not started. ThreadID: "+Integer.toString(_threadID));
        }
    }

    /**
     * Run some LogixNG-specific code at some later point.
     * <p>
     * Please note the operation may have happened before this returns. Or
     * later. No long-term guarantees.
     * <p>
     * Typical uses:
     * <p> {@code
     * ThreadingUtil.runOnLogixNGEventually(() -> {
     *     sensor.setState(value);
     * }); 
     * }
     *
     * @param ta What to run, usually as a lambda expression
     */
    public void runOnLogixNGEventually(@Nonnull ThreadAction ta) {
        if (_logixNGThread != null) {
            _eventQueue.add(new ThreadEvent(ta));
        } else {
            throw new RuntimeException("LogixNG thread not started");
        }
    }

    /**
     * Run some LogixNG-specific code at some later point, at least a known time
     * in the future.
     * <p>
     * There is no long-term guarantee about the accuracy of the interval.
     * <p>
     * Typical uses:
     * <p> {@code
     * ThreadingUtil.runOnLogixNGDelayed(() -> {
     *     sensor.setState(value);
     * }, 1000); 
     * }
     *
     * @param ta    What to run, usually as a lambda expression
     * @param delay interval in milliseconds
     * @return reference to timer object handling delay so you can cancel if desired; note that operation may have already taken place.
     */
    @Nonnull 
    public Timer runOnLogixNGDelayed(@Nonnull ThreadAction ta, int delay) {
        if (_logixNGThread != null) {
            // dispatch to logixng thread via timer. We are forced to use a
            // Swing Timer since the method returns a Timer object and we don't
            // want to change the method interface.
            Timer timer = new Timer(delay, (ActionEvent e) -> {
                // Dispatch the event to the LogixNG event handler once the time
                // has passed.
                _eventQueue.add(new ThreadEvent(ta));
            });
            timer.setRepeats(false);
            timer.start();
            return timer;
        } else {
            throw new RuntimeException("LogixNG thread not started");
        }
    }

    /**
     * Check if on the LogixNG-operation thread.
     *
     * @return true if on the LogixNG-operation thread
     */
    public boolean isLogixNGThread() {
        if (_logixNGThread != null) {
            return _logixNGThread == Thread.currentThread();
        } else {
            throw new RuntimeException("LogixNG thread not started");
        }
    }

    /**
     * Checks if the the current thread is the LogixNG thread.
     * The check is only done if debug is enabled.
     */
    public void checkIsLogixNGThread() {
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

    private void stopLogixNGThread() {
        synchronized(LogixNG_Thread.class) {
            if (_logixNGThread != null) {
                _stopThread = true;
                _logixNGThread.interrupt();
                try {
                    _logixNGThread.join(0);
                } catch (InterruptedException e) {
                    throw new RuntimeException("stopLogixNGThread() was interrupted");
                }
                if (_logixNGThread.getState() != Thread.State.TERMINATED) {
                    throw new RuntimeException("Could not stop logixNGThread. Current state: "+_logixNGThread.getState().name());
                }
                _threads.remove(_threadID);
                _threadNames.remove(_name);
                _stopThread = false;
            }
        }
    }

    public static void stopAllLogixNGThreads() {
        synchronized(LogixNG_Thread.class) {
            List<LogixNG_Thread> list = new ArrayList<>(_threads.values());
            for (LogixNG_Thread thread : list) {
                thread.stopLogixNGThread();
            }
        }
    }
    
    public static void assertLogixNGThreadNotRunning() {
        synchronized(LogixNG_Thread.class) {
            boolean aThreadIsRunning = false;
            for (LogixNG_Thread thread : _threads.values()) {
                if (!thread._threadIsStopped) {
                    aThreadIsRunning = true;
                    thread.stopLogixNGThread();
                }
            }
            if (aThreadIsRunning) {
                throw new RuntimeException("logixNGThread is running");
            }
        }
    }
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LogixNG_Thread.class);

}


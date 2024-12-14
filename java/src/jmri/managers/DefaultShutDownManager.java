package jmri.managers;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import sun.misc.Signal;
import sun.misc.SignalHandler;

import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.event.WindowEvent;

import java.util.*;
import java.util.concurrent.*;

import jmri.ShutDownManager;
import jmri.ShutDownTask;
import jmri.util.SystemType;
import jmri.util.JmriThreadPoolExecutor;

import jmri.beans.Bean;
import jmri.util.ThreadingUtil;

/**
 * The default implementation of {@link ShutDownManager}. This implementation
 * makes the following assumptions:
 * <ul>
 * <li>The {@link #shutdown()} and {@link #restart()} methods are called on the
 * application's main thread.</li>
 * <li>If the application has a graphical user interface, the application's main
 * thread is the event dispatching thread.</li>
 * <li>Application windows may contain code that <em>should</em> be run within a
 * registered {@link ShutDownTask#run()} method, but are not. A side effect
 * of this assumption is that <em>all</em> displayable application windows are
 * closed by this implementation when shutdown() or restart() is called and a
 * ShutDownTask has not aborted the shutdown or restart.</li>
 * <li>It is expected that SIGINT and SIGTERM should trigger a clean application
 * exit.</li>
 * </ul>
 * <p>
 * If another implementation of ShutDownManager has not been registered with the
 * {@link jmri.InstanceManager}, an instance of this implementation will be
 * automatically registered as the ShutDownManager.
 * <p>
 * Developers other applications that cannot accept the above assumptions are
 * recommended to create their own implementations of ShutDownManager that
 * integrates with their application's lifecycle and register that
 * implementation with the InstanceManager as soon as possible in their
 * application.
 *
 * @author Bob Jacobsen Copyright (C) 2008
 */
public class DefaultShutDownManager extends Bean implements ShutDownManager {

    private static volatile boolean shuttingDown = false;
    private volatile boolean shutDownComplete = false; // used by tests

    private final Set<Callable<Boolean>> callables = new CopyOnWriteArraySet<>();
    private final Set<EarlyTask> earlyRunnables = new CopyOnWriteArraySet<>();
    private final Set<Runnable> runnables = new CopyOnWriteArraySet<>();

    protected final Thread shutdownHook;

    // 30secs to complete EarlyTasks, 30 secs to complete Main tasks.
    // package private for testing
    int tasksTimeOutMilliSec = 30000;

    private static final String NO_NULL_TASK = "Shutdown task cannot be null."; // NOI18N
    private static final String PROP_SHUTTING_DOWN = "shuttingDown"; // NOI18N

    private boolean blockingShutdown = false;   // Used by tests

    /**
     * Create a new shutdown manager.
     */
    public DefaultShutDownManager() {
        super(false);
        // This shutdown hook allows us to perform a clean shutdown when
        // running in headless mode and SIGINT (Ctrl-C) or SIGTERM. It
        // executes the shutdown tasks without calling System.exit() since
        // calling System.exit() within a shutdown hook will cause the
        // application to hang.
        // This shutdown hook also allows OS X Application->Quit to trigger our
        // shutdown tasks, since that simply calls System.exit()
        this.shutdownHook = ThreadingUtil.newThread(() -> DefaultShutDownManager.this.shutdown(0, false));
        try {
            Runtime.getRuntime().addShutdownHook(this.shutdownHook);
        } catch (IllegalStateException ex) {
            // thrown only if System.exit() has been called, so ignore
        }

        // register a Signal handlers that do shutdown
        try {
            if (SystemType.isMacOSX() || SystemType.isLinux()) {
                SignalHandler handler = new SignalHandler () {
                    @Override
                    public void handle(Signal sig) {
                        shutdown();
                    }
                };
                Signal.handle(new Signal("TERM"), handler);
                Signal.handle(new Signal("INT"), handler);

                handler = new SignalHandler () {
                    @Override
                    public void handle(Signal sig) {
                        restart();
                    }
                };
                Signal.handle(new Signal("HUP"), handler);
            }

            else if (SystemType.isWindows()) {
                SignalHandler handler = new SignalHandler () {
                    @Override
                    public void handle(Signal sig) {
                        shutdown();
                    }
                };
                Signal.handle(new Signal("TERM"), handler);
            }

        } catch (NullPointerException e) {
            log.warn("Failed to add signal handler due to missing signal definition");
        }
    }

    /**
     * Set if shutdown should block GUI/Layout thread.
     * @param value true if blocking, false otherwise
     */
    public void setBlockingShutdown(boolean value) {
        blockingShutdown = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void register(ShutDownTask s) {
        Objects.requireNonNull(s, NO_NULL_TASK);
        this.earlyRunnables.add(new EarlyTask(s));
        this.runnables.add(s);
        this.callables.add(s);
        this.addPropertyChangeListener(PROP_SHUTTING_DOWN, s);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void register(Callable<Boolean> task) {
        Objects.requireNonNull(task, NO_NULL_TASK);
        this.callables.add(task);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void register(Runnable task) {
        Objects.requireNonNull(task, NO_NULL_TASK);
        this.runnables.add(task);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void deregister(ShutDownTask s) {
        this.removePropertyChangeListener(PROP_SHUTTING_DOWN, s);
        this.callables.remove(s);
        this.runnables.remove(s);
        for (EarlyTask r : earlyRunnables) {
            if (r.task == s) {
                earlyRunnables.remove(r);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void deregister(Callable<Boolean> task) {
        this.callables.remove(task);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void deregister(Runnable task) {
        this.runnables.remove(task);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Callable<Boolean>> getCallables() {
        List<Callable<Boolean>> list = new ArrayList<>();
        list.addAll(callables);
        return Collections.unmodifiableList(list);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Runnable> getRunnables() {
        List<Runnable> list = new ArrayList<>();
        list.addAll(runnables);
        return Collections.unmodifiableList(list);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown() {
        shutdown(0, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void restart() {
        shutdown(100, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void restartOS() {
        shutdown(210, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdownOS() {
        shutdown(200, true);
    }

    /**
     * First asks the shutdown tasks if shutdown is allowed.
     * Returns if the shutdown was aborted by the user, in which case the program
     * should continue to operate.
     * <p>
     * After this check does not return under normal circumstances.
     * Closes any displayable windows.
     * Executes all registered {@link jmri.ShutDownTask}
     * Runs the Early shutdown tasks, the main shutdown tasks,
     * then terminates the program with provided status.
     *
     * @param status integer status on program exit
     * @param exit   true if System.exit() should be called if all tasks are
     *               executed correctly; false otherwise
     */
    public void shutdown(int status, boolean exit) {
        Runnable shutdownTask = () -> doShutdown(status, exit);

        if (!blockingShutdown) {
            new Thread(shutdownTask).start();
        } else {
            shutdownTask.run();
        }
    }

    /**
     * First asks the shutdown tasks if shutdown is allowed.
     * Returns if the shutdown was aborted by the user, in which case the program
     * should continue to operate.
     * <p>
     * After this check does not return under normal circumstances.
     * Closes any displayable windows.
     * Executes all registered {@link jmri.ShutDownTask}
     * Runs the Early shutdown tasks, the main shutdown tasks,
     * then terminates the program with provided status.
     * <p>
     *
     * @param status integer status on program exit
     * @param exit   true if System.exit() should be called if all tasks are
     *               executed correctly; false otherwise
     */
    @SuppressFBWarnings(value = "DM_EXIT", justification = "OK to directly exit standalone main")
    private void doShutdown(int status, boolean exit) {
        log.debug("shutdown called with {} {}", status, exit);
        if (!shuttingDown) {
            long start = System.currentTimeMillis();
            log.debug("Shutting down with {} callable and {} runnable tasks",
                callables.size(), runnables.size());
            setShuttingDown(true);
            // First check if shut down is allowed
            for (Callable<Boolean> task : callables) {
                try {
                    if (Boolean.FALSE.equals(task.call())) {
                        setShuttingDown(false);
                        return;
                    }
                } catch (Exception ex) {
                    log.error("Unable to stop", ex);
                    setShuttingDown(false);
                    return;
                }
            }

            boolean abort = jmri.util.ThreadingUtil.runOnGUIwithReturn(() -> {
                return jmri.configurexml.StoreAndCompare.checkPermissionToStoreIfNeeded();
            });
            if (abort) {
                log.info("User aborted the shutdown request due to not having permission to store changes");
                setShuttingDown(false);
                return;
            }

            closeFrames(start);

            // wait for parallel tasks to complete
            runShutDownTasks(new HashSet<>(earlyRunnables), "JMRI ShutDown - Early Tasks");

            jmri.configurexml.StoreAndCompare.requestStoreIfNeeded();

            // wait for parallel tasks to complete
            runShutDownTasks(runnables, "JMRI ShutDown - Main Tasks");

            // success
            log.debug("Shutdown took {} milliseconds.", System.currentTimeMillis() - start);
            log.info("Normal termination complete");
            // and now terminate forcefully
            if (exit) {
                System.exit(status);
            }
            shutDownComplete = true;
        }
    }

    private void closeFrames( long startTime ) {
        // close any open windows by triggering a closing event
        // this gives open windows a final chance to perform any cleanup
        if (!GraphicsEnvironment.isHeadless()) {
            Arrays.asList(Frame.getFrames()).stream().forEach(frame -> {
                // do not run on thread, or in parallel, as System.exit()
                // will get called before windows can close
                if (frame.isDisplayable()) { // dispose() has not been called
                    log.debug("Closing frame \"{}\", title: \"{}\"", frame.getName(), frame.getTitle());
                    long timer = System.currentTimeMillis();
                    frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
                    log.debug("Frame \"{}\" took {} milliseconds to close",
                        frame.getName(), System.currentTimeMillis() - timer);
                }
            });
        }
        log.debug("windows completed closing {} milliseconds after starting shutdown",
            System.currentTimeMillis() - startTime );
    }

    // blocks the main Thread until tasks complete or timed out
    private void runShutDownTasks(Set<Runnable> toRun, String threadName ) {
        Set<Runnable> sDrunnables = new HashSet<>(toRun); // copy list so cannot be modified
        if ( sDrunnables.isEmpty() ) {
            return;
        }
        // use a custom Executor which checks the Task output for Exceptions.
        JmriThreadPoolExecutor executor = new JmriThreadPoolExecutor(sDrunnables.size(), threadName);
        List<Future<?>> complete = new ArrayList<>();
        long timeoutEnd = System.currentTimeMillis() + tasksTimeOutMilliSec;

        sDrunnables.forEach((runnable) -> complete.add(executor.submit(runnable)));

        executor.shutdown(); // no more tasks allowed from here, starts the threads.

         // Handle individual task timeouts
        for (Future<?> future : complete) {
            long remainingTime = timeoutEnd - System.currentTimeMillis(); // Calculate remaining time

            if (remainingTime <= 0) {
                log.error("Timeout reached before all tasks were completed");
                break;
            }

            try {
                // Attempt to get the result of each task within the remaining time
                future.get(remainingTime, TimeUnit.MILLISECONDS);
            } catch (TimeoutException te) {
                log.error("{} Task timed out: {}", threadName, future);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                // log.error("{} Task was interrupted: {}", threadName, future);
            } catch (ExecutionException ee) {
                // log.error("{} Task threw an exception: {}", threadName, future, ee.getCause());
            }
        }

        executor.shutdownNow(); // do not leave Threads hanging before exit, force stop.

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isShuttingDown() {
        return shuttingDown;
    }

    /**
     * Flag to indicate when all shutDown tasks completed.
     * For test purposes, the app would normally exit before setting the flag.
     * @return true when Shutdown tasks are complete and System.exit is not called.
     */
    public boolean isShutDownComplete() {
        return shutDownComplete;
    }

    /**
     * This method is static so that if multiple DefaultShutDownManagers are
     * registered, they are all aware of this state.
     *
     * @param state true if shutting down; false otherwise
     */
    protected void setShuttingDown(boolean state) {
        boolean old = shuttingDown;
        setStaticShuttingDown(state);
        log.debug("Setting shuttingDown to {}", state);
        if ( !state ) { // reset complete if previously set
            shutDownComplete = false;
        }
        firePropertyChange(PROP_SHUTTING_DOWN, old, state);
    }

    // package private so tests can reset
    static synchronized void setStaticShuttingDown(boolean state){
        shuttingDown = state;
    }

    private static class EarlyTask implements Runnable {

        final ShutDownTask task; // access outside of this class

        EarlyTask( ShutDownTask runnableTask) {
            task = runnableTask;
        }

        @Override
        public void run() {
            task.runEarly();
        }

        @Override // improve error message on failure
        public String toString(){
            return task.toString();
        }

    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultShutDownManager.class);

}

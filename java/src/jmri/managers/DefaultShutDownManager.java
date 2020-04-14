package jmri.managers;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.event.WindowEvent;
import java.util.*;

import jmri.ShutDownManager;
import jmri.ShutDownTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default implementation of {@link ShutDownManager}. This implementation
 * makes the following assumptions:
 * <ul>
 * <li>The {@link #shutdown()} and {@link #restart()} methods are called on the
 * application's main thread.</li>
 * <li>If the application has a graphical user interface, the application's main
 * thread is the event dispatching thread.</li>
 * <li>Application windows may contain code that <em>should</em> be run within a
 * registered {@link ShutDownTask#execute()} method, but are not. A side effect
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
public class DefaultShutDownManager implements ShutDownManager {

    private static boolean shuttingDown = false;
    private final static Logger log = LoggerFactory.getLogger(DefaultShutDownManager.class);
    private final ArrayList<ShutDownTask> tasks = new ArrayList<>();
    protected final Thread shutdownHook;

    /**
     * Create a new shutdown manager.
     */
    public DefaultShutDownManager() {
        // This shutdown hook allows us to perform a clean shutdown when
        // running in headless mode and SIGINT (Ctrl-C) or SIGTERM. It
        // executes the shutdown tasks without calling System.exit() since
        // calling System.exit() within a shutdown hook will cause the
        // application to hang.
        // This shutdown hook also allows OS X Application->Quit to trigger our
        // shutdown tasks, since that simply calls System.exit();
        this.shutdownHook = new Thread(() -> {
            DefaultShutDownManager.this.shutdown(0, false);
        });
        try {
            Runtime.getRuntime().addShutdownHook(this.shutdownHook);
        } catch (IllegalStateException ex) {
            // thrown only if System.exit() has been called, so ignore
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    synchronized public void register(ShutDownTask s) {
        Objects.requireNonNull(s, "Shutdown task cannot be null.");
        if (!this.tasks.contains(s)) {
            this.tasks.add(s);
        } else {
            log.debug("already contains " + s);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    synchronized public void deregister(ShutDownTask s) {
        if (s == null) {
            // silently ignore null task
            return;
        }
        if (this.tasks.contains(s)) {
            this.tasks.remove(s);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ShutDownTask> tasks() {
        return Collections.unmodifiableList(tasks);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressFBWarnings(value = "DM_EXIT", justification = "OK to directly exit standalone main")
    @Override
    public boolean shutdown() {
        return shutdown(0, true);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressFBWarnings(value = "DM_EXIT", justification = "OK to directly exit standalone main")
    @Override
    public boolean restart() {
        return shutdown(100, true);
    }

    /**
     * First asks the shutdown tasks if shutdown is allowed. If not return
     * false.
     * <p>
     * Then run the shutdown tasks, and then terminate the program with status 0
     * if not aborted. Does not return under normal circumstances. Does return
     * false if the shutdown was aborted by the user, in which case the program
     * should continue to operate.
     * <p>
     * Executes all registered {@link jmri.ShutDownTask}s before closing any
     * displayable windows.
     *
     * @param status integer status on program exit
     * @param exit   true if System.exit() should be called if all tasks are
     *               executed correctly; false otherwise
     * @return false if shutdown or restart failed
     */
    @SuppressFBWarnings(value = "DM_EXIT", justification = "OK to directly exit standalone main")
    protected boolean shutdown(int status, boolean exit) {
        if (!shuttingDown) {
            Date start = new Date();
            log.debug("Shutting down with {} tasks", this.tasks.size());
            setShuttingDown(true);
            // First check if shut down is allowed
            for (ShutDownTask task : tasks) {
                if (!task.isShutdownAllowed()) {
                    setShuttingDown(false);
                    return false;
                }
            }
            // all shut down tasks must complete within _timeout_ seconds
            long timeout = 30;
            // trigger parallel tasks (see jmri.ShutDownTask#isParallel())
            if (!this.runShutDownTasks(true)) {
                return false;
            }
            log.debug("parallel tasks executed {} milliseconds after starting shutdown", new Date().getTime() - start.getTime());
            // trigger non-parallel tasks
            if (!this.runShutDownTasks(false)) {
                return false;
            }
            log.debug("sequential tasks executed {} milliseconds after starting shutdown", new Date().getTime() - start.getTime());
            // close any open windows by triggering a closing event
            // this gives open windows a final chance to perform any cleanup
            if (!GraphicsEnvironment.isHeadless()) {
                Arrays.asList(Frame.getFrames()).stream().forEach((frame) -> {
                    // do not run on thread, or in parallel, as System.exit()
                    // will get called before windows can close
                    if (frame.isDisplayable()) { // dispose() has not been called
                        log.debug("Closing frame \"{}\", title: \"{}\"", frame.getName(), frame.getTitle());
                        Date timer = new Date();
                        frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
                        log.debug("Frame \"{}\" took {} milliseconds to close", frame.getName(), new Date().getTime() - timer.getTime());
                    }
                });
            }
            log.debug("windows completed closing {} milliseconds after starting shutdown", new Date().getTime() - start.getTime());
            // wait for parallel tasks to complete
            synchronized (start) {
                while (new ArrayList<>(this.tasks).stream().anyMatch((task) -> (task.isParallel() && !task.isComplete()))) {
                    try {
                        start.wait(100);
                    } catch (InterruptedException ex) {
                        // do nothing
                    }
                    if ((new Date().getTime() - start.getTime()) > (timeout * 1000)) { // milliseconds
                        log.warn("Terminating without waiting for the following tasks to complete");
                        this.tasks.forEach((task) -> {
                            if (!task.isComplete()) {
                                log.warn("\t{}", task.getName());
                            }
                        });
                        break;
                    }
                }
            }
            // success
            log.debug("Shutdown took {} milliseconds.", new Date().getTime() - start.getTime());
            log.info("Normal termination complete");
            // and now terminate forcefully
            if (exit) {
                System.exit(status);
            }
        }
        return false;
    }

    /**
     * Run registered shutdown tasks. Any Exceptions are logged and otherwise
     * ignored.
     *
     * @param isParallel true if parallel-capable shutdown tasks are to be run;
     *                   false if shutdown tasks that must be run sequentially
     *                   are to be run
     * @return true if shutdown tasks ran; false if a shutdown task aborted the
     *         shutdown sequence
     */
    private boolean runShutDownTasks(boolean isParallel) {
        // can't return out of a stream or forEach loop
        for (ShutDownTask task : new ArrayList<>(this.tasks)) {
            if (task.isParallel() == isParallel) {
                log.debug("Calling task \"{}\"", task.getName());
                Date timer = new Date();
                try {
                    setShuttingDown(task.execute()); // if a task aborts the shutdown, stop shutting down
                    if (!shuttingDown) {
                        log.info("Program termination aborted by \"{}\"", task.getName());
                        return false;  // abort early
                    }
                } catch (Exception e) {
                    log.error("Error during processing of ShutDownTask \"{}\"", task.getName(), e);
                } catch (Throwable e) {
                    // try logging the error
                    log.error("Unrecoverable error during processing of ShutDownTask \"{}\"", task.getName(), e);
                    log.error("Terminating abnormally");
                    // also dump error directly to System.err in hopes its more observable
                    System.err.println("Unrecoverable error during processing of ShutDownTask \"" + task.getName() + "\"");
                    System.err.println(e);
                    System.err.println("Terminating abnormally");
                    // forcably halt, do not restart, even if requested
                    Runtime.getRuntime().halt(1);
                }
                log.debug("Task \"{}\" took {} milliseconds to execute", task.getName(), new Date().getTime() - timer.getTime());
            }
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isShuttingDown() {
        return shuttingDown;
    }

    /**
     * This method is static so that if multiple DefaultShutDownManagers are
     * registered, they are all aware of this state.
     *
     * @param state true if shutting down; false otherwise
     */
    protected static void setShuttingDown(boolean state) {
        shuttingDown = state;
        log.debug("Setting shuttingDown to {}", state);
    }

}

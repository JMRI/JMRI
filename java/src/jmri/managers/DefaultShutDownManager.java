package jmri.managers;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.event.WindowEvent;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import jmri.ShutDownManager;
import jmri.ShutDownTask;

import jmri.beans.Bean;
import org.openide.util.RequestProcessor;
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

    private static boolean shuttingDown = false;
    private final static Logger log = LoggerFactory.getLogger(DefaultShutDownManager.class);
    private final List<ShutDownTask> tasks = new ArrayList<>();
    private final Set<Callable<Boolean>> callables = new HashSet<>();
    private final Set<Runnable> runnables = new HashSet<>();
    protected final Thread shutdownHook;
    // use up to 8 threads for parallel tasks
    private static final RequestProcessor RP = new RequestProcessor("On Start/Stop", 8); // NOI18N

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
        // shutdown tasks, since that simply calls System.exit();
        this.shutdownHook = jmri.util.ThreadingUtil.newThread(() -> {
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
            log.debug("already contains {}", s);
        }
        this.runnables.add(s);
        this.callables.add(s);
        this.addPropertyChangeListener("shuttingDown", s);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    synchronized public void register(Callable<Boolean> task) {
        Objects.requireNonNull(task, "Shutdown task cannot be null.");
        this.callables.add(task);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    synchronized public void register(Runnable task) {
        Objects.requireNonNull(task, "Shutdown task cannot be null.");
        this.runnables.add(task);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    synchronized public void deregister(ShutDownTask s) {
        this.removePropertyChangeListener("shuttingDown", s);
        this.tasks.remove(s);
        this.callables.remove(s);
        this.runnables.remove(s);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    synchronized public void deregister(Callable task) {
        this.callables.remove(task);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    synchronized public void deregister(Runnable task) {
        this.runnables.remove(task);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("deprecation")
    public List<ShutDownTask> tasks() {
        return Collections.unmodifiableList(tasks);
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
            for (Callable task : callables) {
                try {
                    if (task.call().equals(Boolean.FALSE)) {
                        setShuttingDown(false);
                        return false;
                    }
                } catch (Exception ex) {
                    log.error("Unable to stop", ex);
                    setShuttingDown(false);
                    return false;
                }
            }
            // each shut down tasks must complete within _timeout_ milliseconds
            int timeout = 30000;
            runnables.forEach(task -> RP.post(task, timeout));
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
                while (!RP.isTerminated()) {
                    try {
                        RP.awaitTermination(100, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException ex) {
                        // do nothing   
                    }
                    if ((new Date().getTime() - start.getTime()) > timeout) {
                        log.warn("Terminating without waiting for stop tasks to complete");
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
    protected void setShuttingDown(boolean state) {
        boolean old = shuttingDown;
        shuttingDown = state;
        log.debug("Setting shuttingDown to {}", state);
        firePropertyChange("shuttingDown", old, state);
    }

}

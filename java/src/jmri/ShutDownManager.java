package jmri;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.CheckForNull;

/**
 * Manage tasks to be completed when the program shuts down normally.
 * <p>
 * Implementations of this interface allow other objects to register and
 * deregister {@link ShutDownTask} objects, which are invoked in an orderly way
 * when the program is is commanded to terminate. There is no requirement a
 * ShutDownTask not interact with the user interface, and an assumption that,
 * barring a headless application, that ShutDownTasks may interact with the user
 * interface.
 * <p>
 * There can only be one instance of this operating, and it is generally
 * obtained via the instance manager.
 * <p>
 * ShutDownTasks should leave the system in a state that can continue, in case a
 * later task aborts the shutdown.
 * <p>
 * Although ShutDownTasks can use {@link ShutDownTask#isParallel()} to notify
 * the ShutDownManager that the ShutDownTask will spawn its own thread to
 * perform its task, there is no requirement that the ShutDownManager wait for
 * these tasks to complete (although it is possible to put a computer or command
 * station into a poor state by not waiting for these tasks to complete).
 * <p>
 * An instance of this is normally obtained from the instance manager, using may
 * assume that one is always present.
 *
 * @author Bob Jacobsen Copyright (C) 2008
 */
public interface ShutDownManager {

    /**
     * Register a task object for later execution. An attempt to register an
     * already registered task will be silently ignored.
     *
     * @param task the task to execute
     * @throws NullPointerException if the task is null
     */
    public void register(@Nonnull ShutDownTask task);

    /**
     * Deregister a task object. Attempts to deregister a task that is not
     * registered are silently ignored.
     *
     * @param task the task not to execute
     */
    public void deregister(@CheckForNull ShutDownTask task);

    /**
     * Provide access to the current registered shutdown tasks.
     * <p>
     * Note that implementations are free to provide a copy of the list of
     * registered tasks and do not need to provide modifiable live access to the
     * internal list of registered tasks.
     * 
     * @return the list of shutdown tasks or an empty list if no shutdown tasks
     *         are registered
     */
    @Nonnull
    public List<ShutDownTask> tasks();

    /**
     * Run the shutdown tasks, and then terminate the program with status 100 if
     * not aborted. Does not return under normal circumstances. Returns false if
     * the shutdown was aborted by the user, in which case the program should
     * continue to operate.
     * <p>
     * By exiting the program with status 100, the batch file (MS Windows) or
     * shell script (Linux/macOS/UNIX) can catch the exit status and restart the
     * java program.
     * <p>
     * <b>NOTE</b> If the macOS {@literal application->quit} menu item is used,
     * this must return false to abort the shutdown.
     *
     * @return false if any shutdown task aborts restarting the application
     */
    public boolean restart();

    /**
     * Run the shutdown tasks, and then terminate the program with status 0 if
     * not aborted. Does not return under normal circumstances. Returns false if
     * the shutdown was aborted by the user, in which case the program should
     * continue to operate.
     * <p>
     * <b>NOTE</b> If the macOS {@literal application->quit} menu item is used,
     * this must return false to abort the shutdown.
     *
     * @return false if any shutdown task aborts the shutdown or if anything
     *         goes wrong.
     */
    public boolean shutdown();

    /**
     * Allow components that normally request confirmation to shutdown to
     * determine if the shutdown is already underway so as not to request
     * confirmation.
     *
     * @return true if shutting down or restarting
     */
    public boolean isShuttingDown();
}

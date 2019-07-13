package jmri;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Manage tasks to be completed when the program shuts down normally.
 * Specifically, allows other object to register and deregister
 * {@link ShutDownTask} objects, which are invoked in an orderly way when the
 * program is is commanded to terminate.
 * <p>
 * Operations:
 * <ol>
 * <li>Execute each {@link ShutDownTask} in order reverse order of creation,
 * allowing it to abort the shutdown if needed.
 * <li>If not aborted, terminate the program.
 * </ol>
 * <p>
 * There can only be one instance of this operating, and it is generally
 * obtained via the instance manager.
 * <p>
 * Items are executed in reverse order to attempt to unwind the creation
 * process. Tasks should not count on this, however, as shutdown can be aborted
 * before a particular task is reached.
 * <p>
 * Tasks should leave the system in a state that can continue, in case a later
 * task aborts the shutdown.
 * <p>
 * An instance of this is normally obtained from the instance manager, but using
 * routines should not assume that one is always present; they should check for
 * a null manager and skip operations if needed.
 *
 * @author Bob Jacobsen Copyright (C) 2008
 */
public interface ShutDownManager {

    /**
     * Register a task object for later execution. If called with an already
     * registered task, the task is not registered twice.
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
    public void deregister(@Nullable ShutDownTask task);

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
     * not aborted. Does not return under normal circumstances. Does return
     * false if the shutdown was aborted by the user, in which case the program
     * should continue to operate.
     * <p>
     * By exiting the program with status 100, the batch file (MS Windows) or
     * shell script (Linux/macOS/UNIX) can catch the exit status and restart
     * the java program.
     * <p>
     * <b>NOTE</b> If the macOS {@literal application->quit} menu item is used,
     * this must return false to abort the shutdown.
     *
     * @return boolean which should be false
     */
    public boolean restart();

    /**
     * First asks the shutdown tasks if shutdown is allowed. If not return false.
     * <p>
     * Then run the shutdown tasks, and then terminate the program with status 0
     * if not aborted. Does not return under normal circumstances. Does return
     * false if the shutdown was aborted by the user, in which case the program
     * should continue to operate.
     * <p>
     * <b>NOTE</b> If the macOS {@literal application->quit} menu item is used,
     * this must return false to abort the shutdown.
     *
     * @return false if any shutdown task aborts the shutdown or if anything goes
     * wrong.
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

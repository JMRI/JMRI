package jmri;

import java.beans.PropertyChangeListener;
import java.util.concurrent.Callable;

/**
 * Execute a specific task before the program terminates.
 * <p>
 * Tasks should leave the system in a state that can continue, in case a later
 * task aborts the shutdown.
 * <p>
 * A ShutDownTask can listen to the "shuttingDown" property of the
 * {@link ShutDownManager} to determine if any other ShutDownTask aborted the
 * shutdown; the property will change from false to true in that case.
 *
 * @author Bob Jacobsen Copyright (C) 2008
 */
public interface ShutDownTask extends Callable<Boolean>, Runnable, PropertyChangeListener {

    /**
     * Ask if shut down is allowed.
     * <p>
     * The shut down manager must call this method first on all the tasks before
     * starting to execute the method execute() on the tasks.
     * <p>
     * If this method returns false on any task, the shut down process must be
     * aborted.
     *
     * @return true if it is OK to shut down, false to abort shut down.
     * @deprecated since 4.21.1; use {@link #call()} instead
     */
    @Deprecated
    public boolean isShutdownAllowed();

    /**
     * Ask if shut down is allowed.
     * <p>
     * The shut down manager calls this method first on all the tasks before
     * starting to execute the method {@link #run()} on the tasks.
     * <p>
     * If this method returns false on any task, the shut down process must be
     * aborted.
     *
     * @return true if it is OK to shut down, false to abort shut down.
     * @throws Exception if there is an exception
     */
    @Override
    public Boolean call() throws Exception;

    /**
     * Take the necessary action.
     * <p>
     * If the task is lengthy and can easily confirm that it should proceed
     * (i.e., prompt the user to save, not save, or cancel shutting down), and
     * spin itself off into a new thread, it should do so.
     * <p>
     * <strong>Note</strong> if a task is parallel, this method should return
     * <em>after</em> any tests that might cause the shutdown to be aborted.
     *
     * @return true if the shutdown should continue, false to abort.
     * @deprecated since 4.21.1; use {@link #run()} instead
     */
    @Deprecated
    public boolean execute();

    /**
     * Take the necessary action. This method cannot abort the shutdown, and
     * must not require user interaction to complete successfully. This method
     * will be run in parallel to other ShutDownTasks.
     */
    @Override
    public void run();

    /**
     * Name to be provided to the user when information about this task is
     * presented.
     *
     * @return the name
     */
    public String getName();

    /**
     * Advise {@link jmri.ShutDownManager}s if {@link #execute()} may return
     * before the task is complete.
     * <p>
     * <strong>Note</strong> if a task is parallel, {@link #execute()} should
     * return <em>after</em> any tests that might cause the shutdown to be
     * aborted.
     *
     * @return true if the task is run within its own Thread
     * @deprecated since 4.21.1; this is ignored and all tasks are run in an
     * independent Thread
     */
    @Deprecated
    public boolean isParallel();

    /**
     * Advise {@link jmri.ShutDownManager}s that the task is complete.
     *
     * @return true if the task is complete
     * @deprecated since 4.21.1; this is ignored as all tasks are run in an
     * independent Thread that is independently monitored
     */
    @Deprecated
    public boolean isComplete();
}

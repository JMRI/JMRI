package jmri;

/**
 * Execute a specific task before the program terminates.
 * <p>
 * Tasks should leave the system in a state that can continue, in case a later
 * task aborts the shutdown.
 *
 * @author Bob Jacobsen Copyright (C) 2008
 */
public interface ShutDownTask {

    /**
     * Take the necessary action.
     * <p>
     * If the task is lengthy and can easily confirm that it should proceed
     * (i.e., prompt the user to save, not save, or cancel shutting down), and
     * spin itself off into a new thread, it should do so.
     * <p>
     * <strong>Note</strong> if a task is parallel, this method should
     * return <em>after</em> any tests that might cause the shutdown to be
     * aborted.
     *
     * @return true if the shutdown should continue, false to abort.
     */
    public boolean execute();

    /**
     * Name to be provided to the user when information about this task is
     * presented.
     *
     * @return the name
     */
    public String getName();

    /**
     * Name to be provided to the user when information about this task is
     * presented.
     *
     * @return the name
     * @deprecated since 4.3.6; use {@link #getName()} instead
     */
    @Deprecated
    public String name();

    /**
     * Advise {@link jmri.ShutDownManager}s if {@link #execute()} may return
     * before the task is complete.
     * <p>
     * <strong>Note</strong> if a task is parallel, {@link #execute()} should
     * return <em>after</em> any tests that might cause the shutdown to be
     * aborted.
     *
     * @return true if the task is run within its own Thread
     */
    public boolean isParallel();

    /**
     * Advise {@link jmri.ShutDownManager}s that the task is complete.
     *
     * @return true if the task is complete
     */
    public boolean isComplete();
}

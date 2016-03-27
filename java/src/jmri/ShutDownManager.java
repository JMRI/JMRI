package jmri;

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
 * @version	$Revision$
 */
public interface ShutDownManager {

    /**
     * Register a task object for later execution
     */
    public void register(ShutDownTask s);

    /**
     * Deregister a task object.
     *
     * @throws IllegalArgumentException if task object not currently registered
     */
    public void deregister(ShutDownTask s);

    /**
     * Run the shutdown tasks, and then terminate the program with status 100 if
     * not aborted. Does not return under normal circumstances. Does return
     * False if the shutdown was aborted by the user, in which case the program
     * should continue to operate.
     * <b>NOTE</b> If the OS X application->quit menu item is used, this must
     * return false to abort the shutdown.
     *
     * @return boolean which should be False
     */
    public Boolean restart();

    /**
     * Run the shutdown tasks, and then terminate the program with status 0 if
     * not aborted. Does not return under normal circumstances. Does return
     * False if the shutdown was aborted by the user, in which case the program
     * should continue to operate.
     * <b>NOTE</b> If the OS X application->quit menu item is used, this must
     * return false to abort the shutdown.
     *
     * @return boolean which should be False
     */
    public Boolean shutdown();
}

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
}

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
     *
     * @return true if the shutdown should continue, false to abort.
     */
    public boolean execute();

    /**
     * Name to be provided to the user when information about this task is
     * presented.
     */
    public String name();
}

package jmri.implementation;

import jmri.ShutDownTask;

/**
 * Handle name for ShutDownTask implementations.
 *
 * @author Bob Jacobsen Copyright (C) 2008
 */
public abstract class AbstractShutDownTask implements ShutDownTask {

    private final String mName;

    /**
     * Constructor specifies the name
     *
     * @param name Name to give this task
     */
    public AbstractShutDownTask(String name) {
        this.mName = name;
    }

    /**
     * Ask if shut down is allowed.
     * <p>
     * The shut down manager must call this method first on all the tasks
     * before starting to execute the method execute() on the tasks.
     * <p>
     * If this method returns false on any task, the shut down process must
     * be aborted.
     *
     * @return true if it is OK to shut down, false to abort shut down.
     */
    @Override
    public boolean isShutdownAllowed() {
        return true;
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public boolean isParallel() {
        return false;
    }

    @Override
    public boolean isComplete() {
        return !this.isParallel();
    }
}

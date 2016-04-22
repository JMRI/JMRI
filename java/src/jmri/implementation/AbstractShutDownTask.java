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
     * @param name
     */
    public AbstractShutDownTask(String name) {
        this.mName = name;
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    @SuppressWarnings("deprecation")
    public String name() {
        return this.getName();
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

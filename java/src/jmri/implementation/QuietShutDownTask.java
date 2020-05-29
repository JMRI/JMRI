package jmri.implementation;


/**
 * Provides a base to perform a shutdown task without user-intervention.
 *
 * @author Matthew Harris Copyright (c) 2008
 * @deprecated since 4.21.1; use {@link AbstractShutDownTask} instead
 */
@Deprecated
public abstract class QuietShutDownTask extends AbstractShutDownTask {

    /**
     * Constructor specifies the shutdown task name
     * @param name Name to give this task
     */
    public QuietShutDownTask(String name) {
        super(name);
    }

}

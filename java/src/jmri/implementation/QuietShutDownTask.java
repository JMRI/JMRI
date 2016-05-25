package jmri.implementation;


/**
 * Provides a base to perform a shutdown task without user-intervention.
 *
 * @author Matthew Harris Copyright (c) 2008
 */
public abstract class QuietShutDownTask extends AbstractShutDownTask {

    /**
     * Constructor specifies the shutdown task name
     * @param name
     */
    public QuietShutDownTask(String name) {
        super(name);
    }

    /**
     * Provide a subclass-specific method to handle the request to fix the
     * problem. This is a dummy implementation, intended to be overloaded.
     *
     * @return true if ready to shutdown, false to end shutdown
     * @deprecated Since 4.3.6; override {@link #execute()} directly
     */
    @Deprecated
    protected boolean doAction() {
        return true;
    }

}

// QuietShutDownTask.java

package jmri.implementation;

import org.apache.log4j.Logger;

/**
 * Provides a base to perform a shutdown task without user-intervention.
 * 
 * @author      Matthew Harris Copyright (c) 2008
 * @version     $Revision$
 */
public class QuietShutDownTask extends AbstractShutDownTask {

    /** 
     * Constructor specifies the shutdown task name
     */
    public QuietShutDownTask(String name) {
        super(name);
    }
    
    /**
     * Take the necessary action.
     * @return true if the shutdown should continue, false
     * to abort.
     */
    public boolean execute() {
        return doAction();
    }

    /**
     * Provide a subclass-specific method to handle the
     * request to fix the problem. This is a dummy implementation,
     * intended to be overloaded.
     * @return true if ready to shutdown, false to end shutdown
     */
    protected boolean doAction() {
        return true;
    }
    
    static Logger log = Logger.getLogger(QuietShutDownTask.class.getName());

}

/* @(#)QuietShutDownTask.java */

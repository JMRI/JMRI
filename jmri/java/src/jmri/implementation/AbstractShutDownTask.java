// AbstractShutDownTask.java

package jmri.implementation;

import jmri.ShutDownTask;

/**
 * Handle name for ShutDownTask implementations.
 *
 * @author      Bob Jacobsen Copyright (C) 2008
 * @version	$Revision$
 */
public abstract class AbstractShutDownTask implements ShutDownTask {
    
    /** 
     * Constructor specifies the name
     */
    public AbstractShutDownTask(String name) {
        this.mName = name;
    }
    
    String mName;

    public String name() {
        return mName;
    }
    
}

/* @(#)AbstractShutDownTask.java */

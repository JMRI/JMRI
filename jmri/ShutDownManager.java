// ShutDownManager.java

package jmri;


/**
 * Manage tasks to be completed when the
 * program shuts down normally.
 * Specifically, allows other object to 
 * register and deregister {@link ShutDownTask} objects, 
 * which are invoked in an orderly way when the program is
 * is commanded to terminate.
 * <p>
 * Operations:
 * <ol>
 * <li>Execute each {@link ShutDownTask} in order, 
 *     allowing it to abort the shutdown if needed.
 * <li>If not aborted, terminate the program.
 * </ol>
 * <p>
 * There can only be one instance of this operating,
 * and it is generally obtained via the instance manager.
 *
 * @author      Bob Jacobsen Copyright (C) 2008
 * @version	$Revision: 1.1 $
 */
public interface ShutDownManager {

    /**
     * Register a task object for later execution
     */
    public void register(ShutDownTask s);
    
    /**
     * Deregister a task object.  
     * @throws IllegalArgumentException if task object not currently registered
     */
    public void deregister(ShutDownTask s);
    
    /**
     * Run the shutdown tasks, and 
     * then terminate the program if not aborted.
     * Does not return under normal circumstances.
     * Does return if the shutdown was aborted by the user,
     * in which case the program should continue to operate.
     */
    public void shutdown();
}

/* @(#)ShutDownManager.java */

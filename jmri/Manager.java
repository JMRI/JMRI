// Manager.java

package jmri;

/**
 * Basic interface for managers.
 * <P>
 * Right now, it just contains the members needed by InstanceManager to handle
 * managers for more than one system.
 *
 * @author      Bob Jacobsen Copyright (C) 2003
 * @version	$Revision: 1.1 $
 */
public interface Manager {

    /**
     * @return The system-specific prefix letter for a specific implementation
     */
    public char systemLetter();

    /**
     * Free resources when no longer used. Specifically, remove all references
     * to and from this object, so it can be garbage-collected.
     */
    public void dispose() throws JmriException;

}


/* @(#)TurnoutManager.java */

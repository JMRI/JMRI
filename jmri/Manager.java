// Manager.java

package jmri;

import java.util.Hashtable;
import java.util.Enumeration;
import com.sun.java.util.collections.List;
import com.sun.java.util.collections.ArrayList;
import com.sun.java.util.collections.Collections;

/**
 * Basic interface for managers.
 * <P>
 * Right now, it just contains the members needed by InstanceManager to handle
 * managers for more than one system.
 * <P>
 * Although they are not defined here because their return type differs, an individual manager
 * provides "get" methods to locate specific objects, and a "new" method
 * to create a new one via the Factory pattern.
 * The "get" methods will
 * return an existing object or null, and will never create a new object.
 * The "new" method will log a warning if an object already exists with
 * that system name.
 *
 * @author      Bob Jacobsen Copyright (C) 2003
 * @version	$Revision: 1.6 $
 */
public interface Manager {

    /**
     * @return The system-specific prefix letter for a specific implementation
     */
    public char systemLetter();

    /**
     * @return The type letter for a specific implementation
     */
    public char typeLetter();

    /**
     * @return A system name from a user input, typically a number.
     */
    public String makeSystemName(String s);

    /**
     * Free resources when no longer used. Specifically, remove all references
     * to and from this object, so it can be garbage-collected.
     */
    public void dispose();

    public List getSystemNameList();

    public void addPropertyChangeListener(java.beans.PropertyChangeListener l);
    public void removePropertyChangeListener(java.beans.PropertyChangeListener l);
    public void register(NamedBean n);
}


/* @(#)TurnoutManager.java */

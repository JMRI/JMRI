// Memory.java

package jmri;

/**
 * Represent a Memory, a place to store values.
 * <P>
 * The AbstractMemory class contains a basic implementation of the state and messaging
 * code, and forms a useful start for a system-specific implementation.
 * Specific implementations in the jmrix package, e.g. for LocoNet and NCE, will
 * convert to and from the layout commands.
 * <P>
 * The states  and names are Java Bean parameters, so that listeners can be
 * registered to be notified of any changes.
 * <P>
 * Each Memory object has a two names.  The "user" name is entirely free form, and
 * can be used for any purpose.  The "system" name is provided by the system-specific
 * implementations, and provides a unique mapping to the layout control system
 * (e.g. LocoNet, NCE, etc) and address within that system.
 * <P>
 *
 * @author	Bob Jacobsen  Copyright (C) 2001
 * @version	$Revision: 1.1 $
 * @see         jmri.AbstractMemory
 * @see         jmri.MemoryManager
 * @see         jmri.InstanceManager
 */
public interface Memory extends NamedBean {

    /**
     * Get the stored value.  The type of this depends on what 
     * was stored...
     */
    public Object getValue();

    /**
     * Set the value.  Any type of Object can be stored,
     * but various utilities use the toString method of the
     * stored Object.
     */
    public void setValue(Object value);

}

/* @(#)Memory.java */

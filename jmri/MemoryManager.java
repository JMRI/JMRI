// MemoryManager.java

package jmri;

import com.sun.java.util.collections.List;

/**
 * Locate a Memory object representing some specific information.
 *<P>
 * Memory objects are obtained from a MemoryManager, which in turn is generally located
 * from the InstanceManager. A typical call
 * sequence might be:
 *<PRE>
 * Memory memory = InstanceManager.memoryManagerInstance().provideMemory("status");
 *</PRE>
 * <P>
 * Each Memory has a two names.  The "user" name is entirely free form, and
 * can be used for any purpose.  The "system" name is provided by the system-specific
 * implementations, if any, and provides a unique mapping to the layout control system
 * (e.g. LocoNet, NCE, etc) and address within that system. Note that 
 * most (all?) layout systems don't have anything corresponding to this, in which
 * case the "Internal" Memory objects are still available with names like IM23.
 * <P>
 * Much of the book-keeping is implemented in the AbstractMemoryManager class, which
 * can form the basis for a system-specific implementation.
 *
 * @author			Bob Jacobsen Copyright (C) 2004
 * @version			$Revision: 1.1 $
 * @see             jmri.Memory
 * @see             jmri.AbstractMemoryManager
 * @see             jmri.InstanceManager
 */
public interface MemoryManager extends Manager {

    /**
     * Locate via user name, then system name if needed.
     * If that fails, create a new Memory. If the name
     * is a valid system name, it will be used for the new
     * Memory.  Otherwise, the makeSystemName method
     * will attempt to turn it into a valid system name.
     *
     * @param name
     * @return Never null under normal circumstances
     */
    public Memory provideMemory(String name);

    /**
     * Locate via user name, then system name if needed.
     * If that fails, return null
     *
     * @param name
     * @return null if no match found
     */
    public Memory getMemory(String name);

    /**
     * Locate an instance based on a system name.  Returns null if no
     * instance already exists.
     * @return requested Memory object or null if none exists
     */
    public Memory getBySystemName(String systemName);

    /**
     * Locate an instance based on a user name.  Returns null if no
     * instance already exists.
     * @return requested Memory object or null if none exists
     */
    public Memory getByUserName(String userName);

    /**
     * Return an instance with the specified system and user names.
     * Note that two calls with the same arguments will get the same instance;
     * there is only one Memory object representing a given physical Memory
     * and therefore only one with a specific system or user name.
     *<P>
     * This will always return a valid object reference; a new object will be
     * created if necessary. In that case:
     *<UL>
     *<LI>If a null reference is given for user name, no user name will be associated
     *    with the Memory object created; a valid system name must be provided
     *<LI>If both names are provided, the system name defines the
     *    hardware access of the desired Memory, and the user address
     *    is associated with it. The system name must be valid.
     *</UL>
     * Note that it is possible to make an inconsistent request if both
     * addresses are provided, but the given values are associated with
     * different objects.  This is a problem, and we don't have a
     * good solution except to issue warnings.
     * This will mostly happen if you're creating Memorys when you should
     * be looking them up.
     * @return requested Memory object (never null)
     */
    public Memory newMemory(String systemName, String userName);

    /**
     * Get a list of all Memorys' system names.
     */
    public List getSystemNameList();

}


/* @(#)MemoryManager.java */

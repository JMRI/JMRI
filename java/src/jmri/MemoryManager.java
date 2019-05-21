package jmri;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Locate a Memory object representing some specific information.
 * <p>
 * Memory objects are obtained from a MemoryManager, which in turn is generally
 * located from the InstanceManager. A typical call sequence might be:
 * <pre>
 * Memory memory = InstanceManager.memoryManagerInstance().provideMemory("status");
 * </pre>
 * <p>
 * Each Memory has a two names. The "user" name is entirely free form, and can
 * be used for any purpose. The "system" name is provided by the system-specific
 * implementations, if any, and provides a unique mapping to the layout control
 * system (for example LocoNet or NCE) and address within that system. Note that
 * most (all?) layout systems don't have anything corresponding to this, in
 * which case the "Internal" Memory objects are still available with names like
 * IM23.
 * <p>
 * Much of the book-keeping is implemented in the AbstractMemoryManager class,
 * which can form the basis for a system-specific implementation.
 *
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author Bob Jacobsen Copyright (C) 2004
 * @see jmri.Memory
 * @see jmri.managers.AbstractMemoryManager
 * @see jmri.InstanceManager
 */
public interface MemoryManager extends ProvidingManager<Memory> {

    /**
     * Get the Memory with the user name, then system name if needed; if that fails, create a
     * new Memory. 
     * If the name is a valid system name, it will be used for the
     * new Memory. Otherwise, the makeSystemName method will attempt to turn it
     * into a valid system name.
     *
     * @param name User name, system name, or address which can be promoted to
     *             system name
     * @return Never null
     * @throws IllegalArgumentException if Memory doesn't already exist and the
     *                                  manager cannot create the Memory due to
     *                                  an illegal name or name that can't
     *                                  be parsed.
     */
    @Nonnull
    public Memory provideMemory(@Nonnull String name) throws IllegalArgumentException;

    /**
     * Get an existing Turnout or return null if it doesn't exist. 
     * 
     * Locates via user name, then system name if needed.
     *
     * @param name User name or system name to match
     * @return null if no match found
     */
    @CheckForNull
    public Memory getMemory(@Nonnull String name);

    /**
     * Locate an existing Memory based on a system name. Returns null if no instance
     * already exists.
     *
     * @param systemName the system name
     * @return requested Memory object or null if none exists
     */
    @CheckForNull
    public Memory getBySystemName(@Nonnull String systemName);

    /**
     * Locate an existing Memory based on a user name. Returns null if no instance
     * already exists.
     *
     * @param userName the user name
     * @return requested Memory object or null if none exists
     */
    @CheckForNull
    public Memory getByUserName(@Nonnull String userName);

    /**
     * Return a Memory with the specified system and user names. Note that
     * two calls with the same arguments will get the same instance; there is
     * only one Memory object representing a given physical Memory and therefore
     * only one with a specific system or user name.
     * <p>
     * This will always return a valid object reference; a new object will be
     * created if necessary. In that case:
     * <ul>
     * <li>If a null reference is given for user name, no user name will be
     * associated with the Memory object created; a valid system name must be
     * provided
     * <li>If both names are provided, the system name defines the hardware
     * access of the desired Memory, and the user address is associated with it.
     * The system name must be valid.
     * </ul>
     * Note that it is possible to make an inconsistent request if both
     * addresses are provided, but the given values are associated with
     * different objects. This is a problem, and we don't have a good solution
     * except to issue warnings. This will mostly happen if you're creating
     * Memory objects when you should be looking them up.
     *
     * @param systemName the system name
     * @param userName   the user name
     * @return requested Memory object (never null)
     * @throws IllegalArgumentException if cannot create the Memory due to e.g.
     *                                  an illegal name or name that can't be
     *                                  parsed.
     */
    @Nonnull
    public Memory newMemory(@Nonnull String systemName, String userName);

    /**
     * For use with User GUI, to allow the auto generation of systemNames, where
     * the user can optionally supply a username.
     * <p>
     * This will always return a valid object reference; a new object will be
     * created if necessary. (If a null reference is given for user name, no
     * user name will be associated with the Memory object created)
     *
     * Note that it is possible to make an inconsistent request if both
     * addresses are provided, but the given values are associated with
     * different objects. This is a problem, and we don't have a good solution
     * except to issue warnings. This will mostly happen if you're creating
     * Memory objects when you should be looking them up.
     *
     * @param userName the user name
     * @return requested Memory object (never null)
     * @throws IllegalArgumentException if cannot create the Memory due to e.g.
     *                                  an illegal name or name that can't be
     *                                  parsed.
     */
    @Nonnull
    public Memory newMemory(@Nonnull String userName);

}
